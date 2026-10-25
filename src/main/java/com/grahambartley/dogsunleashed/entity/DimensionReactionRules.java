package com.grahambartley.dogsunleashed.entity;

public final class DimensionReactionRules {

  public static final double NETHER_FOLLOW_DISTANCE_SCALE = 0.5;
  public static final double CALM_FOLLOW_DISTANCE_SCALE = 1.0;
  public static final long UNEASE_MIN_GAP_TICKS = 60 * 20;
  public static final int UNEASE_ODDS = 120 * 20;
  public static final long ENDERMAN_BARK_EVERY_TICKS = 3 * 20;
  public static final double ENDERMAN_BARK_RANGE = 12.0;

  private DimensionReactionRules() {}

  public static boolean isUneasy(final boolean inNether, final boolean fireGuarded) {
    return inNether && !fireGuarded;
  }

  public static double closestFollowDistanceScale() {
    return Math.min(NETHER_FOLLOW_DISTANCE_SCALE, CALM_FOLLOW_DISTANCE_SCALE);
  }

  public static double followDistanceScale(final boolean uneasy) {
    return uneasy ? NETHER_FOLLOW_DISTANCE_SCALE : CALM_FOLLOW_DISTANCE_SCALE;
  }

  public static boolean actsUneasyNow(
      final boolean reactedSinceArrival, final long ticksSinceReaction, final int roll) {
    return !reactedSinceArrival || (ticksSinceReaction >= UNEASE_MIN_GAP_TICKS && roll == 0);
  }

  public static boolean cowersWhenUneasy(final boolean sitting) {
    return !sitting;
  }

  public static boolean sparesEnderman(final boolean inEnd, final boolean enderman) {
    return inEnd && enderman;
  }

  public static boolean watchesEndermen(
      final boolean inEnd, final boolean asleep, final boolean sitting) {
    return inEnd && !asleep && !sitting;
  }

  public static boolean barksNow(final long watchTicks) {
    return watchTicks % ENDERMAN_BARK_EVERY_TICKS == 0;
  }
}
