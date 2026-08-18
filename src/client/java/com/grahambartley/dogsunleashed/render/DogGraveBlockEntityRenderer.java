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

  // Sized so the headstone stands inside its own block cell, which is what lets its whole height
  // be aimed at, with the very tip crossing into the cell above so a lightning rod placed there
  // has its base buried in the stone rather than hovering over it.
  private static final float GRAVE_SCALE = 1.285f;
  // Just clear of the headstone's top; it tracks the stone's height, not the block's.
  private static final float NAME_TAG_HEIGHT = 1.32f;
  private static final float NAME_TAG_TEXT_SCALE = 0.025f;
  private static final ItemStack TOTEM_STACK = new ItemStack(Items.TOTEM_OF_UNDYING);
  private static final float TOTEM_SCALE = 0.45f;
  private static final float TOTEM_LEAN_DEGREES = -24.0f;
  // Negative tips the totem's top toward the stone's centre, so it leans in, not out.
  private static final float TOTEM_SIDE_TILT_DEGREES = -8.0f;
  private static final double TOTEM_BASE_HEIGHT = 0.218;
  private static final double TOTEM_SIDE_OFFSET = -0.154;
  private static final double TOTEM_OFFSET_FROM_CENTRE = 0.16;

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
    matrices.translate(TOTEM_SIDE_OFFSET, 0.0, TOTEM_OFFSET_FROM_CENTRE);
    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(TOTEM_LEAN_DEGREES));
    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(TOTEM_SIDE_TILT_DEGREES));
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

    // Only render when the crosshair is on this grave
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
