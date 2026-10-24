package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.trick.DogTrick;
import com.grahambartley.dogsunleashed.entity.trick.TrickReadiness;
import org.jetbrains.annotations.Nullable;

public final class DogTrickPerformance extends DogComponent {

  private final UnleashedDogEntity dog;
  private @Nullable DogTrick performing;
  private long performingSince = DogTrick.NOT_STARTED;

  DogTrickPerformance(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public boolean isPerforming() {
    return this.performing != null
        && this.performing.isRunning(this.performingSince, this.dog.getWorld().getTime());
  }

  public TrickReadiness readiness() {
    return new TrickReadiness(
        this.dog.getSleepController().isAsleep(),
        this.dog.getComponents().get(DogSledHarness.class).isHitched(),
        this.dog.hasVehicle() || this.dog.hasPassengers(),
        this.dog.getTarget() != null,
        this.dog.hurtTime > 0,
        this.dog.isTouchingWater(),
        DogIdleness.isOccupied(this.dog),
        this.isPerforming());
  }

  public void perform(final DogTrick trick) {
    this.performing = trick;
    this.performingSince = this.dog.getWorld().getTime();
    this.dog.triggerAnim(trick.animationKey(), trick.animationKey());
  }

  private void stop() {
    if (this.isPerforming()) {
      this.dog.stopTriggeredAnim(this.performing.animationKey(), this.performing.animationKey());
    }
    this.performing = null;
    this.performingSince = DogTrick.NOT_STARTED;
  }

  @Override
  void tick() {
    if (this.performing != null && (!this.isPerforming() || this.readiness().interruptsTrick())) {
      this.stop();
    }
  }
}
