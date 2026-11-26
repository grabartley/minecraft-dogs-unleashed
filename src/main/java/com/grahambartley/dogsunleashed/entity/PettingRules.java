package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.gesture.PlayerGesture;

public final class PettingRules {

  public static final int COOLDOWN_TICKS = PlayerGesture.PAT.ticks();
  public static final long NEVER = Long.MIN_VALUE;

  public record DogState(boolean asleep, boolean hostile, boolean busy, boolean carryingPetter) {

    boolean welcomesPetting() {
      return !this.asleep && !this.hostile && !this.busy && !this.carryingPetter;
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

  public static boolean welcomes(final DogState dog, final long now, final long lastPettedAt) {
    return dog.welcomesPetting() && cooledDown(now, lastPettedAt);
  }
}
