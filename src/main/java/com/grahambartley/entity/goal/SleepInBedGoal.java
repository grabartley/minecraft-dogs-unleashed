package com.grahambartley.entity.goal;

import com.grahambartley.ModBlocks;
import com.grahambartley.block.entity.DogBedBlockEntity;
import com.grahambartley.entity.UnleashedDogEntity;
import java.util.EnumSet;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SleepInBedGoal extends Goal {

  private static final int BED_SEARCH_RANGE = 16;
  private static final double CLOSE_ENOUGH_DISTANCE = 2.0;

  private final UnleashedDogEntity dog;
  private BlockPos targetBedPos;
  private int cooldown;

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
    if (this.dog.isSleepingInBed()) {
      return true;
    }

    if (this.cooldown > 0) {
      this.cooldown--;
      return false;
    }

    if (this.dog.hasAssignedBed()) {
      final BlockPos bedPos = this.dog.getAssignedBedPos().get();
      if (this.isValidBed(bedPos)) {
        this.targetBedPos = bedPos;
        return this.shouldSleep();
      }
    }

    this.targetBedPos = this.findNearbyBed();
    if (this.targetBedPos != null) {
      return this.shouldSleep();
    }

    this.cooldown = 100;
    return false;
  }

  private boolean shouldSleep() {
    final World world = this.dog.getWorld();
    return world.isNight() && !world.isRaining();
  }

  private boolean isValidBed(BlockPos pos) {
    final World world = this.dog.getWorld();
    final BlockState state = world.getBlockState(pos);
    return state.isOf(ModBlocks.DOG_BED);
  }

  private BlockPos findNearbyBed() {
    final World world = this.dog.getWorld();
    final BlockPos dogPos = this.dog.getBlockPos();

    for (BlockPos pos :
        BlockPos.iterateOutwards(dogPos, BED_SEARCH_RANGE, BED_SEARCH_RANGE, BED_SEARCH_RANGE)) {
      if (this.isValidBed(pos)) {
        final var blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof DogBedBlockEntity dogBedEntity) {
          if (!dogBedEntity.hasAssignedDog()
              || dogBedEntity.getAssignedDogUuid().equals(this.dog.getUuid())) {
            return pos;
          }
        }
      }
    }
    return null;
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
    return this.shouldSleep() || this.dog.isSleepingInBed();
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
    this.dog.wakeUp();
    this.targetBedPos = null;
    this.dog.getNavigation().stop();
  }

  @Override
  public void tick() {
    if (this.targetBedPos == null) {
      return;
    }

    final double distanceToBed = this.dog.getBlockPos().getSquaredDistance(this.targetBedPos);

    if (distanceToBed <= CLOSE_ENOUGH_DISTANCE * CLOSE_ENOUGH_DISTANCE) {
      if (!this.dog.isSleepingInBed()) {
        this.dog.commandToSleep(this.targetBedPos);
      }
      this.dog.getNavigation().stop();
      this.dog
          .getLookControl()
          .lookAt(
              this.targetBedPos.getX() + 0.5,
              this.targetBedPos.getY(),
              this.targetBedPos.getZ() + 0.5);
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
