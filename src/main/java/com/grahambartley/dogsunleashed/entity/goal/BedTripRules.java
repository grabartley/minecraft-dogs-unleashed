package com.grahambartley.dogsunleashed.entity.goal;

public final class BedTripRules {

  public static final double DETOUR_ALLOWANCE_BLOCKS = 48.0;
  public static final double MAX_SEARCH_BLOCKS = 128.0;
  public static final int REPATH_INTERVAL_TICKS = 20;
  public static final double ARRIVAL_DISTANCE_BLOCKS = 2.0;

  private BedTripRules() {}

  public static boolean hasArrived(final double squaredDistanceToBed) {
    return squaredDistanceToBed <= ARRIVAL_DISTANCE_BLOCKS * ARRIVAL_DISTANCE_BLOCKS;
  }

  public static double searchRange(final double distanceToBed) {
    return Math.min(MAX_SEARCH_BLOCKS, Math.max(0.0, distanceToBed) + DETOUR_ALLOWANCE_BLOCKS);
  }

  public static double extraFollowRange(final double distanceToBed, final double baseFollowRange) {
    return Math.max(0.0, searchRange(distanceToBed) - baseFollowRange);
  }

  public static float nodeBudgetScale(final double distanceToBed, final double baseFollowRange) {
    return (float) Math.max(1.0, searchRange(distanceToBed) / baseFollowRange);
  }
}
