package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.gesture.PlayerGesture;

public final class PettingRules {

  public static final int COOLDOWN_TICKS = PlayerGesture.PAT.ticks();
  public static final long NEVER = Long.MIN_VALUE;
  public static final double REACH_BLOCKS = 0.45;
  public static final int SETTLE_TICKS = 8;
  public static final int APPROACH_TICKS = PlayerGesture.INVITE.ticks() - SETTLE_TICKS;
  public static final double WALK_AWAY_BLOCKS = 1.0;
  public static final double HAND_SIDE_BLOCKS = 0.2;
  public static final double HAND_SPOT_FRACTION = 0.8;
  public static final double CLOSE_IN_BLOCKS = 1.5;

  public record Spot(double x, double z) {}

  public record DogState(
      boolean asleep,
      boolean targetsPetter,
      boolean angryAtPetter,
      boolean occupied,
      boolean performingTrick,
      boolean inVehicle,
      boolean ridden,
      boolean hitched,
      boolean howling,
      boolean shaking,
      boolean growling) {

    boolean calm() {
      return !this.asleep && !this.targetsPetter && !this.angryAtPetter && !this.growling;
    }

    boolean free() {
      return !this.occupied
          && !this.performingTrick
          && !this.inVehicle
          && !this.ridden
          && !this.hitched
          && !this.howling
          && !this.shaking;
    }
  }

  private PettingRules() {}

  public static boolean isPetGesture(
      final boolean sneaking, final boolean mainHand, final boolean emptyHand) {
    return sneaking && mainHand && emptyHand;
  }

  public static boolean cooledDown(final long now, final long lastPettedAt) {
    return lastPettedAt == NEVER || now - lastPettedAt >= COOLDOWN_TICKS || now < lastPettedAt;
  }

  public static boolean isBeingPetted(final long now, final long lastPettedAt) {
    return !cooledDown(now, lastPettedAt);
  }

  public static boolean isApproaching(final long now, final long requestedAt) {
    return requestedAt != NEVER
        && now >= requestedAt
        && now - requestedAt < SETTLE_TICKS + APPROACH_TICKS;
  }

  public static boolean hasSettled(final long now, final long requestedAt) {
    return requestedAt != NEVER && now - requestedAt >= SETTLE_TICKS;
  }

  public static boolean closeEnoughToStep(final double blocksToSpot) {
    return blocksToSpot <= CLOSE_IN_BLOCKS;
  }

  public static boolean walkedAway(final double movedBlocks) {
    return movedBlocks > WALK_AWAY_BLOCKS;
  }

  public static Spot handSpot(
      final double x,
      final double z,
      final float yawDegrees,
      final boolean rightHanded,
      final double dogHalfWidth) {
    final double yaw = Math.toRadians(yawDegrees);
    final double forward = (REACH_BLOCKS + dogHalfWidth) * HAND_SPOT_FRACTION;
    final double side = rightHanded ? HAND_SIDE_BLOCKS : -HAND_SIDE_BLOCKS;
    return new Spot(
        x - Math.sin(yaw) * forward - Math.cos(yaw) * side,
        z + Math.cos(yaw) * forward - Math.sin(yaw) * side);
  }

  public static boolean inReach(final double horizontalDistance, final double dogHalfWidth) {
    return horizontalDistance <= REACH_BLOCKS + dogHalfWidth;
  }

  public static boolean welcomes(
      final DogState dog, final long now, final long lastPettedAt, final long requestedAt) {
    return dog.calm()
        && dog.free()
        && cooledDown(now, lastPettedAt)
        && !isApproaching(now, requestedAt);
  }
}
