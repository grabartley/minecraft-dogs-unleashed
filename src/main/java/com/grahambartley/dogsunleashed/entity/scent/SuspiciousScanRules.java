package com.grahambartley.dogsunleashed.entity.scent;

import java.util.Optional;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class SuspiciousScanRules {

  private SuspiciousScanRules() {}

  public static Optional<BlockPos> nearestWithin(
      final BlockPos origin, final int range, final Iterable<BlockPos> candidates) {
    final long maxSquared = (long) range * range;
    BlockPos nearest = null;
    for (final BlockPos candidate : candidates) {
      final long squared = squaredDistance(origin, candidate);
      if (squared <= maxSquared
          && (nearest == null || squared < squaredDistance(origin, nearest))) {
        nearest = candidate;
      }
    }
    return Optional.ofNullable(nearest);
  }

  public static boolean isCloser(
      final BlockPos origin, final BlockPos candidate, final @Nullable BlockPos current) {
    return current == null || squaredDistance(origin, candidate) < squaredDistance(origin, current);
  }

  public static long squaredDistance(final BlockPos origin, final BlockPos pos) {
    return (long) pos.getSquaredDistance(origin);
  }
}
