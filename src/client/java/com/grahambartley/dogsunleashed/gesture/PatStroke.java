package com.grahambartley.dogsunleashed.gesture;

import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

public final class PatStroke {

  static final float REACH_TICKS = 5.0f;
  static final int PATS = 3;
  static final float AIM_RADIANS = -MathHelper.HALF_PI;
  static final float PAT_LIFT_RADIANS = 0.3f;
  static final float INWARD_YAW_RADIANS = 0.3f;
  static final float FIRST_PERSON_INWARD = 0.3f;
  static final float FIRST_PERSON_DROP = 0.2f;
  static final float FIRST_PERSON_FORWARD = 0.1f;
  static final float FIRST_PERSON_PAT_LIFT = 0.08f;
  static final float FIRST_PERSON_PALM_DOWN_DEGREES = 30.0f;
  static final float FIRST_PERSON_PAT_TILT_DEGREES = 12.0f;

  private PatStroke() {}

  public static float reach(final float elapsedTicks) {
    final float linear = PlayerGesture.PAT.ramp(elapsedTicks, REACH_TICKS);
    return linear * linear * (3.0f - 2.0f * linear);
  }

  public static float patLift(final float elapsedTicks) {
    if (!PlayerGesture.PAT.isPlaying(elapsedTicks)) {
      return 0.0f;
    }
    final float cycles = PATS * elapsedTicks / PlayerGesture.PAT.ticks();
    return (1.0f - MathHelper.cos(MathHelper.TAU * cycles)) / 2.0f;
  }

  public static float armPitch(final float restPitch, final float headPitch, final float elapsed) {
    final float aimed = AIM_RADIANS + headPitch - PAT_LIFT_RADIANS * patLift(elapsed);
    return MathHelper.lerp(reach(elapsed), restPitch, aimed);
  }

  public static float armYaw(
      final float restYaw, final float headYaw, final Arm arm, final float elapsed) {
    return MathHelper.lerp(reach(elapsed), restYaw, headYaw - side(arm) * INWARD_YAW_RADIANS);
  }

  public static Vector3f firstPersonShift(final Arm arm, final float elapsed) {
    final float reach = reach(elapsed);
    return new Vector3f(
        -side(arm) * FIRST_PERSON_INWARD * reach,
        (FIRST_PERSON_PAT_LIFT * patLift(elapsed) - FIRST_PERSON_DROP) * reach,
        -FIRST_PERSON_FORWARD * reach);
  }

  public static float firstPersonTiltDegrees(final float elapsed) {
    return (FIRST_PERSON_PALM_DOWN_DEGREES + FIRST_PERSON_PAT_TILT_DEGREES * patLift(elapsed))
        * reach(elapsed);
  }

  private static float side(final Arm arm) {
    return arm == Arm.RIGHT ? 1.0f : -1.0f;
  }
}
