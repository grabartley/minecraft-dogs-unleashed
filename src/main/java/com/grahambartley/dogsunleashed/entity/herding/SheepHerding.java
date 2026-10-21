package com.grahambartley.dogsunleashed.entity.herding;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.passive.SheepEntity;

public final class SheepHerding {

  static final int FLEE_GOAL_PRIORITY = 1;
  static final float FLEE_DISTANCE = 6.0f;
  static final double SLOW_SPEED = 1.0;
  static final double FAST_SPEED = 1.25;

  private SheepHerding() {}

  public static void addTo(final SheepEntity sheep) {
    sheep.goalSelector.add(
        FLEE_GOAL_PRIORITY,
        new FleeEntityGoal<>(
            sheep,
            UnleashedDogEntity.class,
            FLEE_DISTANCE,
            SLOW_SPEED,
            FAST_SPEED,
            SheepHerding::herdsSheep));
  }

  static boolean herdsSheep(final LivingEntity entity) {
    return entity instanceof UnleashedDogEntity dog && herds(dog.getBreed());
  }

  public static boolean herds(final UnleashedDogBreed breed) {
    return breed.nature().herdsSheep();
  }
}
