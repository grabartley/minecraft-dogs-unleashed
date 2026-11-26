package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.gesture.PlayerGesture;

public final class PettingRules {

  public static final int COOLDOWN_TICKS = PlayerGesture.PAT.ticks();
  public static final long NEVER = Long.MIN_VALUE;

  public record DogState(
      boolean asleep,
      boolean targetsPetter,
      boolean angryAtPetter,
      boolean occupied,
      boolean performingTrick,
      boolean inVehicle,
      boolean ridden,
      boolean hitched,
      boolean howling,
      boolean shaking,
      boolean growling) {

    boolean calm() {
      return !this.asleep && !this.targetsPetter && !this.angryAtPetter && !this.growling;
    }

    boolean free() {
      return !this.occupied
          && !this.performingTrick
          && !this.inVehicle
          && !this.ridden
          && !this.hitched
          && !this.howling
          && !this.shaking;
    }
  }

  private PettingRules() {}

  public static boolean isPetGesture(
      final boolean sneaking, final boolean mainHand, final boolean emptyHand) {
    return sneaking && mainHand && emptyHand;
  }

  public static boolean cooledDown(final long now, final long lastPettedAt) {
    return lastPettedAt == NEVER || now - lastPettedAt >= COOLDOWN_TICKS || now < lastPettedAt;
  }

  public static boolean isBeingPetted(final long now, final long lastPettedAt) {
    return !cooledDown(now, lastPettedAt);
  }

  public static boolean welcomes(final DogState dog, final long now, final long lastPettedAt) {
    return dog.calm() && dog.free() && cooledDown(now, lastPettedAt);
  }
}
