package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FleeEntityGoal;

public class PackShelterGoal extends FleeEntityGoal<LivingEntity> {

  private static final float KEEP_AWAY_DISTANCE = 12.0f;
  private static final double WALK_SPEED = 1.0;
  private static final double RUN_SPEED = 1.4;

  private final UnleashedDogEntity dog;

  public PackShelterGoal(final UnleashedDogEntity dog) {
    super(
        dog,
        LivingEntity.class,
        entity -> dog.getPack().isThreat(entity),
        KEEP_AWAY_DISTANCE,
        WALK_SPEED,
        RUN_SPEED,
        entity -> true);
    this.dog = dog;
  }

  @Override
  public boolean canStart() {
    return this.dog.getPack().staysOutOfFights() && super.canStart();
  }

  @Override
  public boolean shouldContinue() {
    return this.dog.getPack().staysOutOfFights() && super.shouldContinue();
  }
}
