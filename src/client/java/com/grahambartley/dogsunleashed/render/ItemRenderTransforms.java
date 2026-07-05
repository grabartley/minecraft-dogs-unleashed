package com.grahambartley.dogsunleashed.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

final class ItemRenderTransforms {
  private ItemRenderTransforms() {}

  static void applyDisplayPose(
      final MatrixStack matrices,
      final double x,
      final double y,
      final double z,
      final float xRotation,
      final float yRotation,
      final float scale) {
    matrices.translate(x, y, z);
    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(xRotation));
    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yRotation));
    matrices.scale(scale, scale, scale);
  }

  static void applyGroundPose(
      final MatrixStack matrices,
      final double x,
      final double y,
      final double z,
      final float scale) {
    matrices.translate(x, y, z);
    matrices.scale(scale, scale, scale);
  }

  static void applyFirstPersonPose(
      final MatrixStack matrices,
      final double x,
      final double y,
      final double z,
      final float xRotation,
      final float yRotation,
      final float zRotation,
      final float scale) {
    matrices.translate(x, y, z);
    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(xRotation));
    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yRotation));
    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(zRotation));
    matrices.scale(scale, scale, scale);
  }
}
