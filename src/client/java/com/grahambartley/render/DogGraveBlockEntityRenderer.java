package com.grahambartley.render;

import com.grahambartley.block.entity.DogGraveBlockEntity;
import com.grahambartley.model.DogGraveModel;
import com.grahambartley.render.layer.DogGraveFlowerLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Camera;
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
        packedOverlay,
        colour);
  }

  // Don't override actuallyRender - let default implementation handle it

  @Override
  public void render(
      DogGraveBlockEntity entity,
      float tickDelta,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      int overlay) {

    final String dogName = entity.getDogName();
    if (dogName != null && !dogName.isEmpty()) {
      renderNameTag(entity, dogName, matrices, vertexConsumers, 15728880); // Max brightness
    }

    super.render(entity, tickDelta, matrices, vertexConsumers, light, overlay);
  }

  private void renderNameTag(
      DogGraveBlockEntity entity,
      String name,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light) {

    final MinecraftClient client = MinecraftClient.getInstance();
    final Camera camera = client.gameRenderer.getCamera();
    final double distance =
        camera
            .getPos()
            .squaredDistanceTo(
                entity.getPos().getX() + 0.5,
                entity.getPos().getY() + 0.5,
                entity.getPos().getZ() + 0.5);

    if (distance > 64.0 * 64.0) return; // Only render within 64 blocks

    matrices.push();

    // Position way above the grave for debugging
    matrices.translate(0.5, 2.0, 0.5);

    // Rotate to face camera
    matrices.multiply(camera.getRotation());
    // Much bigger text for visibility
    matrices.scale(-0.05f, -0.05f, 0.05f);

    final Matrix4f matrix = matrices.peek().getPositionMatrix();
    final TextRenderer textRenderer = client.textRenderer;
    final float backgroundOpacity = client.options.getTextBackgroundOpacity(0.25f);
    final int backgroundColor = (int) (backgroundOpacity * 255.0f) << 24;
    final float xOffset = -textRenderer.getWidth(name) / 2f;

    // Draw shadow/background layer
    textRenderer.draw(
        name,
        xOffset,
        0,
        0x20FFFFFF,
        false,
        matrix,
        vertexConsumers,
        TextRenderer.TextLayerType.SEE_THROUGH,
        backgroundColor,
        light);

    // Draw main text in bright yellow
    textRenderer.draw(
        name,
        xOffset,
        0,
        0xFFFFFF00, // Bright yellow
        false,
        matrix,
        vertexConsumers,
        TextRenderer.TextLayerType.NORMAL,
        0,
        light);

    matrices.pop();
  }
}
