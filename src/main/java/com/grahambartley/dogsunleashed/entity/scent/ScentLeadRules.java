package com.grahambartley.dogsunleashed.entity.scent;

public final class ScentLeadRules {

  public static final double OWNER_WAIT_DISTANCE = 12.0;
  public static final double ENTITY_ARRIVAL_DISTANCE = 4.0;
  public static final double BLOCK_ARRIVAL_DISTANCE = 1.5;
  public static final double LANDMARK_ARRIVAL_DISTANCE = 0.5;
  private static final double MAX_LEG = 16.0;

  private ScentLeadRules() {}

  public static boolean shouldWaitForOwner(final double ownerDistance) {
    return ownerDistance > OWNER_WAIT_DISTANCE;
  }

  public static boolean hasArrived(
      final ScentTargetKind kind,
      final double horizontalDistance,
      final double distance,
      final boolean stopped) {
    return switch (kind) {
      case ENTITY -> distance <= ENTITY_ARRIVAL_DISTANCE;
      case LANDMARK ->
          horizontalDistance <= LANDMARK_ARRIVAL_DISTANCE
              || (stopped && horizontalDistance <= BLOCK_ARRIVAL_DISTANCE);
      case BLOCK, SUSPICIOUS_BLOCK -> horizontalDistance <= BLOCK_ARRIVAL_DISTANCE;
    };
  }

  public static int pathReach(final ScentTargetKind kind) {
    return kind == ScentTargetKind.LANDMARK ? 0 : 1;
  }

  public static double legFraction(final double horizontalDistance) {
    return horizontalDistance <= MAX_LEG ? 1.0 : MAX_LEG / horizontalDistance;
  }

  public static ScentPhase arrivalPhase(final ScentTargetKind kind) {
    return kind == ScentTargetKind.SUSPICIOUS_BLOCK || kind == ScentTargetKind.LANDMARK
        ? ScentPhase.DIGGING
        : ScentPhase.POINTING;
  }
}
