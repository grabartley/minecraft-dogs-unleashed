package com.grahambartley.dogsunleashed.gesture;

import com.grahambartley.dogsunleashed.gesture.PatSkeleton.Bones;
import org.joml.Vector3f;

public final class PatReach {

  static final float MAX_EXTRA_LEAN = (float) Math.toRadians(40.0);
  static final int LEAN_STEPS = 20;

  private PatReach() {}

  public static float extraLean(final Bones bones, final Vector3f palmTarget) {
    float closestLean = 0.0f;
    float closest = Float.MAX_VALUE;
    for (int step = 0; step <= LEAN_STEPS; step++) {
      final float lean = MAX_EXTRA_LEAN * step / LEAN_STEPS;
      final PatSkeleton.Part arm = PatSkeleton.of(PatBones.leanedIn(bones, lean)).rightArm();
      final float gap = arm.pivot().distance(palmTarget) - ArmReach.reach();
      if (gap <= 0.0f) {
        return lean;
      }
      if (gap < closest) {
        closest = gap;
        closestLean = lean;
      }
    }
    return closestLean;
  }
}
