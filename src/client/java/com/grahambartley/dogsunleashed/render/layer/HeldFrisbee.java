package com.grahambartley.dogsunleashed.render.layer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class HeldFrisbee {

  static final float HEAD_WIDTHS = 1.25f;
  static final float NOSE_DOWN_DEGREES = 15.0f;
  private static final Vector3f UP = new Vector3f(0.0f, 1.0f, 0.0f);

  private HeldFrisbee() {}

  public static Matrix4f gripToDisc(final MouthGrip grip, final float modelDiameter) {
    final float diameter = HEAD_WIDTHS * grip.headWidth();
    final Vector3f forward = grip.forward();
    return new Matrix4f()
        .rotate((float) Math.toRadians(NOSE_DOWN_DEGREES), new Vector3f(UP).cross(forward))
        .translate(forward.mul(diameter / 2.0f))
        .scale(diameter / modelDiameter);
  }
}
