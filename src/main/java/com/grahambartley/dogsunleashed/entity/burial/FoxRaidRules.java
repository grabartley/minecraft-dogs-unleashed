package com.grahambartley.dogsunleashed.entity.burial;

public final class FoxRaidRules {

  public static final double RAID_RANGE = 24.0;
  public static final double GUARD_RANGE = 16.0;
  public static final int RAID_ODDS = 4;
  public static final int CHASE_TICKS = 120;
  public static final double CHASE_DISTANCE = 12.0;

  private FoxRaidRules() {}

  public static boolean mayRaid(
      final boolean night, final boolean wild, final boolean mouthEmpty, final boolean busy) {
    return night && wild && mouthEmpty && !busy;
  }

  public static boolean rollsRaid(final int roll) {
    return roll == 0;
  }

  public static boolean chaseIsOver(final int ticks, final double distanceFromStart) {
    return ticks >= CHASE_TICKS || distanceFromStart >= CHASE_DISTANCE;
  }
}
