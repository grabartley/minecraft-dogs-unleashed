package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.ModBlockTags;
import com.grahambartley.dogsunleashed.entity.DogSleepApproach;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.EnumSet;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SleepInBedGoal extends Goal {

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
    if (!this.dog.getSleepController().isSleepingInBed()
        && !this.dog.getSleepController().isCommandedToSleep()) {
      return false;
    }
    if (!this.dog.getSleepController().getAssignedBedPos().isPresent()) {
      return false;
    }
    this.targetBedPos = this.dog.getSleepController().getAssignedBedPos().get();
    return this.isValidBed(this.targetBedPos);
  }

  private boolean isValidBed(BlockPos pos) {
    final World world = this.dog.getWorld();
    final BlockState state = world.getBlockState(pos);
    return state.isIn(ModBlockTags.DOG_BEDS)
        && !this.dog.getSleepController().releaseSpotIfTaken(pos);
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

    if (!this.dog.getSleepController().canSleepNow()) {
      return false;
    }
    return this.dog.getSleepController().isSleepingInBed()
        || this.dog.getSleepController().isCommandedToSleep();
  }

  @Override
  public void start() {
    if (this.targetBedPos != null) {
      DogSleepApproach.walkTo(this.dog.getWorld(), this.dog.getNavigation(), this.targetBedPos);
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

    if (this.dog.getSleepController().isSleepingInBed()) {
      this.dog.getSleepController().holdSleepPose(this.targetBedPos);
      return;
    }

    if (DogSleepApproach.hasArrived(this.dog.getWorld(), this.dog.getPos(), this.targetBedPos)) {
      this.dog.getSleepController().startSleepingInBed(this.targetBedPos);
    } else {
      DogSleepApproach.walkTo(this.dog.getWorld(), this.dog.getNavigation(), this.targetBedPos);
    }
  }

  @Override
  public boolean canStop() {
    return !this.dog.getSleepController().isSleepingInBed()
        && !this.dog.getSleepController().isCommandedToSleep();
  }
}
