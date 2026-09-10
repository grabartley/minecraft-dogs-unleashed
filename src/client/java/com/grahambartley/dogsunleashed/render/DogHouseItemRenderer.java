package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.block.entity.DogHouseBlockEntity;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class DogHouseItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

  private static final double CENTRE_X = 0.0;

  private static final double CENTRE_Y = 1.0;

  private static final double CENTRE_Z = 0.0;

  private final CushionTintedDogHouseRenderer renderer;
  private final DogHouseBlockEntity dummyEntity;

  public DogHouseItemRenderer() {
    this.dummyEntity =
        new DogHouseBlockEntity(BlockPos.ORIGIN, ModBlocks.DOG_HOUSE.getDefaultState());
    this.renderer = new CushionTintedDogHouseRenderer();
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
