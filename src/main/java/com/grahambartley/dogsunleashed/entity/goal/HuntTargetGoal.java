package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogFriendlyTargetPolicy;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;

/**
 * While commanded to Hunt, the dog autonomously targets hostile mobs and unnamed wild animals
 * nearby. Anything name-tagged, tamed, or dog-friendly (other dogs, villagers, iron golems) is
 * never prey.
 */
public class HuntTargetGoal extends ActiveTargetGoal<LivingEntity> {

  private static final int TARGET_CHANCE = 10;
  private static final double HUNT_RADIUS = 16.0;

  private final UnleashedDogEntity dog;

  public HuntTargetGoal(final UnleashedDogEntity dog) {
    super(dog, LivingEntity.class, TARGET_CHANCE, true, false, HuntTargetGoal::isHuntableTarget);
    this.dog = dog;
  }

  @Override
  public boolean canStart() {
    return this.dog.getCommand() == DogCommand.HUNT && !this.dog.isBaby() && super.canStart();
  }

  @Override
  public boolean shouldContinue() {
    return this.dog.getCommand() == DogCommand.HUNT && super.shouldContinue();
  }

  @Override
  protected double getFollowRange() {
    return HUNT_RADIUS;
  }

  static boolean isHuntableTarget(final LivingEntity target) {
    if (target.hasCustomName()) {
      return false;
    }
    if (DogFriendlyTargetPolicy.isFriendlyTarget(target)) {
      return false;
    }
    if (target instanceof TameableEntity tameable && tameable.isTamed()) {
      return false;
    }
    if (target instanceof AbstractHorseEntity horse && horse.isTame()) {
      return false;
    }
    if (target instanceof HostileEntity) {
      return true;
    }
    return target instanceof AnimalEntity;
  }
}
