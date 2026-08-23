package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.block.entity.DogHouseBlockEntity;
import com.grahambartley.dogsunleashed.model.DogHouseModel;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/**
 * The house model is two blocks on every axis, so each pose has to both scale for a two-block prop
 * and pull the model back by its own centre. Miss either and the house renders oversized and
 * hanging out of its slot.
 *
 * <p>The cushion is tinted here as well as in the world, so the slot icon shows the colour the
 * house will actually be once it is placed.
 */
public class DogHouseItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

  /**
   * The model is authored centred on its footprint, so its middle sits at x=0, z=0 and one block
   * up. Every pose below turns about that point rather than about the corner the model starts from.
   */
  private static final double CENTRE_X = 0.0;

  private static final double CENTRE_Y = 1.0;

  private static final double CENTRE_Z = 0.0;

  private final GeoBlockRenderer<DogHouseBlockEntity> renderer;
  private final DogHouseBlockEntity dummyEntity;

  public DogHouseItemRenderer() {
    this.dummyEntity =
        new DogHouseBlockEntity(BlockPos.ORIGIN, ModBlocks.DOG_HOUSE.getDefaultState());
    this.renderer =
        new GeoBlockRenderer<>(new DogHouseModel()) {
          @Override
          public void renderRecursively(
              MatrixStack poseStack,
              DogHouseBlockEntity animatable,
              GeoBone bone,
              RenderLayer renderType,
              VertexConsumerProvider bufferSource,
              VertexConsumer buffer,
              boolean isReRender,
              float partialTick,
              int packedLight,
              int packedOverlay,
              int colour) {
            super.renderRecursively(
                poseStack,
                animatable,
                bone,
                renderType,
                bufferSource,
                buffer,
                isReRender,
                partialTick,
                packedLight,
                packedOverlay,
                DogHouseCushionTint.forBone(bone.getName(), animatable.getColor(), colour));
          }
        };
  }

  @Override
  public void render(
      ItemStack stack,
      ModelTransformationMode mode,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      int overlay) {

    dummyEntity.setColor(stack.getOrDefault(ModComponents.DOG_HOUSE_COLOR, DyeColor.WHITE));

    matrices.push();

    switch (mode) {
      case GUI -> centredPose(matrices, 0.5, 0.52, 0.0, 25.0f, 200.0f, 0.27f);
      case GROUND -> centredPose(matrices, 0.5, 0.3, 0.5, 0.0f, 0.0f, 0.20f);
      case FIXED -> centredPose(matrices, 0.5, 0.5, 0.5, 0.0f, 0.0f, 0.26f);
      case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND ->
          centredPose(matrices, 0.5, 0.45, 0.5, 20.0f, 200.0f, 0.16f);
      case FIRST_PERSON_LEFT_HAND -> centredPose(matrices, 0.45, 0.6, 0.4, 0.0f, 160.0f, 0.09f);
      case FIRST_PERSON_RIGHT_HAND -> centredPose(matrices, 0.55, 0.6, 0.4, 0.0f, 200.0f, 0.09f);
      default -> centredPose(matrices, 0.5, 0.3, 0.5, 0.0f, 0.0f, 0.20f);
    }

    final BakedGeoModel bakedModel =
        renderer.getGeoModel().getBakedModel(renderer.getGeoModel().getModelResource(dummyEntity));
    final RenderLayer renderType =
        renderer.getRenderType(
            dummyEntity, renderer.getTextureLocation(dummyEntity), vertexConsumers, 1.0f);

    renderer.actuallyRender(
        matrices,
        dummyEntity,
        bakedModel,
        renderType,
        vertexConsumers,
        vertexConsumers.getBuffer(renderType),
        false,
        1.0f,
        light,
        overlay,
        0xFFFFFFFF);

    matrices.pop();
  }

  /** Poses the house about its own middle rather than about the corner its model starts from. */
  private static void centredPose(
      final MatrixStack matrices,
      final double x,
      final double y,
      final double z,
      final float xRotation,
      final float yRotation,
      final float scale) {
    ItemRenderTransforms.applyDisplayPose(matrices, x, y, z, xRotation, yRotation, scale);
    matrices.translate(-CENTRE_X, -CENTRE_Y, -CENTRE_Z);
  }
}
