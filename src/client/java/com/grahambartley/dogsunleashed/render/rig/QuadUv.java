package com.grahambartley.dogsunleashed.render.rig;

import java.util.Optional;
import org.joml.Vector3f;

public final class QuadUv {

  private static final float DEGENERATE = 1.0e-9f;

  private QuadUv() {}

  public static Optional<Vector3f> pointAt(
      final CubeGeometry.Face face, final float u, final float v) {
    final float du1 = face.us()[1] - face.us()[0];
    final float dv1 = face.vs()[1] - face.vs()[0];
    final float du3 = face.us()[3] - face.us()[0];
    final float dv3 = face.vs()[3] - face.vs()[0];
    final float determinant = du1 * dv3 - du3 * dv1;
    if (Math.abs(determinant) < DEGENERATE) {
      return Optional.empty();
    }
    final float tu = u - face.us()[0];
    final float tv = v - face.vs()[0];
    final float a = (tu * dv3 - du3 * tv) / determinant;
    final float b = (du1 * tv - tu * dv1) / determinant;
    final Vector3f origin = face.corners()[0];
    final Vector3f alongFirst = new Vector3f(face.corners()[1]).sub(origin).mul(a);
    final Vector3f alongSecond = new Vector3f(face.corners()[3]).sub(origin).mul(b);
    return Optional.of(new Vector3f(origin).add(alongFirst).add(alongSecond));
  }

  public record Span(float min, float max) {

    public boolean strictlyContains(final float value) {
      return value > this.min && value < this.max;
    }
  }

  public static boolean covers(final CubeGeometry.Face face, final float u, final float v) {
    return spanOf(face.us()).strictlyContains(u) && spanOf(face.vs()).strictlyContains(v);
  }

  public static Span spanOf(final float[] coordinates) {
    float min = Float.POSITIVE_INFINITY;
    float max = Float.NEGATIVE_INFINITY;
    for (final float coordinate : coordinates) {
      min = Math.min(min, coordinate);
      max = Math.max(max, coordinate);
    }
    return new Span(min, max);
  }
}
