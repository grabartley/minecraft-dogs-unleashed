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

  /**
   * Poses a model drawn by a {@code GeoItemRenderer}. That renderer translates the model half a
   * block on every axis from inside this transform, so without the trailing translate to cancel it
   * the prop swings around the block corner as soon as any rotation is applied.
   *
   * <p>The leading translate is absolute, not an offset from centre: the display modes want the
   * model at the middle of the block, but the in-hand modes are already positioned at the hand and
   * a half-block push there drives the model into the camera.
   */
  static void applyGeoItemPose(
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
    matrices.translate(-0.5, -0.51, -0.5);
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
