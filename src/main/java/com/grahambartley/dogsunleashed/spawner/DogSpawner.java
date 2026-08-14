package com.grahambartley.dogsunleashed.spawner;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.spawner.SpecialSpawner;

/**
 * Cap-independent dog spawner, modeled on vanilla {@code CatSpawner}. Vanilla passive spawning is
 * gated by the shared CREATURE mob cap, which stays permanently saturated in explored terrain (and
 * on servers where other mods fill it), so dogs effectively only spawn at chunk generation. This
 * spawner ticks from {@code ServerWorld.tickSpawners} outside that cap, periodically spawning one
 * wild pack near a random player in a matching biome. It is gated by {@code
 * capIndependentSpawningEnabled} (on by default, opt-out), re-checks the config every attempt (no
 * restart needed), respects {@code doMobSpawning} and the spawn-animals flag, validates positions
 * with the same spawn predicate as natural spawning, and enforces its own conservative cap on
 * nearby untamed dogs. Dogs it spawns are flagged for far-distance despawn via {@link
 * UnleashedDogEntity#canImmediatelyDespawn} so the world never fills up monotonically.
 */
public class DogSpawner implements SpecialSpawner {

  static final int MIN_COOLDOWN_TICKS = 1200;
  static final int COOLDOWN_JITTER_TICKS = 1200;
  static final int MIN_SPAWN_DISTANCE_BLOCKS = 24;
  static final int MAX_SPAWN_DISTANCE_BLOCKS = 48;
  static final int PACK_SCATTER_RADIUS_BLOCKS = 3;
  public static final int UNTAMED_DOG_CAP = 4;
  public static final int UNTAMED_DOG_CAP_RADIUS_BLOCKS = 64;
  private static final int REGION_LOADED_MARGIN_BLOCKS = 8;

  private int cooldownTicks = 0;

  @Override
  public int spawn(
      final ServerWorld world, final boolean spawnMonsters, final boolean spawnAnimals) {
    final DogsUnleashedConfig config = DogsUnleashed.SERVER_CONFIG;
    if (!config.capIndependentSpawningEnabled() || !config.enableNaturalSpawning()) {
      return 0;
    }
    if (!spawnAnimals || !world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)) {
      return 0;
    }
    --this.cooldownTicks;
    if (this.cooldownTicks > 0) {
      return 0;
    }
    this.cooldownTicks = MIN_COOLDOWN_TICKS + world.random.nextInt(COOLDOWN_JITTER_TICKS);
    return trySpawnPack(world, config);
  }

  private int trySpawnPack(final ServerWorld world, final DogsUnleashedConfig config) {
    final ServerPlayerEntity player = world.getRandomAlivePlayer();
    if (player == null) {
      return 0;
    }
    final Random random = world.random;
    final BlockPos anchor = player.getBlockPos();
    final BlockPos candidate = anchor.add(nextSpawnOffset(random), 0, nextSpawnOffset(random));
    if (!world.isRegionLoaded(
        candidate.getX() - REGION_LOADED_MARGIN_BLOCKS,
        candidate.getZ() - REGION_LOADED_MARGIN_BLOCKS,
        candidate.getX() + REGION_LOADED_MARGIN_BLOCKS,
        candidate.getZ() + REGION_LOADED_MARGIN_BLOCKS)) {
      return 0;
    }
    final BlockPos surface =
        world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, candidate);
    final List<UnleashedDogBreed> candidates = spawnableBreedsIn(world.getBiome(surface), config);
    if (candidates.isEmpty()) {
      return 0;
    }
    // The multipliers scale how often an attempt proceeds: at 100% every attempt does (like
    // CatSpawner), below 100% attempts are skipped proportionally, and above 100% the extra boost
    // is already reflected in the weighted breed pick and the baked vanilla pool weights.
    final int totalBase = totalBaseWeight(candidates);
    final int totalEffective = totalEffectiveWeight(candidates, config);
    if (totalEffective < totalBase && random.nextInt(totalBase) >= totalEffective) {
      return 0;
    }
    if (countUntamedDogsNear(world, anchor) >= UNTAMED_DOG_CAP) {
      return 0;
    }
    final UnleashedDogBreed breed =
        pickWeighted(candidates, config, random.nextInt(totalEffective));
    return spawnPack(world, breed, surface);
  }

  private int spawnPack(
      final ServerWorld world, final UnleashedDogBreed breed, final BlockPos center) {
    final UnleashedDogBreed.SpawnSettings settings = breed.spawnSettings();
    final Random random = world.random;
    final int packSize =
        settings.minGroupSize()
            + random.nextInt(settings.maxGroupSize() - settings.minGroupSize() + 1);
    final EntityType<? extends UnleashedDogEntity> type = ModEntities.getDogEntityType(breed);
    EntityData entityData = null;
    int spawned = 0;
    for (int i = 0; i < packSize; i++) {
      final BlockPos pos = i == 0 ? center : scatterAround(world, center, random);
      if (!SpawnRestriction.isSpawnPosAllowed(type, world, pos)
          || !SpawnRestriction.canSpawn(type, world, SpawnReason.NATURAL, pos, random)) {
        continue;
      }
      final UnleashedDogEntity dog = type.create(world);
      if (dog == null) {
        continue;
      }
      dog.refreshPositionAndAngles(pos, random.nextFloat() * 360.0F, 0.0F);
      if (!world.isSpaceEmpty(dog)) {
        dog.discard();
        continue;
      }
      dog.setSpawnedByDogSpawner(true);
      entityData =
          dog.initialize(world, world.getLocalDifficulty(pos), SpawnReason.NATURAL, entityData);
      world.spawnEntityAndPassengers(dog);
      spawned++;
    }
    return spawned;
  }

  // The helpers below are public so gametests can assert the spawner's observable decisions
  // (candidate resolution, weighting, cap counting) against a live world without spawning packs
  // at uncontrolled world positions. Production code calls them from spawn() exclusively.

  public static List<UnleashedDogBreed> spawnableBreedsIn(
      final RegistryEntry<Biome> biome, final DogsUnleashedConfig config) {
    final List<UnleashedDogBreed> matching = new ArrayList<>();
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      final UnleashedDogBreed.SpawnSettings settings = breed.spawnSettings();
      if (config.effectiveSpawnWeight(settings.weight(), breed.serializedId()) == 0) {
        continue;
      }
      for (final RegistryKey<Biome> key : settings.biomes()) {
        if (biome.matchesKey(key)) {
          matching.add(breed);
          break;
        }
      }
    }
    return matching;
  }

  public static int totalBaseWeight(final List<UnleashedDogBreed> breeds) {
    int total = 0;
    for (final UnleashedDogBreed breed : breeds) {
      total += breed.spawnSettings().weight();
    }
    return total;
  }

  public static int totalEffectiveWeight(
      final List<UnleashedDogBreed> breeds, final DogsUnleashedConfig config) {
    int total = 0;
    for (final UnleashedDogBreed breed : breeds) {
      total += config.effectiveSpawnWeight(breed.spawnSettings().weight(), breed.serializedId());
    }
    return total;
  }

  /**
   * Picks a breed from a non-empty candidate list by effective spawn weight. The roll must be in
   * {@code [0, totalEffectiveWeight(candidates, config))}.
   */
  public static UnleashedDogBreed pickWeighted(
      final List<UnleashedDogBreed> candidates, final DogsUnleashedConfig config, final int roll) {
    int remaining = roll;
    for (final UnleashedDogBreed breed : candidates) {
      remaining -=
          config.effectiveSpawnWeight(breed.spawnSettings().weight(), breed.serializedId());
      if (remaining < 0) {
        return breed;
      }
    }
    return candidates.get(candidates.size() - 1);
  }

  public static int countUntamedDogsNear(final ServerWorld world, final BlockPos center) {
    final Box searchBox = new Box(center).expand(UNTAMED_DOG_CAP_RADIUS_BLOCKS);
    int count = 0;
    for (final UnleashedDogEntity dog :
        world.getNonSpectatingEntities(UnleashedDogEntity.class, searchBox)) {
      if (!dog.isTamed()) {
        count++;
      }
    }
    return count;
  }

  private static int nextSpawnOffset(final Random random) {
    final int magnitude =
        MIN_SPAWN_DISTANCE_BLOCKS
            + random.nextInt(MAX_SPAWN_DISTANCE_BLOCKS - MIN_SPAWN_DISTANCE_BLOCKS + 1);
    return random.nextBoolean() ? magnitude : -magnitude;
  }

  private static BlockPos scatterAround(
      final ServerWorld world, final BlockPos center, final Random random) {
    final BlockPos offset =
        center.add(
            random.nextInt(PACK_SCATTER_RADIUS_BLOCKS * 2 + 1) - PACK_SCATTER_RADIUS_BLOCKS,
            0,
            random.nextInt(PACK_SCATTER_RADIUS_BLOCKS * 2 + 1) - PACK_SCATTER_RADIUS_BLOCKS);
    return world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, offset);
  }
}
