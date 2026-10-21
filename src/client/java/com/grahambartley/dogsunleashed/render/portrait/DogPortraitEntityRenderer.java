package com.grahambartley.dogsunleashed.render.portrait;

import com.grahambartley.dogsunleashed.portrait.DogPortraitEntity;
import com.grahambartley.dogsunleashed.portrait.PortraitBounds;
import com.grahambartley.dogsunleashed.portrait.PortraitSize;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class DogPortraitEntityRenderer extends EntityRenderer<DogPortraitEntity> {

  private static final float HALF_DEPTH = (float) PortraitBounds.THICKNESS / 2.0f;

  public DogPortraitEntityRenderer(final EntityRendererFactory.Context context) {
    super(context);
  }

  @Override
  public void render(
      final DogPortraitEntity entity,
      final float yaw,
      final float tickDelta,
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light) {
    final Sprite back = MinecraftClient.getInstance().getPaintingManager().getBackSprite();
    final Identifier front =
        entity.portrait().map(PortraitTextures.cache()::textureOf).orElse(null);
    final PortraitSize size = entity.size();
    final float right = size.width() / 2.0f;
    final float top = size.height() / 2.0f;
    matrices.push();
    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - yaw));
    final MatrixStack.Entry pose = matrices.peek();
    if (front != null) {
      final VertexConsumer canvas = vertexConsumers.getBuffer(RenderLayer.getEntitySolid(front));
      vertex(pose, canvas, right, -top, -HALF_DEPTH, 0.0f, 1.0f, 0, 0, -1, light);
      vertex(pose, canvas, -right, -top, -HALF_DEPTH, 1.0f, 1.0f, 0, 0, -1, light);
      vertex(pose, canvas, -right, top, -HALF_DEPTH, 1.0f, 0.0f, 0, 0, -1, light);
      vertex(pose, canvas, right, top, -HALF_DEPTH, 0.0f, 0.0f, 0, 0, -1, light);
    }
    final VertexConsumer backs =
        vertexConsumers.getBuffer(RenderLayer.getEntitySolid(back.getAtlasId()));
    if (front == null) {
      face(pose, backs, right, top, -HALF_DEPTH, -1, back, light);
    }
    face(pose, backs, right, top, HALF_DEPTH, 1, back, light);
    edges(pose, backs, right, top, back, light);
    matrices.pop();
    super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
  }

  @Override
  public Identifier getTexture(final DogPortraitEntity entity) {
    return MinecraftClient.getInstance().getPaintingManager().getBackSprite().getAtlasId();
  }

  private static void face(
      final MatrixStack.Entry pose,
      final VertexConsumer consumer,
      final float right,
      final float top,
      final float depth,
      final int normal,
      final Sprite sprite,
      final int light) {
    final float minU = sprite.getMinU();
    final float maxU = sprite.getMaxU();
    final float minV = sprite.getMinV();
    final float maxV = sprite.getMaxV();
    final float near = normal < 0 ? right : -right;
    final float far = -near;
    vertex(pose, consumer, near, -top, depth, minU, maxV, 0, 0, normal, light);
    vertex(pose, consumer, far, -top, depth, maxU, maxV, 0, 0, normal, light);
    vertex(pose, consumer, far, top, depth, maxU, minV, 0, 0, normal, light);
    vertex(pose, consumer, near, top, depth, minU, minV, 0, 0, normal, light);
  }

  private static void edges(
      final MatrixStack.Entry pose,
      final VertexConsumer consumer,
      final float right,
      final float top,
      final Sprite sprite,
      final int light) {
    final float minU = sprite.getMinU();
    final float maxU = sprite.getMaxU();
    final float minV = sprite.getMinV();
    final float edgeV = sprite.getFrameV((float) PortraitBounds.THICKNESS);
    vertex(pose, consumer, right, top, -HALF_DEPTH, minU, minV, 0, 1, 0, light);
    vertex(pose, consumer, -right, top, -HALF_DEPTH, maxU, minV, 0, 1, 0, light);
    vertex(pose, consumer, -right, top, HALF_DEPTH, maxU, edgeV, 0, 1, 0, light);
    vertex(pose, consumer, right, top, HALF_DEPTH, minU, edgeV, 0, 1, 0, light);
    vertex(pose, consumer, right, -top, HALF_DEPTH, minU, minV, 0, -1, 0, light);
    vertex(pose, consumer, -right, -top, HALF_DEPTH, maxU, minV, 0, -1, 0, light);
    vertex(pose, consumer, -right, -top, -HALF_DEPTH, maxU, edgeV, 0, -1, 0, light);
    vertex(pose, consumer, right, -top, -HALF_DEPTH, minU, edgeV, 0, -1, 0, light);
    vertex(pose, consumer, right, top, HALF_DEPTH, minU, minV, -1, 0, 0, light);
    vertex(pose, consumer, right, -top, HALF_DEPTH, minU, edgeV, -1, 0, 0, light);
    vertex(pose, consumer, right, -top, -HALF_DEPTH, maxU, edgeV, -1, 0, 0, light);
    vertex(pose, consumer, right, top, -HALF_DEPTH, maxU, minV, -1, 0, 0, light);
    vertex(pose, consumer, -right, top, -HALF_DEPTH, minU, minV, 1, 0, 0, light);
    vertex(pose, consumer, -right, -top, -HALF_DEPTH, minU, edgeV, 1, 0, 0, light);
    vertex(pose, consumer, -right, -top, HALF_DEPTH, maxU, edgeV, 1, 0, 0, light);
    vertex(pose, consumer, -right, top, HALF_DEPTH, maxU, minV, 1, 0, 0, light);
  }

  private static void vertex(
      final MatrixStack.Entry pose,
      final VertexConsumer consumer,
      final float x,
      final float y,
      final float z,
      final float u,
      final float v,
      final int normalX,
      final int normalY,
      final int normalZ,
      final int light) {
    consumer
        .vertex(pose, x, y, z)
        .color(Colors.WHITE)
        .texture(u, v)
        .overlay(OverlayTexture.DEFAULT_UV)
        .light(light)
        .normal(pose, normalX, normalY, normalZ);
  }
}
