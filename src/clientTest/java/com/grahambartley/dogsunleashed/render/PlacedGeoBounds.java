package com.grahambartley.dogsunleashed.render;

import net.minecraft.util.math.Box;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

final class PlacedGeoBounds {

  private static final double BLOCK_CENTRE = 0.5;

  private PlacedGeoBounds() {}

  static Box of(final BakedGeoModel model, final float scale) {
    final double[] min = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
    final double[] max = {-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
    for (final GeoBone bone : model.topLevelBones()) {
      collect(bone, min, max);
    }
    return new Box(
        BLOCK_CENTRE + min[0] * scale,
        min[1] * scale,
        BLOCK_CENTRE + min[2] * scale,
        BLOCK_CENTRE + max[0] * scale,
        max[1] * scale,
        BLOCK_CENTRE + max[2] * scale);
  }

  private static void collect(final GeoBone bone, final double[] min, final double[] max) {
    for (final GeoCube cube : bone.getCubes()) {
      for (final GeoQuad quad : cube.quads()) {
        if (quad == null) {
          continue;
        }
        for (final GeoVertex vertex : quad.vertices()) {
          final float[] p = {vertex.position().x, vertex.position().y, vertex.position().z};
          for (int axis = 0; axis < 3; axis++) {
            min[axis] = Math.min(min[axis], p[axis]);
            max[axis] = Math.max(max[axis], p[axis]);
          }
        }
      }
    }
    for (final GeoBone child : bone.getChildBones()) {
      collect(child, min, max);
    }
  }
}
