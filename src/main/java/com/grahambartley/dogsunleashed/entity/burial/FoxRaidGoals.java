package com.grahambartley.dogsunleashed.entity.burial;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.goal.FoxRaidCacheGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.passive.FoxEntity;

public final class FoxRaidGoals {

  static final int FLEE_GOAL_PRIORITY = 4;
  static final int RAID_GOAL_PRIORITY = 6;
  static final float FLEE_DISTANCE = 8.0F;
  static final double FLEE_FAR_SPEED = 1.6;
  static final double FLEE_NEAR_SPEED = 1.4;

  private FoxRaidGoals() {}

  public static void addTo(final FoxEntity fox) {
    fox.goalSelector.add(
        FLEE_GOAL_PRIORITY,
        new FleeEntityGoal<>(
            fox,
            UnleashedDogEntity.class,
            FLEE_DISTANCE,
            FLEE_FAR_SPEED,
            FLEE_NEAR_SPEED,
            dog -> ((UnleashedDogEntity) dog).isTamed() && BoneThieves.isThief(fox)));
    fox.goalSelector.add(RAID_GOAL_PRIORITY, new FoxRaidCacheGoal(fox));
  }
}
