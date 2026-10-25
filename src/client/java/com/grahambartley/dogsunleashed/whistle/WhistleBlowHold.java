package com.grahambartley.dogsunleashed.whistle;

import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

public final class WhistleBlowHold {

  static final float TOWARDS_MOUTH = 0.42f;
  static final float LIFT = 0.12f;
  static final float CLOSER = 0.25f;
  static final float TURN_DEGREES = 20.0f;
  static final float MOUTH_DROP = 0.25f;
  static final float OUT_FROM_FACE = 0.6f;
  static final float TRUE_SIZE = 1.6f;
  static final float POINT_OUT_DEGREES = -90.0f;

  private WhistleBlowHold() {}

  public static void apply(final MatrixStack matrices, final Arm arm, final float lift) {
    final Vector3f shift = shift(arm, lift);
    matrices.translate(shift.x, shift.y, shift.z);
    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(turn(arm, lift)));
  }

  public static void atMouth(final MatrixStack matrices) {
    HeadFeatureRenderer.translate(matrices, false);
    matrices.translate(0.0f, -MOUTH_DROP, -OUT_FROM_FACE);
    matrices.scale(TRUE_SIZE, TRUE_SIZE, TRUE_SIZE);
    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(POINT_OUT_DEGREES));
  }

  static Vector3f shift(final Arm arm, final float lift) {
    return new Vector3f(-side(arm) * TOWARDS_MOUTH * lift, LIFT * lift, CLOSER * lift);
  }

  static float turn(final Arm arm, final float lift) {
    return side(arm) * TURN_DEGREES * lift;
  }

  private static float side(final Arm arm) {
    return arm == Arm.RIGHT ? 1.0f : -1.0f;
  }
}
