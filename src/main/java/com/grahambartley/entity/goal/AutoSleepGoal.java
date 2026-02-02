package com.grahambartley.entity.goal;

import com.grahambartley.ModBlocks;
import com.grahambartley.entity.UnleashedDogEntity;
import java.util.EnumSet;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AutoSleepGoal extends Goal {

  private static final double BED_RANGE = 32.0;
  private static final double CLOSE_ENOUGH_DISTANCE = 2.0;

  private final UnleashedDogEntity dog;
  private BlockPos targetBedPos;

  public AutoSleepGoal(UnleashedDogEntity dog) {
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
    if (this.dog.isCommandedToSleep()) {
      return false;
    }
    if (this.dog.isSleepingInBed()) {
      return false;
    }
    if (!this.dog.hasAssignedBed()) {
      return false;
    }

    final World world = this.dog.getWorld();
    if (!this.shouldSleep(world)) {
      return false;
    }

    this.targetBedPos = this.dog.getAssignedBedPos().get();

    if (!this.isValidBed(this.targetBedPos)) {
      return false;
    }

    if (!this.isWithinRange(this.targetBedPos)) {
      return false;
    }

    return true;
  }

  private boolean shouldSleep(World world) {
    final long timeOfDay = world.getTimeOfDay() % 24000;
    final boolean isNightTime = timeOfDay >= 13000 && timeOfDay < 23000;
    final boolean isRaining = world.isRaining();
    final boolean isThundering = world.isThundering();

    return isNightTime && !isRaining && !isThundering;
  }

  private boolean shouldWake(World world) {
    final long timeOfDay = world.getTimeOfDay() % 24000;
    final boolean isDayTime = timeOfDay < 13000 || timeOfDay >= 23000;
    final boolean isRaining = world.isRaining();
    final boolean isThundering = world.isThundering();

    return isDayTime || isRaining || isThundering;
  }

  private boolean isValidBed(BlockPos pos) {
    final World world = this.dog.getWorld();
    final BlockState state = world.getBlockState(pos);
    return state.isOf(ModBlocks.DOG_BED);
  }

  private boolean isWithinRange(BlockPos bedPos) {
    final double distance = this.dog.getBlockPos().getSquaredDistance(bedPos);
    return distance <= BED_RANGE * BED_RANGE;
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

    final World world = this.dog.getWorld();

    if (this.dog.isSleepingInBed()) {
      return !this.shouldWake(world);
    }

    return this.shouldSleep(world);
  }

  @Override
  public void start() {
    if (this.targetBedPos != null) {
      this.dog
          .getNavigation()
          .startMovingTo(
              this.targetBedPos.getX() + 0.5,
              this.targetBedPos.getY(),
              this.targetBedPos.getZ() + 0.5,
              1.0);
    }
  }

  @Override
  public void stop() {
    if (this.dog.isSleepingInBed()) {
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
              this.targetBedPos.getX() + 0.5,
              this.targetBedPos.getY(),
              this.targetBedPos.getZ() + 0.5,
              1.0);
    }
  }

  @Override
  public boolean canStop() {
    return !this.dog.isSleepingInBed();
  }
}
