package com.grahambartley.dogsunleashed.entity;

public final class RaidDutyRules {

  public static final int CHECK_INTERVAL_TICKS = 20;

  public enum Outcome {
    PENDING,
    REWARD,
    FORGET
  }

  private RaidDutyRules() {}

  public static boolean counts(final boolean tamed, final boolean opponentInActiveRaid) {
    return tamed && opponentInActiveRaid;
  }

  public static Outcome outcome(final boolean won, final boolean ended) {
    if (won) {
      return Outcome.REWARD;
    }
    return ended ? Outcome.FORGET : Outcome.PENDING;
  }
}
