package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

final class PlacedFootprint {

  private PlacedFootprint() {}

  static Vector3f[] of(final String model, final float placedScale) {
    final CubeGeometry.Bounds bounds = FittedGeoItemRenderer.boundsOf(ShippedRigs.model(model));
    final MatrixStack stack = new MatrixStack();
    PlacedBlockItemPose.apply(stack, placedScale);
    final Matrix4f pose = stack.peek().getPositionMatrix();
    return new Vector3f[] {
      pose.transformPosition(bounds.min()), pose.transformPosition(bounds.max())
    };
  }
}
