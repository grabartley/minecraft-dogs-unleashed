package com.grahambartley.dogsunleashed.entity.scent;

public final class ScentLeadRules {

  public static final double OWNER_WAIT_DISTANCE = 12.0;
  public static final double ENTITY_ARRIVAL_DISTANCE = 2.5;
  public static final double BLOCK_ARRIVAL_DISTANCE = 1.5;

  private ScentLeadRules() {}

  public static boolean shouldWaitForOwner(final double ownerDistance) {
    return ownerDistance > OWNER_WAIT_DISTANCE;
  }

  public static boolean hasArrived(
      final ScentTargetKind kind, final double horizontalDistance, final double distance) {
    return kind == ScentTargetKind.ENTITY
        ? distance <= ENTITY_ARRIVAL_DISTANCE
        : horizontalDistance <= BLOCK_ARRIVAL_DISTANCE;
  }

  public static ScentPhase arrivalPhase(final ScentTargetKind kind) {
    return kind == ScentTargetKind.SUSPICIOUS_BLOCK ? ScentPhase.DIGGING : ScentPhase.POINTING;
  }
}
