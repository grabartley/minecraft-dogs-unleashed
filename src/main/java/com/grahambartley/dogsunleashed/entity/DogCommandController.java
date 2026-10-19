package com.grahambartley.dogsunleashed.entity;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class DogCommandController {

  private final UnleashedDogEntity dog;

  private @Nullable BlockPos anchorPos = null;
  private @Nullable RegistryKey<World> anchorDimension = null;

  DogCommandController(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public @Nullable BlockPos getAnchorPos() {
    return SavedDimension.isHere(this.anchorDimension, this.dog.getWorld().getRegistryKey())
        ? this.anchorPos
        : null;
  }

  public void anchorWhereItArrived() {
    if (this.anchorPos != null) {
      this.anchorAt(this.dog.getBlockPos(), this.dog.getWorld().getRegistryKey());
    }
  }

  @Nullable
  BlockPos getAnchorPosInAnyDimension() {
    return this.anchorPos;
  }

  @Nullable
  RegistryKey<World> getAnchorDimension() {
    return this.anchorDimension;
  }

  public void apply(final DogCommand command) {
    if (!this.dog.isTamed()) {
      return;
    }
    if (this.dog.getSleepController().isSleepingInBed()
        || this.dog.getSleepController().isCommandedToSleep()) {
      this.dog.getSleepController().markManuallyWoken();
      this.dog.wakeUp();
    }
    this.dog.getDataTracker().set(UnleashedDogEntity.COMMAND, command.id());
    if (command.isAnchored()) {
      this.anchorAt(this.dog.getBlockPos(), this.dog.getWorld().getRegistryKey());
    } else {
      this.clearAnchor();
    }
    this.dog.setSitting(command == DogCommand.SIT);
    this.dog.stopJumping();
    this.dog.getNavigation().stop();
    this.dog.setTarget(null);
  }

  public void acknowledge() {
    this.dog.getAmbienceEffects().startTailWag();
    this.dog.getVocalization().barkIfReady(this.dog.getVocalization().getBarkPitch());
  }

  void demoteSitToFollow() {
    if (this.dog.getCommand() == DogCommand.SIT) {
      this.dog.getDataTracker().set(UnleashedDogEntity.COMMAND, DogCommand.FOLLOW.id());
    }
    this.dog.setSitting(false);
  }

  void setCommandFromSave(final DogCommand command) {
    this.dog.getDataTracker().set(UnleashedDogEntity.COMMAND, command.id());
  }

  void setAnchorFromSave(final BlockPos savedAnchorPos, final RegistryKey<World> savedDimension) {
    this.anchorAt(savedAnchorPos, savedDimension);
  }

  private void anchorAt(final BlockPos pos, final RegistryKey<World> dimension) {
    this.anchorPos = pos;
    this.anchorDimension = dimension;
  }

  private void clearAnchor() {
    this.anchorPos = null;
    this.anchorDimension = null;
  }
}
