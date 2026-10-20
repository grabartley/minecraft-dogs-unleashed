package com.grahambartley.dogsunleashed.render.pawprint;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

final class PawPrintRenderer {

  static final Identifier TEXTURE = Identifier.of(MOD_ID, "textures/misc/paw_print.png");
  private static final float LIFT = 0.015f;

  private PawPrintRenderer() {}

  static void render(final WorldRenderContext context, final PawPrintTrail trail) {
    final MatrixStack matrices = context.matrixStack();
    final VertexConsumerProvider consumers = context.consumers();
    if (matrices == null || consumers == null || context.world() == null) {
      return;
    }
    final Vec3d camera = context.camera().getPos();
    final VertexConsumer buffer = consumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));
    for (final PawPrintTrail.Print print : trail.prints()) {
      final double dx = print.x() - camera.x;
      final double dy = print.y() - camera.y;
      final double dz = print.z() - camera.z;
      if (!PawPrintRules.inRange(dx * dx + dy * dy + dz * dz)) {
        continue;
      }
      final float alpha = PawPrintRules.alpha(trail.tick() - print.bornTick());
      final int light =
          WorldRenderer.getLightmapCoordinates(
              context.world(), BlockPos.ofFloored(print.x(), print.y() + LIFT, print.z()));
      matrices.push();
      matrices.translate(dx, dy + LIFT, dz);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - print.yaw()));
      matrices.translate(print.sideOffset(), 0.0, 0.0);
      quad(buffer, matrices.peek(), print.size() / 2.0f, alpha, light);
      matrices.pop();
    }
  }

  private static void quad(
      final VertexConsumer buffer,
      final MatrixStack.Entry entry,
      final float half,
      final float alpha,
      final int light) {
    final Matrix4f pose = entry.getPositionMatrix();
    vertex(buffer, entry, pose, -half, -half, 0.0f, 0.0f, alpha, light);
    vertex(buffer, entry, pose, -half, half, 0.0f, 1.0f, alpha, light);
    vertex(buffer, entry, pose, half, half, 1.0f, 1.0f, alpha, light);
    vertex(buffer, entry, pose, half, -half, 1.0f, 0.0f, alpha, light);
  }

  private static void vertex(
      final VertexConsumer buffer,
      final MatrixStack.Entry entry,
      final Matrix4f pose,
      final float x,
      final float z,
      final float u,
      final float v,
      final float alpha,
      final int light) {
    buffer
        .vertex(pose, x, 0.0f, z)
        .color(1.0f, 1.0f, 1.0f, alpha)
        .texture(u, v)
        .overlay(OverlayTexture.DEFAULT_UV)
        .light(light)
        .normal(entry, 0.0f, 1.0f, 0.0f);
  }
}
