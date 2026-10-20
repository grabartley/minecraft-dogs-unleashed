package com.grahambartley.dogsunleashed.render.cosmetic;

import com.grahambartley.dogsunleashed.item.cosmetic.MobHead;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import org.joml.Vector3f;

public final class MobHeadShape {

  private static final float SKULL_PIXELS = 8.0f;

  private MobHeadShape() {}

  public static CubeGeometry.Bounds footprint(final MobHead head) {
    return switch (head) {
      case PIGLIN -> pixels(-8.5f, 0.0f, -5.0f, 8.5f, 8.0f, 4.0f);
      case DRAGON -> pixels(-6.0f, -4.5f, -18.0f, 6.0f, 15.0f, 4.5f);
      case ZOMBIE -> pixels(-4.25f, -0.25f, -4.25f, 4.25f, 8.25f, 4.25f);
      case SKELETON, WITHER_SKELETON, CREEPER, CARVED_PUMPKIN ->
          pixels(-4.0f, 0.0f, -4.0f, 4.0f, 8.0f, 4.0f);
    };
  }

  public static CubeGeometry.Bounds placed(
      final MobHead head, final Vector3f base, final float edge, final float depthStretch) {
    final CubeGeometry.Bounds unit = footprint(head);
    final Vector3f scale = new Vector3f(edge, edge, edge * depthStretch);
    final CubeGeometry.Bounds placed = new CubeGeometry.Bounds();
    placed.include(unit.min().mul(scale).add(base));
    placed.include(unit.max().mul(scale).add(base));
    return placed;
  }

  private static CubeGeometry.Bounds pixels(
      final float minX,
      final float minY,
      final float minZ,
      final float maxX,
      final float maxY,
      final float maxZ) {
    final CubeGeometry.Bounds bounds = new CubeGeometry.Bounds();
    bounds.include(new Vector3f(minX, minY, minZ).div(SKULL_PIXELS));
    bounds.include(new Vector3f(maxX, maxY, maxZ).div(SKULL_PIXELS));
    return bounds;
  }
}
