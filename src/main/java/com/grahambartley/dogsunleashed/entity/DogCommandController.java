package com.grahambartley.dogsunleashed.entity;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/** The dog's command mode, and the state that has to stay consistent with it. */
public final class DogCommandController {

  private final UnleashedDogEntity dog;

  private @Nullable BlockPos anchorPos = null;

  DogCommandController(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public @Nullable BlockPos getAnchorPos() {
    return this.anchorPos;
  }

  /**
   * Single entry point for switching command modes: keeps the sitting pose, the Stay/Guard anchor,
   * and any in-progress sleep consistent with the new command.
   */
  public void apply(final DogCommand command) {
    if (!this.dog.isTamed()) {
      return;
    }
    if (this.dog.isSleepingInBed() || this.dog.isCommandedToSleep()) {
      this.dog.getSleepController().markManuallyWoken();
      this.dog.wakeUp();
    }
    this.dog.setCommandRaw(command);
    this.anchorPos = command.isAnchored() ? this.dog.getBlockPos() : null;
    this.dog.setSitting(command == DogCommand.SIT);
    this.dog.stopJumping();
    this.dog.getNavigation().stop();
    this.dog.setTarget(null);
  }

  /** Bark-and-wag feedback for a command issued in person, separate from silent state changes. */
  public void acknowledge() {
    this.dog.getAmbienceEffects().startTailWag();
    this.dog.getVocalization().barkIfReady(this.dog.getVocalization().getBarkPitch());
  }

  /**
   * For code paths that force a sitting dog to stand (damage, play mode, bed-block sleep): the
   * command must stop being Sit or the pose and command would disagree, but a full {@link #apply}
   * would also clear the attack target these paths may have just set.
   */
  void demoteSitToFollow() {
    if (this.dog.getCommand() == DogCommand.SIT) {
      this.dog.setCommandRaw(DogCommand.FOLLOW);
    }
    this.dog.setSitting(false);
  }

  void setCommandFromSave(final DogCommand command) {
    this.dog.setCommandRaw(command);
  }

  void setAnchorPosFromSave(final BlockPos savedAnchorPos) {
    this.anchorPos = savedAnchorPos;
  }
}
