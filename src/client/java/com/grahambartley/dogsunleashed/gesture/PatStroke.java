package com.grahambartley.dogsunleashed.gesture;

import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

public final class PatStroke {

  static final float REACH_TICKS = 5.0f;
  static final int PATS = 3;
  static final float PAT_LIFT_RADIANS = 0.3f;
  static final float FIRST_PERSON_INWARD = 0.5f;
  static final float FIRST_PERSON_RISE = 0.35f;
  static final float FIRST_PERSON_FORWARD = 0.3f;
  static final float FIRST_PERSON_PAT_LIFT = 0.08f;

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

  public static float liftRadians(final float elapsedTicks) {
    return PAT_LIFT_RADIANS * patLift(elapsedTicks);
  }

  public static Vector3f firstPersonShift(final Arm arm, final float elapsed) {
    final float reach = reach(elapsed);
    return new Vector3f(
        -side(arm) * FIRST_PERSON_INWARD * reach,
        (FIRST_PERSON_RISE + FIRST_PERSON_PAT_LIFT * patLift(elapsed)) * reach,
        -FIRST_PERSON_FORWARD * reach);
  }

  static float side(final Arm arm) {
    return arm == Arm.RIGHT ? 1.0f : -1.0f;
  }
}
