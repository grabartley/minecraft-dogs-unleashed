package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.block.DogGraveBlock;
import com.grahambartley.dogsunleashed.block.entity.DogGraveBlockEntity;
import com.grahambartley.dogsunleashed.model.DogGraveModel;
import com.grahambartley.dogsunleashed.render.layer.DogGraveFlowerLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class DogGraveBlockEntityRenderer extends GeoBlockRenderer<DogGraveBlockEntity> {

  private static final float GRAVE_SCALE = 2.0f;
  private static final float NAME_TAG_HEIGHT = 2.2f;
  private static final float NAME_TAG_TEXT_SCALE = 0.025f;
  private static final ItemStack TOTEM_STACK = new ItemStack(Items.TOTEM_OF_UNDYING);
  private static final float TOTEM_SCALE = 0.7f;
  private static final float TOTEM_LEAN_DEGREES = 20.0f;
  private static final double TOTEM_BASE_HEIGHT = 0.3;
  private static final double TOTEM_OFFSET_FROM_CENTRE = 0.32;

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
  public void render(
      DogGraveBlockEntity entity,
      float partialTick,
      MatrixStack matrices,
      VertexConsumerProvider bufferSource,
      int packedLight,
      int packedOverlay) {
    super.render(entity, partialTick, matrices, bufferSource, packedLight, packedOverlay);

    if (entity.hasTotem()) {
      renderTotem(entity, matrices, bufferSource, packedLight, packedOverlay);
    }

    final String dogName = entity.getDogName();
    if (dogName != null && !dogName.isEmpty()) {
      renderNameTag(
          entity, dogName, entity.getFlowerColor().getEntityColor(), matrices, packedLight);
    }
  }

  /** The offering leans against the front of the headstone, at the foot of the grave. */
  private void renderTotem(
      DogGraveBlockEntity entity,
      MatrixStack matrices,
      VertexConsumerProvider bufferSource,
      int packedLight,
      int packedOverlay) {
    final Direction facing = entity.getCachedState().get(DogGraveBlock.FACING);
    matrices.push();
    matrices.translate(0.5, TOTEM_BASE_HEIGHT, 0.5);
    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-facing.asRotation()));
    matrices.translate(0.0, 0.0, TOTEM_OFFSET_FROM_CENTRE);
    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(TOTEM_LEAN_DEGREES));
    matrices.scale(TOTEM_SCALE, TOTEM_SCALE, TOTEM_SCALE);
    MinecraftClient.getInstance()
        .getItemRenderer()
        .renderItem(
            TOTEM_STACK,
            ModelTransformationMode.FIXED,
            packedLight,
            packedOverlay,
            matrices,
            bufferSource,
            entity.getWorld(),
            0);
    matrices.pop();
  }

  private void renderNameTag(
      DogGraveBlockEntity entity, String name, int textColor, MatrixStack matrices, int light) {
    final MinecraftClient client = MinecraftClient.getInstance();

    // Only render when crosshair is on this grave
    if (!(client.crosshairTarget instanceof BlockHitResult hit)) return;
    if (!hit.getBlockPos().equals(entity.getPos())) return;

    final VertexConsumerProvider.Immediate immediate =
        client.getBufferBuilders().getEntityVertexConsumers();
    final TextRenderer textRenderer = client.textRenderer;
    final int backgroundColor =
        (int) (client.options.getTextBackgroundOpacity(0.25f) * 255.0f) << 24;

    matrices.push();
    matrices.translate(0.5, NAME_TAG_HEIGHT, 0.5);
    matrices.multiply(client.getEntityRenderDispatcher().getRotation());
    matrices.scale(NAME_TAG_TEXT_SCALE, -NAME_TAG_TEXT_SCALE, NAME_TAG_TEXT_SCALE);

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
    immediate.draw();
    matrices.pop();
  }
}
