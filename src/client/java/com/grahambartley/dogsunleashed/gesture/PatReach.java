package com.grahambartley.dogsunleashed.gesture;

import net.minecraft.util.math.MathHelper;

public final class PatReach {

  static final float PIXELS_PER_BLOCK = 16.0f / 0.9375f;
  static final float ARM_REACH = 9.0f;
  static final float HIP_HEIGHT = 12.0f;
  static final float HIP_TO_SHOULDER = 10.0f;
  static final float SHOULDER_SPREAD = 5.0f;
  static final float KNEEL_BELOW = 13.0f;
  static final float KNEEL_BLEND = 1.0f;
  static final float MAX_LEAN = (float) Math.toRadians(45.0);
  static final int LEAN_STEPS = 40;

  public record Pose(float drop, float lean, float armPitch, float armInward) {}

  private PatReach() {}

  public static Pose toward(
      final float crownUpBlocks,
      final float crownForwardBlocks,
      final float crownSideBlocks,
      final float kneelDrop,
      final float minLean) {
    final float up = crownUpBlocks * PIXELS_PER_BLOCK;
    final float forward = crownForwardBlocks * PIXELS_PER_BLOCK;
    final float drop = kneelWeight(crownUpBlocks) * kneelDrop;
    float bestLean = minLean;
    float bestError = Float.MAX_VALUE;
    float bestPitch = Float.NaN;
    float closestLean = minLean;
    float closestDistance = Float.MAX_VALUE;
    for (int step = 0; step <= LEAN_STEPS; step++) {
      final float lean = minLean + (MAX_LEAN - minLean) * step / LEAN_STEPS;
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

  public static float kneelWeight(final float crownUpBlocks) {
    final float up = crownUpBlocks * PIXELS_PER_BLOCK;
    final float t =
        MathHelper.clamp((KNEEL_BELOW + KNEEL_BLEND - up) / (2.0f * KNEEL_BLEND), 0.0f, 1.0f);
    return t * t * (3.0f - 2.0f * t);
  }

  static float shoulderHeight(final float drop, final float lean) {
    return HIP_HEIGHT - drop + HIP_TO_SHOULDER * MathHelper.cos(lean);
  }
}
