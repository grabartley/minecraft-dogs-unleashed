package com.grahambartley.dogsunleashed.entity.goal;

public final class DogBowlVisitRules {

  public enum Bowl {
    WATER,
    FOOD
  }

  public static final int SEARCH_RANGE = 16;
  public static final int REGENERATION_TICKS = 200;
  static final double POST_RANGE = 8.0;
  static final int SEARCH_INTERVAL_TICKS = 20;
  static final int MEAL_COOLDOWN_TICKS = 100;
  static final int UNREACHABLE_COOLDOWN_TICKS = 1200;
  static final int MEAL_TICKS = 30;
  static final int MAX_WALK_TICKS = 400;
  static final int PATH_CHECKS_PER_SEARCH = 3;
  static final double REACH = 1.5;

  private DogBowlVisitRules() {}

  public static boolean wants(
      final Bowl bowl,
      final float health,
      final float maxHealth,
      final boolean undead,
      final boolean regenerating) {
    if (undead || health >= maxHealth) {
      return false;
    }
    return bowl == Bowl.FOOD || !regenerating;
  }

  public static boolean isSafe(
      final boolean onFire, final boolean recentlyAttacked, final boolean freezing) {
    return !onFire && !recentlyAttacked && !freezing;
  }

  public static boolean isWithinPost(
      final boolean anchored, final double squaredDistanceFromAnchor) {
    return !anchored || squaredDistanceFromAnchor <= POST_RANGE * POST_RANGE;
  }

  public static boolean isBeside(
      final int bowlX,
      final int bowlY,
      final int bowlZ,
      final double x,
      final double y,
      final double z) {
    final double dx = x - (bowlX + 0.5);
    final double dz = z - (bowlZ + 0.5);
    return Math.hypot(dx, dz) <= REACH && Math.abs(y - bowlY) <= 1.0;
  }
}
