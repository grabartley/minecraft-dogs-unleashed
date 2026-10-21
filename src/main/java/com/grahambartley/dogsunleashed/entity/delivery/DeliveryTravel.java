package com.grahambartley.dogsunleashed.entity.delivery;

public final class DeliveryTravel {

  public static final double BLOCKS_PER_SECOND = 8.0;
  public static final long TICKS_PER_SECOND = 20L;
  public static final long MIN_LEG_TICKS = 200L;
  public static final long MAX_LEG_TICKS = 6000L;
  public static final long CROSS_DIMENSION_TICKS = 600L;

  private DeliveryTravel() {}

  public static long legTicks(final double blocks, final boolean crossDimension) {
    final long walking = Math.round(blocks / BLOCKS_PER_SECOND * TICKS_PER_SECOND);
    final long clamped = Math.max(MIN_LEG_TICKS, Math.min(MAX_LEG_TICKS, walking));
    return crossDimension ? clamped + CROSS_DIMENSION_TICKS : clamped;
  }

  public static double distance(
      final double fromX,
      final double fromZ,
      final double fromScale,
      final double toX,
      final double toZ,
      final double toScale) {
    final double dx = fromX * fromScale - toX * toScale;
    final double dz = fromZ * fromScale - toZ * toScale;
    return Math.sqrt(dx * dx + dz * dz);
  }
}
