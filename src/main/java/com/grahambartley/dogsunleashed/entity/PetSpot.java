package com.grahambartley.dogsunleashed.entity;

public final class PetSpot {

  static final double PLAYER_HALF_WIDTH = 0.3;
  static final double CLEARANCE = 0.05;
  static final double KNEEL_HAND_FORWARD = 0.42;
  static final double KNEEL_HAND_SIDE = 0.22;
  static final double STAND_HAND_FORWARD = 0.5;
  static final double STAND_HAND_SIDE = 0.3;
  static final double ARRIVE_BLOCKS = 0.3;
  static final double NECK_SLACK_BLOCKS = 0.25;
  static final double VERTICAL_REACH_BLOCKS = 1.0;

  public record Place(double x, double z, double faceX, double faceZ) {}

  private PetSpot() {}

  public static Place of(
      final double playerX,
      final double playerZ,
      final float yawDegrees,
      final boolean rightHanded,
      final DogHeadReach head,
      final double dogHalfWidth,
      final float kneel) {
    final double side = rightHanded ? 1.0 : -1.0;
    final double handForward = lerp(kneel, STAND_HAND_FORWARD, KNEEL_HAND_FORWARD);
    final double handSide = side * lerp(kneel, STAND_HAND_SIDE, KNEEL_HAND_SIDE);
    final double faceSide = side * (PLAYER_HALF_WIDTH + head.halfWidth() + CLEARANCE);
    final double towardForward = handForward;
    final double towardSide = handSide - faceSide;
    final double length = Math.hypot(towardForward, towardSide);
    double forward = handForward + towardForward / length * head.neck();
    double right = handSide + towardSide / length * head.neck();
    final double distance = Math.hypot(forward, right);
    final double nearest = PLAYER_HALF_WIDTH + dogHalfWidth + CLEARANCE;
    if (distance < nearest) {
      forward *= nearest / distance;
      right *= nearest / distance;
    }
    final double yaw = Math.toRadians(yawDegrees);
    final double sin = Math.sin(yaw);
    final double cos = Math.cos(yaw);
    return new Place(
        playerX - sin * forward - cos * right,
        playerZ + cos * forward - sin * right,
        playerX - cos * faceSide,
        playerZ - sin * faceSide);
  }

  public static boolean arrived(final double blocksFromSpot) {
    return blocksFromSpot <= ARRIVE_BLOCKS;
  }

  public static boolean reachesNeck(
      final double neckBlocksAway, final double neckRise, final float kneel) {
    final double hand =
        Math.hypot(
            lerp(kneel, STAND_HAND_FORWARD, KNEEL_HAND_FORWARD),
            lerp(kneel, STAND_HAND_SIDE, KNEEL_HAND_SIDE));
    return neckBlocksAway <= hand + NECK_SLACK_BLOCKS
        && Math.abs(neckRise) <= VERTICAL_REACH_BLOCKS;
  }

  private static double lerp(final float t, final double from, final double to) {
    return from + (to - from) * t;
  }
}
