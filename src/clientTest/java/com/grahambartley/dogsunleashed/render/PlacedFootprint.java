package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import org.joml.Matrix4f;
import org.joml.Vector3f;

final class PlacedFootprint {

  private PlacedFootprint() {}

  static Vector3f[] of(final String model, final float placedScale) {
    final CubeGeometry.Bounds bounds = FittedGeoItemRenderer.boundsOf(ShippedRigs.model(model));
    final Matrix4f pose = PlacedBlockItemPose.of(placedScale);
    return new Vector3f[] {
      pose.transformPosition(bounds.min()), pose.transformPosition(bounds.max())
    };
  }
}
