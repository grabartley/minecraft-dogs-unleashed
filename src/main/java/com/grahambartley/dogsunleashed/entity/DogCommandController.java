package com.grahambartley.dogsunleashed.entity;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class DogCommandController {

  private final UnleashedDogEntity dog;

  private @Nullable BlockPos anchorPos = null;

  DogCommandController(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public @Nullable BlockPos getAnchorPos() {
    return this.anchorPos;
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
    this.anchorPos = command.isAnchored() ? this.dog.getBlockPos() : null;
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

  void setAnchorPosFromSave(final BlockPos savedAnchorPos) {
    this.anchorPos = savedAnchorPos;
  }
}
