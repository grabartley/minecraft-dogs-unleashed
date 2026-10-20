package com.grahambartley.dogsunleashed.entity.burial;

public final class BoneCacheRules {

  public static final int BURY_RADIUS = 16;
  public static final int DIG_TICKS = 60;
  public static final int DAYS_UNTIL_DIG_UP = 3;
  public static final long TICKS_PER_DAY = 24000L;
  public static final int BONUS_ODDS = 5;
  public static final double OWNER_RANGE = 32.0;
  public static final double DIG_UP_RANGE = 48.0;

  private BoneCacheRules() {}

  public static boolean isDue(final long buriedAt, final long now) {
    return now < buriedAt || now - buriedAt >= DAYS_UNTIL_DIG_UP * TICKS_PER_DAY;
  }

  public static boolean rollsBonus(final int roll) {
    return roll == 0;
  }
}
