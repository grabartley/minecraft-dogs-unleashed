package com.grahambartley.dogsunleashed.entity;

import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.AnimalEntity;

public final class WildPackRules {

  public static final double DEFENCE_RANGE = 24.0;
  public static final double HUNT_RANGE = 16.0;
  public static final int PEN_RANGE = 4;
  public static final int HUNT_COOLDOWN_TICKS = 2 * 60 * 20;
  public static final int THREAT_MEMORY_TICKS = 30 * 20;

  private WildPackRules() {}

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
