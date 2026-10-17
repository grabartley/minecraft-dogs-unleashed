package com.grahambartley.dogsunleashed.pendant;

import java.util.OptionalInt;

public final class VitalityCadence {

  public static final int HEAL_INTERVAL_TICKS = 50;
  public static final int REFRESH_BELOW_TICKS = 30;

  private VitalityCadence() {}

  public static OptionalInt durationToApply(final OptionalInt remainingTicks) {
    if (remainingTicks.isEmpty()) {
      return OptionalInt.of(HEAL_INTERVAL_TICKS);
    }
    final int remaining = remainingTicks.getAsInt();
    if (remaining < 0 || remaining > REFRESH_BELOW_TICKS) {
      return OptionalInt.empty();
    }
    return OptionalInt.of(remaining + HEAL_INTERVAL_TICKS);
  }
}
