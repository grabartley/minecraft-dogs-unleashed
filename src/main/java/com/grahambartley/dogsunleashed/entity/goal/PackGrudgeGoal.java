package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.player.PlayerEntity;

public class PackGrudgeGoal extends ActiveTargetGoal<PlayerEntity> {

  private static final int TARGET_CHANCE = 5;

  private final UnleashedDogEntity dog;

  public PackGrudgeGoal(final UnleashedDogEntity dog) {
    super(
        dog,
        PlayerEntity.class,
        TARGET_CHANCE,
        true,
        false,
        target -> dog.getPack().huntsDown(target));
    this.dog = dog;
  }

  @Override
  public boolean shouldContinue() {
    return this.dog.getPack().huntsDown(this.dog.getTarget()) && super.shouldContinue();
  }
}
