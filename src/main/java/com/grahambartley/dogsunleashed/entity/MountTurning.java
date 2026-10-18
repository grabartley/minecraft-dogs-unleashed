package com.grahambartley.dogsunleashed.entity;

import net.minecraft.util.math.MathHelper;

public final class MountTurning {

  static final float STANDING_TURN_DEGREES = 20.0f;
  static final float GALLOPING_TURN_DEGREES = 8.0f;
  static final float EASE = 0.5f;
  static final float SETTLE_DEGREES = 0.5f;

  private MountTurning() {}

  public static float maxTurn(final float pace) {
    return MathHelper.lerp(
        MathHelper.clamp(pace, 0.0f, 1.0f), STANDING_TURN_DEGREES, GALLOPING_TURN_DEGREES);
  }

  public static float turn(final float yaw, final float lookYaw, final float pace) {
    final float gap = MathHelper.wrapDegrees(lookYaw - yaw);
    if (Math.abs(gap) <= SETTLE_DEGREES) {
      return yaw + gap;
    }
    final float limit = maxTurn(pace);
    return yaw + MathHelper.clamp(gap * EASE, -limit, limit);
  }
}
