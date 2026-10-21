package com.grahambartley.dogsunleashed.render.sled;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.joml.Vector3f;

public final class HarnessEyes {

  private HarnessEyes() {}

  public static List<Vector3f> assign(final List<Vector3f> eyes, final List<Double> dogSides) {
    final List<Vector3f> left = sideOf(eyes, true);
    final List<Vector3f> right = sideOf(eyes, false);
    final List<Vector3f> centre = new ArrayList<>(eyes);
    centre.sort(Comparator.comparingDouble(eye -> Math.abs(eye.x)));
    final List<Vector3f> assigned = new ArrayList<>();
    int nextLeft = 0;
    int nextRight = 0;
    for (final double side : dogSides) {
      final Vector3f eye;
      if (side > 0.0 && nextLeft < left.size()) {
        eye = left.get(nextLeft++);
      } else if (side < 0.0 && nextRight < right.size()) {
        eye = right.get(nextRight++);
      } else {
        eye = centre.isEmpty() ? new Vector3f() : centre.get(0);
      }
      assigned.add(new Vector3f(eye));
    }
    return assigned;
  }

  private static List<Vector3f> sideOf(final List<Vector3f> eyes, final boolean left) {
    final List<Vector3f> side = new ArrayList<>();
    for (final Vector3f eye : eyes) {
      if (left ? eye.x > 0.0f : eye.x < 0.0f) {
        side.add(eye);
      }
    }
    side.sort(SledAnchors.outermostFirst());
    return side;
  }
}
