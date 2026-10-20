package com.grahambartley.dogsunleashed.entity.expedition;

public final class ExpeditionSchedule {

  public static final long TICKS_PER_DAY = 24000L;
  public static final long MIN_TRIP_TICKS = 1200L;

  private ExpeditionSchedule() {}

  public static long returnTimeOfDay(final long departedTimeOfDay) {
    final long nextMorning =
        Math.floorDiv(departedTimeOfDay, TICKS_PER_DAY) * TICKS_PER_DAY + TICKS_PER_DAY;
    return Math.max(nextMorning, departedTimeOfDay + MIN_TRIP_TICKS);
  }

  public static boolean isDue(
      final long returnTimeOfDay,
      final long departedGameTime,
      final long timeOfDay,
      final long gameTime) {
    return timeOfDay >= returnTimeOfDay || gameTime - departedGameTime >= TICKS_PER_DAY;
  }
}
