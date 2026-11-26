package com.grahambartley.dogsunleashed.gesture;

import net.minecraft.util.math.MathHelper;

public final class PatReach {

  static final float PIXELS_PER_BLOCK = 16.0f / 0.9375f;
  static final float ARM_REACH = 9.0f;
  static final float HIP_HEIGHT = 12.0f;
  static final float HIP_TO_SHOULDER = 10.0f;
  static final float SHOULDER_SPREAD = 5.0f;
  static final float KNEEL_DROP = 6.0f;
  static final float KNEEL_BELOW = 13.0f;
  static final float MAX_LEAN = (float) Math.toRadians(35.0);
  static final int LEAN_STEPS = 40;

  public record Pose(float drop, float lean, float armPitch, float armInward) {

    public float eyeDropBlocks() {
      return this.drop / PIXELS_PER_BLOCK;
    }
  }

  private PatReach() {}

  public static Pose toward(
      final float crownUpBlocks, final float crownForwardBlocks, final float crownSideBlocks) {
    final float up = crownUpBlocks * PIXELS_PER_BLOCK;
    final float forward = crownForwardBlocks * PIXELS_PER_BLOCK;
    final float drop = up < KNEEL_BELOW ? KNEEL_DROP : 0.0f;
    float bestLean = 0.0f;
    float bestError = Float.MAX_VALUE;
    float bestPitch = Float.NaN;
    float closestLean = 0.0f;
    float closestDistance = Float.MAX_VALUE;
    for (int step = 0; step <= LEAN_STEPS; step++) {
      final float lean = MAX_LEAN * step / LEAN_STEPS;
      final float shoulderUp = shoulderHeight(drop, lean);
      final float shoulderForward = HIP_TO_SHOULDER * MathHelper.sin(lean);
      final float below = shoulderUp - up;
      final float distance = (float) MathHelper.hypot(forward - shoulderForward, below);
      if (distance < closestDistance) {
        closestDistance = distance;
        closestLean = lean;
      }
      if (below < 0.0f || below > ARM_REACH) {
        continue;
      }
      final float handForward = MathHelper.sqrt(ARM_REACH * ARM_REACH - below * below);
      final float error = Math.abs(shoulderForward + handForward - forward);
      if (error < bestError) {
        bestError = error;
        bestLean = lean;
        bestPitch = -(float) MathHelper.atan2(handForward, below);
      }
    }
    if (Float.isNaN(bestPitch)) {
      bestLean = closestLean;
      bestPitch =
          -(float)
              MathHelper.atan2(
                  forward - HIP_TO_SHOULDER * MathHelper.sin(closestLean),
                  shoulderHeight(drop, closestLean) - up);
    }
    final float reachForward = Math.max(1.0f, forward - HIP_TO_SHOULDER * MathHelper.sin(bestLean));
    final float inward =
        (float)
            MathHelper.atan2(SHOULDER_SPREAD - crownSideBlocks * PIXELS_PER_BLOCK, reachForward);
    return new Pose(drop, bestLean, bestPitch, inward);
  }

  static float shoulderHeight(final float drop, final float lean) {
    return HIP_HEIGHT - drop + HIP_TO_SHOULDER * MathHelper.cos(lean);
  }
}
