package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.advancement.DogSleptInBedCriterion;
import com.grahambartley.dogsunleashed.block.DogSleepPose;
import com.grahambartley.dogsunleashed.block.DogSleepSpotAnchor;
import com.grahambartley.dogsunleashed.block.DogSleepSpotHandover;
import com.grahambartley.dogsunleashed.block.entity.AssignedDogHolder;
import com.grahambartley.dogsunleashed.entity.goal.DogSleepWindow;
import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class DogSleepController {

  public static final String NOT_SLEEPY_MESSAGE_KEY = "message.dogs-unleashed.not_sleepy";

  private static final long NOT_SUPPRESSED = Long.MIN_VALUE;
  private static final double POSITION_CENTER_OFFSET = 0.5;
  private static final double BED_APPROACH_SPEED = 1.0;

  private final UnleashedDogEntity dog;

  private long autoSleepSuppressedFrom = NOT_SUPPRESSED;
  private long autoSleepSuppressedUntil = NOT_SUPPRESSED;

  DogSleepController(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public void clearAssignedBed() {
    final Optional<BlockPos> leaving = this.getAssignedBedPos();
    this.setAssignedBedPos(null);
    this.wakeFrom(leaving);
  }

  public boolean holdsSpot(final BlockPos spotPos) {
    if (this.dog.getWorld().getBlockEntity(spotPos) instanceof AssignedDogHolder holder
        && DogSleepSpotHandover.isTakenByAnother(holder.getAssignedDogUuid(), this.dog.getUuid())) {
      this.clearAssignedBed();
      return false;
    }
    return true;
  }

  public boolean canSleepNow() {
    final World world = this.dog.getWorld();
    return DogSleepWindow.isSleepTime(world.getTimeOfDay(), this.dog.isBaby(), this.dog.isUndead())
        && !DogSleepWindow.isWokenByWeather(world, this.dog.isUndead());
  }

  public boolean commandToSleep(final BlockPos bedPos) {
    if (!this.dog.isTamed() || !this.canSleepNow()) {
      return false;
    }
    this.setAssignedBedPos(bedPos);
    this.dog.getCommandController().demoteSitToFollow();
    this.autoSleepSuppressedUntil = NOT_SUPPRESSED;
    this.dog.getDataTracker().set(UnleashedDogEntity.COMMANDED_TO_SLEEP, true);
    this.dog
        .getNavigation()
        .startMovingTo(
            bedPos.getX() + POSITION_CENTER_OFFSET,
            bedPos.getY(),
            bedPos.getZ() + POSITION_CENTER_OFFSET,
            BED_APPROACH_SPEED);
    return true;
  }

  public void markManuallyWoken() {
    final long now = this.dog.getWorld().getTimeOfDay();
    this.autoSleepSuppressedFrom = now;
    this.autoSleepSuppressedUntil =
        DogSleepWindow.currentSleepWindowEnd(now, this.dog.isBaby(), this.dog.isUndead())
            .orElse(NOT_SUPPRESSED);
  }

  public boolean isAutoSleepSuppressed() {
    final long now = this.dog.getWorld().getTimeOfDay();
    return now >= this.autoSleepSuppressedFrom && now < this.autoSleepSuppressedUntil;
  }

  public void startSleepingInBed(final BlockPos bedPos) {
    this.dog.getDataTracker().set(UnleashedDogEntity.SLEEPING_IN_BED, true);
    this.dog.getDataTracker().set(UnleashedDogEntity.COMMANDED_TO_SLEEP, false);
    holdSleepPose(bedPos);
    this.dog.setNoGravity(true);
    this.dog.getNavigation().stop();

    if (this.dog.getOwner() instanceof ServerPlayerEntity player) {
      DogSleptInBedCriterion.INSTANCE.trigger(player);
    }
  }

  public void holdSleepPose(final BlockPos bedPos) {
    final DogSleepPose pose =
        DogSleepSpotAnchor.poseFor(this.dog.getWorld(), bedPos, this.dog.getYaw());
    this.dog.refreshPositionAndAngles(
        pose.position().x, pose.position().y, pose.position().z, pose.yaw(), this.dog.getPitch());
    this.dog.setBodyYaw(pose.yaw());
    this.dog.setHeadYaw(pose.yaw());
    this.dog.setVelocity(0, 0, 0);
  }

  void wakeUp() {
    final Optional<BlockPos> bedPos = this.getAssignedBedPos();
    if (this.isSleepingOnServer()) {
      bedPos.ifPresent(this::awardHouseComfort);
    }
    this.wakeFrom(bedPos);
  }

  private void wakeFrom(final Optional<BlockPos> bedPos) {
    if (this.isSleepingOnServer()) {
      bedPos.ifPresent(this::stepOutOf);
    }
    this.dog.setNoGravity(false);
    this.dog.getDataTracker().set(UnleashedDogEntity.SLEEPING_IN_BED, false);
    this.dog.getDataTracker().set(UnleashedDogEntity.COMMANDED_TO_SLEEP, false);
    this.dog.getAmbienceEffects().releaseBirthWakeHearts();
  }

  private boolean isSleepingOnServer() {
    return this.isSleepingInBed() && !this.dog.getWorld().isClient;
  }

  private void stepOutOf(final BlockPos bedPos) {
    final World world = this.dog.getWorld();
    DogStandingSpot.firstSafe(world, this.dog, DogSleepSpotAnchor.wakeCellsFor(world, bedPos))
        .ifPresent(this.dog::refreshPositionAfterTeleport);
  }

  private void awardHouseComfort(final BlockPos bedPos) {
    if (this.dog.getWorld().getBlockState(bedPos).isOf(ModBlocks.DOG_HOUSE)) {
      this.dog.addStatusEffect(DogHouseComfort.wakeEffect());
    }
  }

  void writeNbt(final NbtCompound nbt) {
    nbt.putBoolean(ModNbtKeys.SLEEPING_IN_BED, this.isSleepingInBed());
    this.getAssignedBedPos()
        .ifPresent(
            pos -> {
              nbt.putInt(ModNbtKeys.BED_POS_X, pos.getX());
              nbt.putInt(ModNbtKeys.BED_POS_Y, pos.getY());
              nbt.putInt(ModNbtKeys.BED_POS_Z, pos.getZ());
            });
  }

  void readNbt(final NbtCompound nbt) {
    if (nbt.contains(ModNbtKeys.SLEEPING_IN_BED)) {
      this.dog
          .getDataTracker()
          .set(UnleashedDogEntity.SLEEPING_IN_BED, nbt.getBoolean(ModNbtKeys.SLEEPING_IN_BED));
    }
    if (nbt.contains(ModNbtKeys.BED_POS_X)
        && nbt.contains(ModNbtKeys.BED_POS_Y)
        && nbt.contains(ModNbtKeys.BED_POS_Z)) {
      this.setAssignedBedPos(
          new BlockPos(
              nbt.getInt(ModNbtKeys.BED_POS_X),
              nbt.getInt(ModNbtKeys.BED_POS_Y),
              nbt.getInt(ModNbtKeys.BED_POS_Z)));
    }
  }

  public boolean isSleepingInBed() {
    return this.dog.getDataTracker().get(UnleashedDogEntity.SLEEPING_IN_BED);
  }

  public Optional<BlockPos> getAssignedBedPos() {
    return this.dog.getDataTracker().get(UnleashedDogEntity.ASSIGNED_BED_POS);
  }

  public void setAssignedBedPos(final @Nullable BlockPos pos) {
    this.dog.getDataTracker().set(UnleashedDogEntity.ASSIGNED_BED_POS, Optional.ofNullable(pos));
  }

  public boolean isCommandedToSleep() {
    return this.dog.getDataTracker().get(UnleashedDogEntity.COMMANDED_TO_SLEEP);
  }
}
