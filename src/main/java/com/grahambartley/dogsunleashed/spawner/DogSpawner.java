package com.grahambartley.dogsunleashed.spawner;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.DogSpawnerOrigin;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
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

public class DogSpawner implements SpecialSpawner {

  static final int MIN_COOLDOWN_TICKS = 1200;
  static final int COOLDOWN_JITTER_TICKS = 1200;
  static final int MIN_SPAWN_DISTANCE_BLOCKS = 24;
  static final int MAX_SPAWN_DISTANCE_BLOCKS = 48;
  static final int GROUP_SCATTER_RADIUS_BLOCKS = 3;
  public static final int WILD_GROUP_CAP = 2;
  public static final int WILD_GROUP_CAP_RADIUS_BLOCKS = 64;
  private static final int REGION_LOADED_MARGIN_BLOCKS = 8;
  private static final List<UnleashedDogBreed> NATURAL_BREEDS =
      Stream.of(UnleashedDogBreed.values()).filter(UnleashedDogBreed::isNaturallySpawning).toList();

  private int cooldownTicks = 0;

  @Override
  public int spawn(
      final ServerWorld world, final boolean spawnMonsters, final boolean spawnAnimals) {
    final DogsUnleashedConfig config = DogsUnleashed.SERVER_CONFIG;
    if (!config.spawning().enableNaturalSpawning()
        || !config.spawning().capIndependentSpawningEnabled()) {
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
    final int tries = DogSpawnRates.triesPerCycle(NATURAL_BREEDS, config);
    int spawned = 0;
    for (int i = 0; i < tries; i++) {
      spawned += trySpawnGroup(world, config, tries);
    }
    return spawned;
  }

  private int trySpawnGroup(
      final ServerWorld world, final DogsUnleashedConfig config, final int tries) {
    final ServerPlayerEntity player = world.getRandomAlivePlayer();
    if (player == null) {
      return 0;
    }
    final Random random = world.random;
    final BlockPos anchor = player.getBlockPos();
    if (countWildGroupsNear(world, anchor) >= WILD_GROUP_CAP) {
      return 0;
    }
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
    final List<UnleashedDogBreed> candidates = breedsNativeTo(world.getBiome(surface));
    if (candidates.isEmpty()) {
      return 0;
    }
    final UnleashedDogBreed breed =
        DogSpawnRates.pickByBaseWeight(
            candidates, random.nextInt(DogSpawnRates.totalBaseWeight(candidates)));
    if (!DogSpawnRates.keeps(
        config.spawning().spawnRate(breed.serializedId()), tries, random.nextDouble())) {
      return 0;
    }
    return spawnGroup(world, breed, surface);
  }

  private int spawnGroup(
      final ServerWorld world, final UnleashedDogBreed breed, final BlockPos center) {
    final UnleashedDogBreed.SpawnSettings settings = breed.spawnSettings();
    final Random random = world.random;
    final int groupSize =
        settings.minGroupSize()
            + random.nextInt(settings.maxGroupSize() - settings.minGroupSize() + 1);
    final EntityType<? extends UnleashedDogEntity> type = ModEntities.getDogEntityType(breed);
    int spawned = 0;
    EntityData entityData = null;
    for (int i = 0; i < groupSize; i++) {
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
      dog.getComponents().get(DogSpawnerOrigin.class).setFromSpawner(true);
      entityData =
          dog.initialize(world, world.getLocalDifficulty(pos), SpawnReason.NATURAL, entityData);
      world.spawnEntityAndPassengers(dog);
      spawned++;
    }
    return spawned;
  }

  public static List<UnleashedDogBreed> breedsNativeTo(final RegistryEntry<Biome> biome) {
    final List<UnleashedDogBreed> matching = new ArrayList<>();
    for (final UnleashedDogBreed breed : NATURAL_BREEDS) {
      for (final RegistryKey<Biome> key : breed.spawnSettings().biomes()) {
        if (biome.matchesKey(key)) {
          matching.add(breed);
          break;
        }
      }
    }
    return matching;
  }

  public static int countWildGroupsNear(final ServerWorld world, final BlockPos center) {
    final Box searchBox = new Box(center).expand(WILD_GROUP_CAP_RADIUS_BLOCKS);
    final List<UnleashedDogEntity> wild =
        world.getEntitiesByClass(
            UnleashedDogEntity.class, searchBox, dog -> !dog.isSpectator() && !dog.isTamed());
    final Set<UUID> groups = new HashSet<>();
    int ungrouped = 0;
    for (final UnleashedDogEntity dog : wild) {
      if (dog.getPack().id() == null) {
        ungrouped++;
      } else {
        groups.add(dog.getPack().id());
      }
    }
    return groups.size() + ungrouped;
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
            random.nextInt(GROUP_SCATTER_RADIUS_BLOCKS * 2 + 1) - GROUP_SCATTER_RADIUS_BLOCKS,
            0,
            random.nextInt(GROUP_SCATTER_RADIUS_BLOCKS * 2 + 1) - GROUP_SCATTER_RADIUS_BLOCKS);
    return world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, offset);
  }
}
