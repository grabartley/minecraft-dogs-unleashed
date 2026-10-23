package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import org.joml.Vector3f;

final class PlacedFootprint {

  private PlacedFootprint() {}

  static Vector3f[] of(final String model, final float placedScale) {
    final Vector3f[] bounds = new Vector3f[2];
    FittedGeoItemRenderer.fitOf(
        ShippedRigs.model(model),
        (min, max) -> {
          bounds[0] = PlacedBlockItemPose.of(placedScale).transformPosition(new Vector3f(min));
          bounds[1] = PlacedBlockItemPose.of(placedScale).transformPosition(new Vector3f(max));
          return new ModelFit(1.0f, new Vector3f());
        });
    return bounds;
  }
}
