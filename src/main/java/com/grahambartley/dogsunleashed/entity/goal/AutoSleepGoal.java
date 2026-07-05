package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.EnumSet;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AutoSleepGoal extends Goal {

  private static final double CLOSE_ENOUGH_DISTANCE = 2.0;

  private static final long DAY_LENGTH_TICKS = 24000;
  private static final long MINECRAFT_HOUR_TICKS = 1000;
  private static final long NIGHT_START_TICK = 13000;
  private static final long SUNRISE_TICK = 23000;
  // Puppies turn in two hours before adults and sleep in one hour past sunrise. Their wake time
  // lands on the 24000 day rollover, so the window is one contiguous block up to the new day.
  private static final long PUPPY_SLEEP_START_TICK = NIGHT_START_TICK - 2 * MINECRAFT_HOUR_TICKS;
  private static final long PUPPY_SLEEP_END_TICK = SUNRISE_TICK + MINECRAFT_HOUR_TICKS;

  private final UnleashedDogEntity dog;
  private BlockPos targetBedPos;

  public AutoSleepGoal(UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (!DogsUnleashed.SERVER_CONFIG.autoSleepEnabled()) {
      return false;
    }
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
    if (this.dog.isAutoSleepSuppressed()) {
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
    if (world.isRaining() || world.isThundering()) {
      return false;
    }
    return this.isWithinSleepWindow(world.getTimeOfDay() % DAY_LENGTH_TICKS);
  }

  private boolean shouldWake(World world) {
    if (world.isRaining() || world.isThundering()) {
      return true;
    }
    return !this.isWithinSleepWindow(world.getTimeOfDay() % DAY_LENGTH_TICKS);
  }

  private boolean isWithinSleepWindow(final long timeOfDay) {
    if (this.dog.isBaby()) {
      return isWithinWindow(timeOfDay, PUPPY_SLEEP_START_TICK, PUPPY_SLEEP_END_TICK);
    }
    return isWithinWindow(timeOfDay, NIGHT_START_TICK, SUNRISE_TICK);
  }

  /** Half-open window {@code [start, end)} on the 24000-tick clock, handling midnight wrap. */
  private static boolean isWithinWindow(final long timeOfDay, final long start, final long end) {
    return start <= end
        ? timeOfDay >= start && timeOfDay < end
        : timeOfDay >= start || timeOfDay < end;
  }

  private boolean isValidBed(BlockPos pos) {
    final World world = this.dog.getWorld();
    final BlockState state = world.getBlockState(pos);
    return state.isOf(ModBlocks.DOG_BED);
  }

  private boolean isWithinRange(BlockPos bedPos) {
    final double distance = this.dog.getBlockPos().getSquaredDistance(bedPos);
    final double range = DogsUnleashed.SERVER_CONFIG.autoSleepRangeBlocks();
    return distance <= range * range;
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

    if (this.dog.isAutoSleepSuppressed()) {
      this.dog.getNavigation().stop();
      return;
    }

    if (this.dog.isSleepingInBed()) {
      this.dog.refreshPositionAndAngles(
          this.targetBedPos.getX() + 0.5,
          this.targetBedPos.getY() + 0.1,
          this.targetBedPos.getZ() + 0.5,
          this.dog.getYaw(),
          this.dog.getPitch());
      this.dog.setVelocity(0, 0, 0);
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
