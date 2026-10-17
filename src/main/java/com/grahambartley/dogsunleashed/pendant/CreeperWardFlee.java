package com.grahambartley.dogsunleashed.pendant;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.pendant.PendantType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.CreeperEntity;

public final class CreeperWardFlee {

  static final int FLEE_GOAL_PRIORITY = 3;
  static final float FLEE_DISTANCE = 6.0f;
  static final double SLOW_SPEED = 1.0;
  static final double FAST_SPEED = 1.2;

  private CreeperWardFlee() {}

  public static void addTo(final Entity entity) {
    if (entity instanceof CreeperEntity creeper) {
      creeper.goalSelector.add(
          FLEE_GOAL_PRIORITY,
          new FleeEntityGoal<>(
              creeper,
              UnleashedDogEntity.class,
              FLEE_DISTANCE,
              SLOW_SPEED,
              FAST_SPEED,
              CreeperWardFlee::wardsCreepers));
    }
  }

  static boolean wardsCreepers(final LivingEntity entity) {
    return entity instanceof UnleashedDogEntity dog
        && dog.getPendants().isWearing(PendantType.WARDING_CREEPER);
  }
}
