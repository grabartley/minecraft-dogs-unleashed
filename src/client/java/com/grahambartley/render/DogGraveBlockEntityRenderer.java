package com.grahambartley.render;

import com.grahambartley.block.entity.DogGraveBlockEntity;
import com.grahambartley.model.DogGraveModel;
import com.grahambartley.render.layer.DogGraveFlowerLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class DogGraveBlockEntityRenderer extends GeoBlockRenderer<DogGraveBlockEntity> {

  private static final float GRAVE_SCALE = 2.0f;

  public DogGraveBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    super(new DogGraveModel());
    addRenderLayer(new DogGraveFlowerLayer(this));
  }

  @Override
  public void preRender(
      MatrixStack poseStack,
      DogGraveBlockEntity animatable,
      BakedGeoModel model,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour) {
    // Only scale on initial render, not when flower layer reRenders
    if (!isReRender) {
      poseStack.translate(0.5, 0, 0.5);
      poseStack.scale(GRAVE_SCALE, GRAVE_SCALE, GRAVE_SCALE);
      poseStack.translate(-0.5, 0, -0.5);
    }
    super.preRender(
        poseStack,
        animatable,
        model,
        bufferSource,
        buffer,
        isReRender,
        partialTick,
        packedLight,
        packedOverlay,
        colour);
  }

  @Override
  public void actuallyRender(
      MatrixStack matrices,
      DogGraveBlockEntity entity,
      BakedGeoModel model,
      RenderLayer renderType,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour) {

    super.actuallyRender(
        matrices,
        entity,
        model,
        renderType,
        bufferSource,
        buffer,
        isReRender,
        partialTick,
        packedLight,
        packedOverlay,
        colour);

    if (!isReRender) {
      final String dogName = entity.getDogName();
      final int textColor = entity.getFlowerColor().getEntityColor();
      if (dogName != null && !dogName.isEmpty()) {
        renderNameTag(entity, dogName, textColor, matrices, packedLight);
      }
    }
  }

  private void renderNameTag(
      DogGraveBlockEntity entity, String name, int textColor, MatrixStack matrices, int light) {
    final MinecraftClient client = MinecraftClient.getInstance();
    final VertexConsumerProvider.Immediate immediate =
        client.getBufferBuilders().getEntityVertexConsumers();
    final TextRenderer textRenderer = client.textRenderer;
    final int backgroundColor =
        (int) (client.options.getTextBackgroundOpacity(0.25f) * 255.0f) << 24;
    matrices.push();
    matrices.translate(0.0, 1.1, 0.0);
    matrices.scale(-0.02f, -0.02f, 0.02f); // scale down to block space
    final Matrix4f matrix = matrices.peek().getPositionMatrix();
    final float xOffset = -textRenderer.getWidth(name) / 2f;
    textRenderer.draw(
        name,
        xOffset,
        0,
        textColor,
        false,
        matrix,
        immediate,
        TextRenderer.TextLayerType.NORMAL,
        backgroundColor,
        light);
    matrices.pop();
  }
}
