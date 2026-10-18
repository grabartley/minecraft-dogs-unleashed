package com.grahambartley.dogsunleashed.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public final class WildPackRules {

  public static final double DEFENCE_RANGE = 24.0;
  public static final double HUNT_RANGE = 16.0;
  public static final int PEN_RANGE = 4;
  public static final int HUNT_COOLDOWN_TICKS = 2 * 60 * 20;
  public static final int THREAT_MEMORY_TICKS = 30 * 20;
  public static final int PACK_SPREAD_BLOCKS = 3;

  private WildPackRules() {}

  public static List<BlockPos> spreadColumns(final BlockPos leaderPos) {
    final ChunkPos leaderChunk = new ChunkPos(leaderPos);
    final List<BlockPos> columns = new ArrayList<>();
    for (int dx = -PACK_SPREAD_BLOCKS; dx <= PACK_SPREAD_BLOCKS; dx++) {
      for (int dz = -PACK_SPREAD_BLOCKS; dz <= PACK_SPREAD_BLOCKS; dz++) {
        final BlockPos column = leaderPos.add(dx, 0, dz);
        if ((dx != 0 || dz != 0) && new ChunkPos(column).equals(leaderChunk)) {
          columns.add(column);
        }
      }
    }
    return columns;
  }

  public static boolean hasRoomForPack(final int freeSpots, final int smallestPack) {
    return freeSpots >= smallestPack - 1;
  }

  public static int membersToPlace(final int packSize, final int freeSpots) {
    return Math.max(0, Math.min(packSize - 1, freeSpots));
  }

  public static boolean isGrownStage(final DireWolfGrowthStage stage) {
    return stage.number() >= DireWolfGrowthStage.ADULT.number();
  }

  public static boolean isPreyType(final Class<?> type) {
    return !DogFriendlyTargetPolicy.isFriendlyClass(type)
        && (AnimalEntity.class.isAssignableFrom(type)
            || AbstractSkeletonEntity.class.isAssignableFrom(type)
            || ZombieEntity.class.isAssignableFrom(type));
  }

  public static boolean huntCooldownOver(final long now, final long nextHuntTime) {
    return now >= nextHuntTime;
  }

  public static boolean remembersThreat(final long now, final long threatUntil) {
    return now < threatUntil;
  }
}
