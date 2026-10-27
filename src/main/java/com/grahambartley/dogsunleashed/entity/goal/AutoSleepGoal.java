package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModBlockTags;
import com.grahambartley.dogsunleashed.entity.DogDeathWatch;
import com.grahambartley.dogsunleashed.entity.DogSleepApproach;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.EnumSet;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AutoSleepGoal extends Goal {

  private final UnleashedDogEntity dog;
  private BlockPos targetBedPos;

  public AutoSleepGoal(UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (!DogsUnleashed.SERVER_CONFIG.sleep().autoSleepEnabled()) {
      return false;
    }
    if (!this.dog.isTamed()) {
      return false;
    }
    if (this.dog.isLeashed()) {
      return false;
    }
    if (this.dog.isInSittingPose()) {
      return false;
    }
    if (this.dog.getComponents().get(DogDeathWatch.class).isKeepingWatch()) {
      return false;
    }
    if (this.dog.getSleepController().isCommandedToSleep()) {
      return false;
    }
    if (this.dog.getSleepController().isSleepingInBed()) {
      return false;
    }
    if (this.dog.getSleepController().isAutoSleepSuppressed()) {
      return false;
    }
    if (!this.dog.getSleepController().getAssignedBedPos().isPresent()) {
      return false;
    }

    if (!this.dog.getSleepController().canSleepNow()) {
      return false;
    }

    this.targetBedPos = this.dog.getSleepController().getAssignedBedPos().get();

    if (!this.isValidBed(this.targetBedPos)) {
      return false;
    }

    if (!this.isWithinRange(this.targetBedPos)) {
      return false;
    }

    return true;
  }

  private boolean isValidBed(BlockPos pos) {
    final World world = this.dog.getWorld();
    final BlockState state = world.getBlockState(pos);
    return state.isIn(ModBlockTags.DOG_BEDS)
        && !this.dog.getSleepController().releaseSpotIfTaken(pos);
  }

  private boolean isWithinRange(BlockPos bedPos) {
    final double distance = this.dog.getBlockPos().getSquaredDistance(bedPos);
    final double range = DogsUnleashed.SERVER_CONFIG.sleep().autoSleepRangeBlocks();
    return distance <= range * range;
  }

  @Override
  public boolean shouldContinue() {
    if (!this.dog.isTamed()) {
      return false;
    }
    if (this.dog.isLeashed()) {
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

    if (this.dog.getSleepController().isSleepingInBed()) {
      return this.dog.getSleepController().canSleepNow();
    }

    return !this.dog.getSleepController().isAutoSleepSuppressed()
        && this.dog.getSleepController().canSleepNow();
  }

  @Override
  public void start() {
    if (this.targetBedPos != null) {
      DogSleepApproach.walkTo(this.dog.getWorld(), this.dog.getNavigation(), this.targetBedPos);
    }
  }

  @Override
  public void stop() {
    if (this.dog.getSleepController().isSleepingInBed()) {
      this.dog.wakeUp();
    }
    this.targetBedPos = null;
    this.dog.getNavigation().stop();
  }

  @Override
  public void tick() {
    if (this.targetBedPos == null) {
      return;
    }

    if (this.dog.getSleepController().isSleepingInBed()) {
      this.dog.getSleepController().holdSleepPose(this.targetBedPos);
      return;
    }

    DogSleepApproach.step(this.dog, this.targetBedPos);
  }

  @Override
  public boolean canStop() {
    return !this.dog.getSleepController().isSleepingInBed();
  }
}
