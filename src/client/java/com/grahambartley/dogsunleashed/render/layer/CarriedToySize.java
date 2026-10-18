package com.grahambartley.dogsunleashed.render.layer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class CarriedToySize {

  private CarriedToySize() {}

  public static float frameScale(final Matrix4f frame) {
    return frame.getScale(new Vector3f()).x;
  }

  public static float scaleInFrame(
      final float adultToyScale, final float adultModelScale, final float frameScale) {
    return adultToyScale / (float) Math.sqrt(frameScale / adultModelScale);
  }
}
