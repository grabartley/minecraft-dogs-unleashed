package com.grahambartley.dogsunleashed.entity;

public final class DogNavigationRules {

  public static final double DETOUR_ALLOWANCE_BLOCKS = 48.0;
  public static final double MAX_SEARCH_BLOCKS = 176.0;
  public static final long RETRY_UNREACHABLE_TICKS = 20;

  private DogNavigationRules() {}

  public static double searchRange(final double distanceToTarget, final double followRange) {
    return Math.max(
        followRange, Math.min(MAX_SEARCH_BLOCKS, distanceToTarget + DETOUR_ALLOWANCE_BLOCKS));
  }

  public static float nodeBudgetScale(final double searchRange, final double nodeBudgetRange) {
    return (float) Math.max(1.0, searchRange / nodeBudgetRange);
  }

  public static boolean mayRetryUnreachable(final long missedAt, final long now) {
    return now - missedAt >= RETRY_UNREACHABLE_TICKS;
  }
}
