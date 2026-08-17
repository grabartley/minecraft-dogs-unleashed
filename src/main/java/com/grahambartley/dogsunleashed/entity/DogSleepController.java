package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.advancement.DogSleptInBedCriterion;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public final class DogSleepController {

  static final long DAY_LENGTH_TICKS = 24000;
  static final long NIGHT_START_TICK = 13000;

  private static final double POSITION_CENTER_OFFSET = 0.5;
  private static final double SLEEP_POSITION_Y_OFFSET = 0.1;
  private static final double BED_APPROACH_SPEED = 1.0;

  private final UnleashedDogEntity dog;

  private int manuallyWokenAge = -1;
  private boolean manuallyWokenAtNight = false;

  DogSleepController(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public static long timeOfDay(final long worldTime) {
    return worldTime % DAY_LENGTH_TICKS;
  }

  public static boolean isNight(final long timeOfDay) {
    return timeOfDay >= NIGHT_START_TICK;
  }

  public static boolean shouldKeepAutoSleepSuppressed(
      final int elapsed, final long currentTimeOfDay) {
    if (elapsed >= DAY_LENGTH_TICKS) {
      return false;
    }
    return isNight(currentTimeOfDay);
  }

  void clearAssignedBed() {
    this.dog.setAssignedBedPos(null);
    this.wakeUp();
  }

  void commandToSleep(final BlockPos bedPos) {
    if (!this.dog.isTamed()) {
      return;
    }
    this.dog.setAssignedBedPos(bedPos);
    this.dog.demoteSitToFollow();
    this.manuallyWokenAge = -1;
    this.manuallyWokenAtNight = false;
    this.dog.setCommandedToSleep(true);
    this.dog
        .getNavigation()
        .startMovingTo(
            bedPos.getX() + POSITION_CENTER_OFFSET,
            bedPos.getY(),
            bedPos.getZ() + POSITION_CENTER_OFFSET,
            BED_APPROACH_SPEED);
  }

  void markManuallyWoken() {
    this.manuallyWokenAge = this.dog.age;
    this.manuallyWokenAtNight = isNight(timeOfDay(this.dog.getWorld().getTimeOfDay()));
  }

  boolean isAutoSleepSuppressed() {
    if (!this.manuallyWokenAtNight) {
      return false;
    }

    final int elapsed = this.dog.age - this.manuallyWokenAge;
    final long currentTimeOfDay = timeOfDay(this.dog.getWorld().getTimeOfDay());

    if (!shouldKeepAutoSleepSuppressed(elapsed, currentTimeOfDay)) {
      this.manuallyWokenAtNight = false;
      this.manuallyWokenAge = -1;
      return false;
    }

    return true;
  }

  void startSleepingInBed(final BlockPos bedPos) {
    this.dog.setSleepingInBed(true);
    this.dog.setCommandedToSleep(false);
    this.dog.refreshPositionAndAngles(
        bedPos.getX() + POSITION_CENTER_OFFSET,
        bedPos.getY() + SLEEP_POSITION_Y_OFFSET,
        bedPos.getZ() + POSITION_CENTER_OFFSET,
        this.dog.getYaw(),
        this.dog.getPitch());
    this.dog.setVelocity(0, 0, 0);
    this.dog.setNoGravity(true);
    this.dog.getNavigation().stop();

    if (this.dog.getOwner() instanceof ServerPlayerEntity player) {
      DogSleptInBedCriterion.INSTANCE.trigger(player);
    }
  }

  void wakeUp() {
    this.dog.setNoGravity(false);
    this.dog.setSleepingInBed(false);
    this.dog.setCommandedToSleep(false);
    this.dog.releaseBirthWakeHearts();
  }

  void writeNbt(final NbtCompound nbt) {
    nbt.putBoolean(ModNbtKeys.SLEEPING_IN_BED, this.dog.isSleepingInBed());
    this.dog
        .getAssignedBedPos()
        .ifPresent(
            pos -> {
              nbt.putInt(ModNbtKeys.BED_POS_X, pos.getX());
              nbt.putInt(ModNbtKeys.BED_POS_Y, pos.getY());
              nbt.putInt(ModNbtKeys.BED_POS_Z, pos.getZ());
            });
  }

  void readNbt(final NbtCompound nbt) {
    if (nbt.contains(ModNbtKeys.SLEEPING_IN_BED)) {
      this.dog.setSleepingInBed(nbt.getBoolean(ModNbtKeys.SLEEPING_IN_BED));
    }
    if (nbt.contains(ModNbtKeys.BED_POS_X)
        && nbt.contains(ModNbtKeys.BED_POS_Y)
        && nbt.contains(ModNbtKeys.BED_POS_Z)) {
      this.dog.setAssignedBedPos(
          new BlockPos(
              nbt.getInt(ModNbtKeys.BED_POS_X),
              nbt.getInt(ModNbtKeys.BED_POS_Y),
              nbt.getInt(ModNbtKeys.BED_POS_Z)));
    }
  }
}
