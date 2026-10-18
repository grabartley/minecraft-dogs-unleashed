package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.config.DireWolfSettings;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;

public final class WildPackFounding {

  private WildPackFounding() {}

  public static boolean hasRoomForPack(
      final UnleashedDogBreed breed,
      final EntityType<? extends UnleashedDogEntity> type,
      final ServerWorldAccess world,
      final SpawnReason reason,
      final BlockPos leaderPos) {
    if (!breed.nature().livesInPacks() || !DogPack.isWildSpawn(reason)) {
      return true;
    }
    return WildPackRules.hasRoomForPack(
        freeSpots(type, world, reason, leaderPos).size(), settings().smallestPack());
  }

  static void bringPack(
      final ServerWorldAccess world,
      final UnleashedDogEntity leader,
      final SpawnReason reason,
      final @Nullable EntityData leaderData,
      final @Nullable EntityData packData) {
    if (!leader.getBreed().nature().livesInPacks()
        || leaderData instanceof WildPackData
        || !(packData instanceof WildPackData)) {
      return;
    }
    final EntityType<? extends UnleashedDogEntity> type =
        ModEntities.getDogEntityType(leader.getBreed());
    final Random random = leader.getRandom();
    final List<BlockPos> spots = freeSpots(type, world, reason, leader.getBlockPos());
    Util.shuffle(spots, random);
    final DireWolfSettings settings = settings();
    final int packSize =
        settings.smallestPack()
            + random.nextInt(settings.largestPack() - settings.smallestPack() + 1);
    final int members = WildPackRules.membersToPlace(packSize, spots.size());
    for (int i = 0; i < members; i++) {
      final UnleashedDogEntity member = type.create(world.toServerWorld());
      if (member == null) {
        return;
      }
      final BlockPos spot = spots.get(i);
      member.refreshPositionAndAngles(spot, random.nextFloat() * 360.0F, 0.0F);
      member.setSpawnedByDogSpawner(leader.isSpawnedByDogSpawner());
      member.initialize(world, world.getLocalDifficulty(spot), reason, packData);
      world.spawnEntityAndPassengers(member);
    }
  }

  private static List<BlockPos> freeSpots(
      final EntityType<? extends UnleashedDogEntity> type,
      final ServerWorldAccess world,
      final SpawnReason reason,
      final BlockPos leaderPos) {
    final List<BlockPos> spots = new ArrayList<>();
    for (final BlockPos column : WildPackRules.spreadColumns(leaderPos)) {
      final BlockPos spot = world.getTopPosition(SpawnRestriction.getHeightmapType(type), column);
      if (SpawnRestriction.isSpawnPosAllowed(type, world, spot)
          && UnleashedDogEntity.canSpawn(type, world, reason, spot, world.getRandom())
          && world.isSpaceEmpty(
              type.getSpawnBox(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5))) {
        spots.add(spot);
      }
    }
    return spots;
  }

  private static DireWolfSettings settings() {
    return DogsUnleashed.SERVER_CONFIG.direWolf();
  }
}
