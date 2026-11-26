package com.grahambartley.dogsunleashed.gesture;

import com.grahambartley.dogsunleashed.gesture.PatSkeleton.Part;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class ArmReach {

  static final float UPPER = PatSkeleton.ELBOW;
  static final float LOWER = PatSkeleton.PALM - PatSkeleton.ELBOW;
  static final float MAX_STRETCH = 0.995f;
  static final int PASSES = 3;
  private static final float TINY = 1.0e-4f;

  private ArmReach() {}

  public static float reach() {
    return (UPPER + LOWER) * MAX_STRETCH;
  }

  public static Part toward(final Part arm, final Vector3f palmTarget, final Vector3f elbowHint) {
    Part solved = arm;
    for (int pass = 0; pass < PASSES; pass++) {
      final Vector3f centreOffset =
          solved.rotation().transform(new Vector3f(-PatSkeleton.ARM_CENTRE_SIDE, 0.0f, 0.0f));
      final Vector3f shoulder = new Vector3f(arm.pivot()).add(centreOffset);
      solved = solve(arm.pivot(), shoulder, palmTarget, elbowHint);
    }
    return solved;
  }

  private static Part solve(
      final Vector3f pivot,
      final Vector3f shoulder,
      final Vector3f target,
      final Vector3f elbowHint) {
    final Vector3f toTarget = new Vector3f(target).sub(shoulder);
    final float raw = toTarget.length();
    final Vector3f along = raw < TINY ? new Vector3f(0.0f, 1.0f, 0.0f) : toTarget.div(raw);
    final float distance =
        Math.max(Math.abs(UPPER - LOWER) + TINY, Math.min(raw, (UPPER + LOWER) * MAX_STRETCH));
    final Vector3f pole = new Vector3f(elbowHint).sub(shoulder);
    pole.sub(new Vector3f(along).mul(pole.dot(along)));
    if (pole.lengthSquared() < TINY) {
      pole.set(0.0f, 0.0f, 1.0f).sub(new Vector3f(along).mul(along.z));
    }
    pole.normalize();
    final float cosShoulder =
        (UPPER * UPPER + distance * distance - LOWER * LOWER) / (2.0f * UPPER * distance);
    final float shoulderAngle = (float) Math.acos(Math.max(-1.0f, Math.min(1.0f, cosShoulder)));
    final Vector3f upper =
        new Vector3f(along)
            .mul((float) Math.cos(shoulderAngle))
            .add(new Vector3f(pole).mul((float) Math.sin(shoulderAngle)));
    final Vector3f elbow = new Vector3f(upper).mul(UPPER).add(shoulder);
    final Vector3f lower =
        new Vector3f(along).mul(distance).add(shoulder).sub(elbow).div(LOWER).normalize();
    final Vector3f across = new Vector3f(lower).sub(new Vector3f(upper).mul(lower.dot(upper)));
    final Vector3f back =
        across.lengthSquared() < TINY ? new Vector3f(pole) : across.normalize().negate();
    final Vector3f side = new Vector3f(upper).cross(back);
    final Quaternionf rotation =
        new Quaternionf().setFromNormalized(new Matrix3f(side, upper, back));
    final float bend = -(float) Math.acos(Math.max(-1.0f, Math.min(1.0f, lower.dot(upper))));
    return new Part(new Vector3f(pivot), rotation, bend);
  }
}
