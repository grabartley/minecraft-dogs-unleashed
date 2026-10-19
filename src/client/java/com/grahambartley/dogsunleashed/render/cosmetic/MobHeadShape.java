package com.grahambartley.dogsunleashed.render.cosmetic;

import com.grahambartley.dogsunleashed.item.cosmetic.MobHead;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import org.joml.Vector3f;

public final class MobHeadShape {

  private MobHeadShape() {}

  public static CubeGeometry.Bounds footprint(final MobHead head) {
    return switch (head) {
      case PIGLIN -> box(-1.0625f, 0.0f, -0.625f, 1.0625f, 1.0f, 0.5f);
      case DRAGON -> box(-0.75f, -0.5625f, -2.25f, 0.75f, 1.875f, 0.5625f);
      case ZOMBIE -> box(-0.53125f, -0.03125f, -0.53125f, 0.53125f, 1.03125f, 0.53125f);
      case SKELETON, WITHER_SKELETON, CREEPER, CARVED_PUMPKIN ->
          box(-0.5f, 0.0f, -0.5f, 0.5f, 1.0f, 0.5f);
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

  private static CubeGeometry.Bounds box(
      final float minX,
      final float minY,
      final float minZ,
      final float maxX,
      final float maxY,
      final float maxZ) {
    final CubeGeometry.Bounds bounds = new CubeGeometry.Bounds();
    bounds.include(new Vector3f(minX, minY, minZ));
    bounds.include(new Vector3f(maxX, maxY, maxZ));
    return bounds;
  }
}
