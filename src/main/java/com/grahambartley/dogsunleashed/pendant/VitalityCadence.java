package com.grahambartley.dogsunleashed.pendant;

public final class VitalityCadence {

  public static final int HEAL_INTERVAL_TICKS = 50;
  public static final int REFRESH_BELOW_TICKS = 30;
  public static final int NO_EFFECT = -1;
  public static final int NO_CHANGE = -1;

  private VitalityCadence() {}

  public static int durationToApply(final int remainingTicks) {
    if (remainingTicks == NO_EFFECT) {
      return HEAL_INTERVAL_TICKS;
    }
    if (remainingTicks <= REFRESH_BELOW_TICKS) {
      return remainingTicks + HEAL_INTERVAL_TICKS;
    }
    return NO_CHANGE;
  }
}
