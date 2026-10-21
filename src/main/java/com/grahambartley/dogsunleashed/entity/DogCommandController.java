package com.grahambartley.dogsunleashed.entity;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class DogCommandController {

  private final UnleashedDogEntity dog;

  private @Nullable BlockPos anchorPos = null;
  private @Nullable RegistryKey<World> anchorDimension = null;
  private boolean headingToAnchor = false;

  DogCommandController(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public @Nullable BlockPos getAnchorPos() {
    return SavedDimension.isHere(this.anchorDimension, this.dog.getWorld().getRegistryKey())
        ? this.anchorPos
        : null;
  }

  public void goTo(final BlockPos pos) {
    this.apply(DogCommand.STAY);
    this.anchorAt(pos, this.dog.getWorld().getRegistryKey());
    this.headingToAnchor = true;
  }

  public boolean isHeadingToAnchor() {
    return this.headingToAnchor && this.getAnchorPos() != null;
  }

  public void arrivedAtAnchor() {
    this.headingToAnchor = false;
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
    this.wakeIfSleeping();
    this.dog.getComponents().get(DogDeathWatch.class).end();
    this.dog.getDataTracker().set(UnleashedDogEntity.COMMAND, command.id());
    this.dog.getComponents().get(DogPointedOrders.class).clear();
    this.dog.getComponents().get(DogScent.class).stop();
    this.dog.getComponents().get(DogPlayChase.class).end();
    this.dog.getComponents().get(DogTug.class).abandon();
    this.headingToAnchor = false;
    if (command.isAnchored()) {
      this.anchorAt(this.dog.getBlockPos(), this.dog.getWorld().getRegistryKey());
    } else {
      this.clearAnchor();
    }
    this.settle(command);
  }

  void guardAt(final BlockPos spot, final RegistryKey<World> dimension) {
    this.wakeIfSleeping();
    this.dog.getDataTracker().set(UnleashedDogEntity.COMMAND, DogCommand.GUARD.id());
    this.anchorAt(spot, dimension);
    this.settle(DogCommand.GUARD);
  }

  private void settle(final DogCommand command) {
    this.dog.setSitting(command == DogCommand.SIT);
    this.dog.stopJumping();
    this.dog.getNavigation().stop();
    this.dog.setTarget(null);
  }

  private void wakeIfSleeping() {
    if (this.dog.getSleepController().isAsleep()
        || this.dog.getSleepController().isCommandedToSleep()) {
      this.dog.getSleepController().markManuallyWoken();
      this.dog.wakeUp();
    }
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
