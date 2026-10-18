package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry.Bounds;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

public final class ToyBounds {

  private ToyBounds() {}

  public static Bounds of(final BakedGeoModel toy) {
    final Bounds bounds = new Bounds();
    for (final GeoBone bone : toy.topLevelBones()) {
      final Bounds part = CubeGeometry.posedBoundsOf(bone);
      if (!part.isEmpty()) {
        bounds.include(part.min());
        bounds.include(part.max());
      }
    }
    return bounds;
  }
}
