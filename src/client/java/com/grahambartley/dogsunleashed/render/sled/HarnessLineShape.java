package com.grahambartley.dogsunleashed.render.sled;

import org.joml.Vector3f;

public final class HarnessLineShape {

  public static final int SEGMENTS = 24;

  private HarnessLineShape() {}

  public static Vector3f pointAt(final Vector3f span, final int segment) {
    final float t = segment / (float) SEGMENTS;
    final float y = span.y > 0.0f ? span.y * t * t : span.y - span.y * (1.0f - t) * (1.0f - t);
    return new Vector3f(span.x * t, y, span.z * t);
  }
}
