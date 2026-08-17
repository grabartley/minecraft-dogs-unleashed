package com.grahambartley.dogsunleashed.entity;

/**
 * How long a dog's Dog Treat buff has left to run. {@link DogTreatBuff} owns the attribute
 * modifiers themselves; this owns the countdown that decides when they are applied and cleared.
 */
public final class DogTreatBuffState {

  private final UnleashedDogEntity dog;

  DogTreatBuffState(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public int getRemainingTicks() {
    return this.dog.getTreatBuffTicks();
  }

  public boolean isActive() {
    return this.getRemainingTicks() > 0;
  }

  /**
   * Starts (or refreshes) the buff and plays the reaction: a tail wag, a heart burst and a single
   * bark. Refreshing resets the full duration rather than stacking, matching how vanilla handles a
   * re-applied status effect of equal strength.
   */
  public void apply() {
    this.dog.setTreatBuffTicks(DogTreatBuff.DURATION_TICKS);
    DogTreatBuff.apply(this.dog);
    this.dog.getAmbienceEffects().startTailWag();
    this.dog.getAmbienceEffects().burstHearts();
    this.dog.getVocalization().forceBark(this.dog.getVocalization().getBarkPitch());
  }

  void tick() {
    final int remaining = this.getRemainingTicks();
    if (remaining <= 0) {
      return;
    }
    if (remaining == 1) {
      DogTreatBuff.clear(this.dog);
    }
    this.dog.setTreatBuffTicks(remaining - 1);
  }

  void setRemainingTicksFromSave(final int remainingTicks) {
    this.dog.setTreatBuffTicks(remainingTicks);
  }
}
