package com.grahambartley.dogsunleashed.gesture;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class PatSkeleton {

  static final Vector3f HIPS = new Vector3f(0.0f, 12.0f, 0.0f);
  static final float TORSO_LENGTH = 12.0f;
  static final float SHOULDER_BELOW_NECK = 2.0f;
  static final float SHOULDER_SIDE = 5.0f;
  static final float HIP_SIDE = 1.9f;
  static final float ARM_CENTRE_SIDE = 1.0f;
  public static final float ELBOW = 4.0f;
  public static final float KNEE = 6.0f;
  static final float PALM = 9.0f;

  public record Bones(
      Vector3f hipsOffset,
      Quaternionf hips,
      Quaternionf torso,
      Quaternionf head,
      Quaternionf rightArm,
      float rightElbow,
      Quaternionf leftArm,
      float leftElbow,
      Quaternionf rightLeg,
      float rightKnee,
      Quaternionf leftLeg,
      float leftKnee) {}

  public record Part(Vector3f pivot, Quaternionf rotation, float bend) {

    public Vector3f angles() {
      return new Matrix4f().rotation(this.rotation).getEulerAnglesZYX(new Vector3f());
    }

    Part mirrored() {
      return new Part(
          new Vector3f(-this.pivot.x, this.pivot.y, this.pivot.z),
          new Quaternionf(this.rotation.x, -this.rotation.y, -this.rotation.z, this.rotation.w),
          this.bend);
    }
  }

  public record Pose(
      Part body, Part head, Part rightArm, Part leftArm, Part rightLeg, Part leftLeg) {

    public Pose mirrored() {
      return new Pose(
          this.body.mirrored(),
          this.head.mirrored(),
          this.leftArm.mirrored(),
          this.rightArm.mirrored(),
          this.leftLeg.mirrored(),
          this.rightLeg.mirrored());
    }

    public Pose withRightArm(final Part arm) {
      return new Pose(this.body, this.head, arm, this.leftArm, this.rightLeg, this.leftLeg);
    }

    public Pose withHead(final Part newHead) {
      return new Pose(this.body, newHead, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg);
    }
  }

  private PatSkeleton() {}

  public static Pose of(final Bones bones) {
    final Vector3f hips = new Vector3f(HIPS).add(bones.hipsOffset());
    final Quaternionf torso = new Quaternionf(bones.hips()).mul(bones.torso());
    final Vector3f neck = at(hips, torso, 0.0f, -TORSO_LENGTH);
    final float shoulderUp = -TORSO_LENGTH + SHOULDER_BELOW_NECK;
    return new Pose(
        new Part(neck, torso, 0.0f),
        new Part(new Vector3f(neck), new Quaternionf(torso).mul(bones.head()), 0.0f),
        new Part(
            at(hips, torso, -SHOULDER_SIDE, shoulderUp),
            new Quaternionf(torso).mul(bones.rightArm()),
            bones.rightElbow()),
        new Part(
            at(hips, torso, SHOULDER_SIDE, shoulderUp),
            new Quaternionf(torso).mul(bones.leftArm()),
            bones.leftElbow()),
        new Part(
            at(hips, bones.hips(), -HIP_SIDE, 0.0f),
            new Quaternionf(bones.hips()).mul(bones.rightLeg()),
            bones.rightKnee()),
        new Part(
            at(hips, bones.hips(), HIP_SIDE, 0.0f),
            new Quaternionf(bones.hips()).mul(bones.leftLeg()),
            bones.leftKnee()));
  }

  public static Vector3f rightPalm(final Part arm) {
    return pointOnArm(arm, -ARM_CENTRE_SIDE, PALM);
  }

  public static Vector3f rightElbow(final Part arm) {
    return pointOnArm(arm, -ARM_CENTRE_SIDE, ELBOW);
  }

  static Vector3f pointOnArm(final Part arm, final float side, final float along) {
    final Vector3f local =
        along <= ELBOW
            ? new Vector3f(side, along, 0.0f)
            : new Vector3f(0.0f, along - ELBOW, 0.0f).rotateX(arm.bend()).add(side, ELBOW, 0.0f);
    return arm.rotation().transform(local).add(arm.pivot());
  }

  private static Vector3f at(
      final Vector3f origin, final Quaternionf rotation, final float side, final float up) {
    return rotation.transform(new Vector3f(side, up, 0.0f)).add(origin);
  }
}
