package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class PackGrudgeGoal extends OwnTargetGoal<PlayerEntity> {

  private static final int TARGET_CHANCE = 5;

  private final UnleashedDogEntity dog;

  public PackGrudgeGoal(final UnleashedDogEntity dog) {
    super(dog, PlayerEntity.class, TARGET_CHANCE, target -> dog.getPack().huntsDown(target));
    this.dog = dog;
  }

  @Override
  protected boolean stillWants(final LivingEntity target) {
    return this.dog.getPack().huntsDown(target);
  }
}
