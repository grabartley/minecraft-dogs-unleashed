package com.grahambartley.dogsunleashed.spawner;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.VillagerType;
import net.minecraft.world.GameRules;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import net.minecraft.world.spawner.SpecialSpawner;

public class StraySpawner implements SpecialSpawner {

  private static final int REGION_LOADED_MARGIN_BLOCKS = 10;

  private int cooldownTicks;

  @Override
  public int spawn(
      final ServerWorld world, final boolean spawnMonsters, final boolean spawnAnimals) {
    if (!StraySpawnRules.canAttempt(
        DogsUnleashed.SERVER_CONFIG.spawning().villageStrays(),
        spawnAnimals,
        world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING))) {
      return 0;
    }
    if (--this.cooldownTicks > 0) {
      return 0;
    }
    this.cooldownTicks = StraySpawnRules.COOLDOWN_TICKS;
    final PlayerEntity player = world.getRandomAlivePlayer();
    if (player == null) {
      return 0;
    }
    final Random random = world.random;
    final BlockPos pos =
        player
            .getBlockPos()
            .add(
                StraySpawnRules.offset(random.nextInt(), random.nextBoolean()),
                0,
                StraySpawnRules.offset(random.nextInt(), random.nextBoolean()));
    if (!world.isRegionLoaded(
        pos.getX() - REGION_LOADED_MARGIN_BLOCKS,
        pos.getZ() - REGION_LOADED_MARGIN_BLOCKS,
        pos.getX() + REGION_LOADED_MARGIN_BLOCKS,
        pos.getZ() + REGION_LOADED_MARGIN_BLOCKS)) {
      return 0;
    }
    return trySpawnAt(world, pos);
  }

  public static int trySpawnAt(final ServerWorld world, final BlockPos pos) {
    if (!DogsUnleashed.SERVER_CONFIG.spawning().villageStrays()) {
      return 0;
    }
    final UnleashedDogBreed breed =
        StraySpawnRules.breedFor(
            Registries.VILLAGER_TYPE.getId(VillagerType.forBiome(world.getBiome(pos))).getPath());
    final EntityType<? extends UnleashedDogEntity> type = ModEntities.getDogEntityType(breed);
    if (!SpawnRestriction.isSpawnPosAllowed(type, world, pos)
        || !StraySpawnRules.isVillage(occupiedHomesNear(world, pos))
        || !StraySpawnRules.hasRoom(straysNear(world, pos))) {
      return 0;
    }
    final UnleashedDogEntity stray = type.create(world);
    if (stray == null) {
      return 0;
    }
    stray.refreshPositionAndAngles(pos, world.random.nextFloat() * 360.0F, 0.0F);
    stray.initialize(world, world.getLocalDifficulty(pos), SpawnReason.NATURAL, null);
    stray.getTrust().markStray();
    world.spawnEntityAndPassengers(stray);
    return 1;
  }

  private static long occupiedHomesNear(final ServerWorld world, final BlockPos pos) {
    return world
        .getPointOfInterestStorage()
        .count(
            entry -> entry.matchesKey(PointOfInterestTypes.HOME),
            pos,
            StraySpawnRules.VILLAGE_RADIUS_BLOCKS,
            PointOfInterestStorage.OccupationStatus.IS_OCCUPIED);
  }

  private static int straysNear(final ServerWorld world, final BlockPos pos) {
    return world
        .getEntitiesByClass(
            UnleashedDogEntity.class,
            new Box(pos)
                .expand(
                    StraySpawnRules.VILLAGE_RADIUS_BLOCKS,
                    StraySpawnRules.VILLAGE_HEIGHT_BLOCKS,
                    StraySpawnRules.VILLAGE_RADIUS_BLOCKS),
            dog -> dog.getTrust().isStray() && !dog.isTamed())
        .size();
  }
}
