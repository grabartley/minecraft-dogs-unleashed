package com.grahambartley.dogsunleashed.entity;

public final class PetSpot {

  static final double PLAYER_HALF_WIDTH = 0.3;
  static final double CLEARANCE = 0.05;
  static final double KNEEL_HAND_FORWARD = 0.42;
  static final double KNEEL_HAND_SIDE = 0.22;
  static final double STAND_HAND_FORWARD = 0.5;
  static final double STAND_HAND_SIDE = 0.3;
  static final double ALONGSIDE_FORWARD_PER_NECK = -0.43;
  static final double ALONGSIDE_SIDE_PER_NECK = 0.964;
  static final double BOW_FORWARD_PER_NECK = 0.347;
  static final double BOW_SIDE_PER_NECK = 1.341;
  static final double BOW_FACE_FORWARD = 0.342;
  static final double BOW_FACE_ACROSS = 0.94;
  static final double LOOK_AHEAD = 3.0;
  static final double ARRIVE_BLOCKS = 0.3;
  static final double NECK_SLACK_BLOCKS = 0.25;
  static final double VERTICAL_REACH_BLOCKS = 1.0;

  public record Place(double x, double z, double faceX, double faceZ) {

    public float faceYawDegrees() {
      return (float) Math.toDegrees(Math.atan2(this.faceZ - this.z, this.faceX - this.x)) - 90.0f;
    }
  }

  public record Point(double x, double z) {}

  private record Local(double forward, double right, double faceForward, double faceRight) {

    Local towards(final Local other, final double weight) {
      return new Local(
          lerp(weight, this.forward, other.forward),
          lerp(weight, this.right, other.right),
          lerp(weight, this.faceForward, other.faceForward),
          lerp(weight, this.faceRight, other.faceRight));
    }

    Local clearOf(final double nearest) {
      final double distance = Math.hypot(this.forward, this.right);
      if (distance >= nearest) {
        return this;
      }
      final double right =
          Math.copySign(
              Math.sqrt(Math.max(0.0, nearest * nearest - this.forward * this.forward)),
              this.right);
      return new Local(this.forward, right, this.faceForward, this.faceRight + right - this.right);
    }
  }

  private PetSpot() {}

  public static Place of(
      final double playerX,
      final double playerZ,
      final float yawDegrees,
      final boolean rightHanded,
      final DogHeadReach head,
      final double dogHalfWidth,
      final float kneel,
      final boolean bows) {
    final double side = rightHanded ? 1.0 : -1.0;
    final Local spot =
        (bows
                ? bowing(side, head)
                : facingPast(side, head, kneel).towards(alongside(side, head), 1.0 - kneel))
            .clearOf(PLAYER_HALF_WIDTH + dogHalfWidth + CLEARANCE);
    final double yaw = Math.toRadians(yawDegrees);
    final double sin = Math.sin(yaw);
    final double cos = Math.cos(yaw);
    return new Place(
        playerX - sin * spot.forward() - cos * spot.right(),
        playerZ + cos * spot.forward() - sin * spot.right(),
        playerX - sin * spot.faceForward() - cos * spot.faceRight(),
        playerZ + cos * spot.faceForward() - sin * spot.faceRight());
  }

  private static Local facingPast(final double side, final DogHeadReach head, final float kneel) {
    final double handForward = lerp(kneel, STAND_HAND_FORWARD, KNEEL_HAND_FORWARD);
    final double handSide = side * lerp(kneel, STAND_HAND_SIDE, KNEEL_HAND_SIDE);
    final double faceSide = side * (PLAYER_HALF_WIDTH + head.halfWidth() + CLEARANCE);
    final double towardSide = handSide - faceSide;
    final double length = Math.hypot(handForward, towardSide);
    return new Local(
        handForward + handForward / length * head.neck(),
        handSide + towardSide / length * head.neck(),
        0.0,
        faceSide);
  }

  private static Local alongside(final double side, final DogHeadReach head) {
    final double forward = ALONGSIDE_FORWARD_PER_NECK * head.neck();
    final double right = side * ALONGSIDE_SIDE_PER_NECK * head.neck();
    return new Local(forward, right, forward + LOOK_AHEAD, right);
  }

  private static Local bowing(final double side, final DogHeadReach head) {
    final double forward = BOW_FORWARD_PER_NECK * head.neck();
    final double right = side * BOW_SIDE_PER_NECK * head.neck();
    return new Local(
        forward,
        right,
        forward + BOW_FACE_FORWARD * LOOK_AHEAD,
        right - side * BOW_FACE_ACROSS * LOOK_AHEAD);
  }

  public static Point neckOf(
      final double dogX, final double dogZ, final float bodyYawDegrees, final double neck) {
    final double yaw = Math.toRadians(bodyYawDegrees);
    return new Point(dogX - Math.sin(yaw) * neck, dogZ + Math.cos(yaw) * neck);
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

  private static double lerp(final double t, final double from, final double to) {
    return from + (to - from) * t;
  }
}
