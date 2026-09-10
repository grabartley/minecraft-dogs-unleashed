package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;

public class GuardTargetGoal extends ActiveTargetGoal<HostileEntity> {

  private static final int TARGET_CHANCE = 10;
  private static final double GUARD_RADIUS = 12.0;
  private static final double SCAN_RANGE = 24.0;
  private static final double POSITION_CENTER_OFFSET = 0.5;

  private final UnleashedDogEntity dog;

  public GuardTargetGoal(final UnleashedDogEntity dog) {
    super(
        dog,
        HostileEntity.class,
        TARGET_CHANCE,
        true,
        false,
        target -> isThreatNearAnchor(dog, target));
    this.dog = dog;
  }

  @Override
  public boolean canStart() {
    return this.dog.getCommand() == DogCommand.GUARD
        && !this.dog.isBaby()
        && this.dog.getCommandController().getAnchorPos() != null
        && super.canStart();
  }

  @Override
  public boolean shouldContinue() {
    return this.dog.getCommand() == DogCommand.GUARD && super.shouldContinue();
  }

  @Override
  protected double getFollowRange() {
    return SCAN_RANGE;
  }

  static boolean isThreatNearAnchor(final UnleashedDogEntity dog, final LivingEntity target) {
    if (target.hasCustomName()) {
      return false;
    }
    final BlockPos anchor = dog.getCommandController().getAnchorPos();
    return anchor != null
        && target.squaredDistanceTo(
                anchor.getX() + POSITION_CENTER_OFFSET,
                anchor.getY() + POSITION_CENTER_OFFSET,
                anchor.getZ() + POSITION_CENTER_OFFSET)
            <= GUARD_RADIUS * GUARD_RADIUS;
  }
}
