package com.grahambartley.dogsunleashed.render.bend;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;

public final class LimbBend {

  static final float EPSILON = 1.0e-4f;

  public record Corner(Vector3f pos, float u, float v) {}

  private LimbBend() {}

  public static List<Corner[]> split(final Corner[] quad, final float joint) {
    boolean above = false;
    boolean below = false;
    for (final Corner corner : quad) {
      above |= corner.pos().y < joint - EPSILON;
      below |= corner.pos().y > joint + EPSILON;
    }
    if (!above || !below) {
      return List.<Corner[]>of(quad);
    }
    return List.of(clip(quad, joint, true), clip(quad, joint, false));
  }

  public static boolean isLower(final Corner[] quad, final float joint) {
    for (final Corner corner : quad) {
      if (corner.pos().y > joint + EPSILON) {
        return true;
      }
    }
    return false;
  }

  public static Vector3f bend(final Vector3f point, final float joint, final float angle) {
    final float down = point.y - joint;
    if (down < -EPSILON) {
      return new Vector3f(point);
    }
    if (down > EPSILON) {
      final float cos = (float) Math.cos(angle);
      final float sin = (float) Math.sin(angle);
      return new Vector3f(point.x, joint + down * cos - point.z * sin, down * sin + point.z * cos);
    }
    final float half = angle / 2.0f;
    final float mitre = point.z / (float) Math.cos(half);
    return new Vector3f(
        point.x, joint - mitre * (float) Math.sin(half), mitre * (float) Math.cos(half));
  }

  public static Vector3f bendNormal(final Vector3f normal, final boolean lower, final float angle) {
    return lower ? new Vector3f(normal).rotateX(angle) : new Vector3f(normal);
  }

  private static Corner[] clip(final Corner[] quad, final float joint, final boolean keepAbove) {
    final List<Corner> kept = new ArrayList<>(5);
    for (int i = 0; i < quad.length; i++) {
      final Corner from = quad[i];
      final Corner to = quad[(i + 1) % quad.length];
      final boolean fromIn = keepAbove ? from.pos().y <= joint : from.pos().y >= joint;
      final boolean toIn = keepAbove ? to.pos().y <= joint : to.pos().y >= joint;
      if (fromIn) {
        kept.add(from);
      }
      if (fromIn != toIn) {
        final float t = (joint - from.pos().y) / (to.pos().y - from.pos().y);
        kept.add(
            new Corner(
                new Vector3f(from.pos()).lerp(to.pos(), t).setComponent(1, joint),
                from.u() + (to.u() - from.u()) * t,
                from.v() + (to.v() - from.v()) * t));
      }
    }
    while (kept.size() < quad.length) {
      kept.add(kept.get(kept.size() - 1));
    }
    return kept.subList(0, quad.length).toArray(new Corner[0]);
  }
}
