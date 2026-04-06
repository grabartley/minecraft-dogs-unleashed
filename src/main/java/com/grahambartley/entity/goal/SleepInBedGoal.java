package com.grahambartley.entity.goal;

import com.grahambartley.ModBlocks;
import com.grahambartley.entity.UnleashedDogEntity;
import java.util.EnumSet;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SleepInBedGoal extends Goal {
  private static final double BED_POSITION_OFFSET = 0.5;
  private static final double MOVE_SPEED = 1.0;
  private static final double CLOSE_ENOUGH_DISTANCE = 2.0;

  private final UnleashedDogEntity dog;
  private BlockPos targetBedPos;

  public SleepInBedGoal(UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (!this.dog.isTamed()) {
      return false;
    }
    if (this.dog.isInSittingPose()) {
      return false;
    }
    if (!this.dog.isSleepingInBed() && !this.dog.isCommandedToSleep()) {
      return false;
    }
    if (!this.dog.hasAssignedBed()) {
      return false;
    }
    this.targetBedPos = this.dog.getAssignedBedPos().get();
    return this.isValidBed(this.targetBedPos);
  }

  private boolean isValidBed(BlockPos pos) {
    final World world = this.dog.getWorld();
    final BlockState state = world.getBlockState(pos);
    return state.isOf(ModBlocks.DOG_BED);
  }

  @Override
  public boolean shouldContinue() {
    if (!this.dog.isTamed()) {
      return false;
    }
    if (this.dog.isInSittingPose()) {
      return false;
    }
    if (this.targetBedPos == null) {
      return false;
    }
    if (!this.isValidBed(this.targetBedPos)) {
      return false;
    }
    return this.dog.isSleepingInBed() || this.dog.isCommandedToSleep();
  }

  @Override
  public void start() {
    if (this.targetBedPos != null) {
      this.dog
          .getNavigation()
          .startMovingTo(
              this.targetBedPos.getX() + BED_POSITION_OFFSET,
              this.targetBedPos.getY(),
              this.targetBedPos.getZ() + BED_POSITION_OFFSET,
              MOVE_SPEED);
    }
  }

  @Override
  public void stop() {
    this.dog.wakeUp();
    this.targetBedPos = null;
    this.dog.getNavigation().stop();
  }

  @Override
  public void tick() {
    if (this.targetBedPos == null) {
      return;
    }

    if (this.dog.isSleepingInBed()) {
      return;
    }

    final double distanceToBed = this.dog.getBlockPos().getSquaredDistance(this.targetBedPos);

    if (distanceToBed <= CLOSE_ENOUGH_DISTANCE * CLOSE_ENOUGH_DISTANCE) {
      this.dog.startSleepingInBed(this.targetBedPos);
    } else {
      this.dog
          .getNavigation()
          .startMovingTo(
              this.targetBedPos.getX() + BED_POSITION_OFFSET,
              this.targetBedPos.getY(),
              this.targetBedPos.getZ() + BED_POSITION_OFFSET,
              MOVE_SPEED);
    }
  }

  @Override
  public boolean canStop() {
    return !this.dog.isSleepingInBed() && !this.dog.isCommandedToSleep();
  }
}
