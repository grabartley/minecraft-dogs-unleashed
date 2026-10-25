package com.grahambartley.dogsunleashed.entity;

import java.util.Set;
import net.minecraft.util.math.BlockPos;

public final class DogNavigationRules {

  private static final double DETOUR_ALLOWANCE_BLOCKS = 48.0;
  private static final double MAX_SEARCH_BLOCKS = 176.0;
  private static final long WIDE_SEARCH_COOLDOWN_TICKS = 20;

  private DogNavigationRules() {}

  public static double searchRange(
      final double distanceToTarget, final double followRange, final boolean recentlyMissed) {
    if (recentlyMissed) {
      return followRange;
    }
    return Math.max(
        followRange, Math.min(MAX_SEARCH_BLOCKS, distanceToTarget + DETOUR_ALLOWANCE_BLOCKS));
  }

  public static float nodeBudgetScale(final double searchRange, final double nodeBudgetRange) {
    return (float) Math.max(1.0, searchRange / nodeBudgetRange);
  }

  public static boolean recentlyMissed(final long missedAt, final long now) {
    return missedAt > now - WIDE_SEARCH_COOLDOWN_TICKS;
  }

  public static boolean wideSearchMissed(
      final double searchRange, final double followRange, final boolean reachesTarget) {
    return searchRange > followRange && !reachesTarget;
  }

  public static double nearestDistance(final BlockPos from, final Set<BlockPos> targets) {
    return targets.stream()
        .mapToDouble(target -> Math.sqrt(from.getSquaredDistance(target)))
        .min()
        .orElse(0.0);
  }
}
