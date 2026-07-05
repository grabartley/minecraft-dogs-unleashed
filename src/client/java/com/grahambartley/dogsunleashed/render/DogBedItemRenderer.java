package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.block.entity.DogBedBlockEntity;
import com.grahambartley.dogsunleashed.model.DogBedModel;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class DogBedItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

  private final GeoBlockRenderer<DogBedBlockEntity> renderer;
  private final DogBedBlockEntity dummyEntity;

  public DogBedItemRenderer() {
    this.dummyEntity = new DogBedBlockEntity(BlockPos.ORIGIN, ModBlocks.DOG_BED.getDefaultState());
    this.renderer = new GeoBlockRenderer<>(new DogBedModel());
  }

  @Override
  public void render(
      ItemStack stack,
      ModelTransformationMode mode,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      int overlay) {

    DyeColor color = stack.getOrDefault(ModComponents.DOG_BED_COLOR, DyeColor.WHITE);
    dummyEntity.setColor(color);

    final int tintColor = color.getEntityColor() | 0xFF000000;

    matrices.push();

    switch (mode) {
      case GUI -> {
        ItemRenderTransforms.applyDisplayPose(matrices, 0.5, 0.3, 0.0, 30.0f, 45.0f, 1.2f);
      }
      case GROUND -> ItemRenderTransforms.applyGroundPose(matrices, 0.5, 0.2, 0.5, 0.8f);
      case FIXED -> {
        ItemRenderTransforms.applyDisplayPose(matrices, 0.5, 0.2, 0.5, 30.0f, 45.0f, 1.0f);
      }
      case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
        ItemRenderTransforms.applyDisplayPose(matrices, 0.5, 0.35, 0.5, 75.0f, 45.0f, 0.8f);
      }
      case FIRST_PERSON_LEFT_HAND -> {
        ItemRenderTransforms.applyFirstPersonPose(
            matrices, 0.6, 0.25, 0.3, 10.0f, -80.0f, -10.0f, 0.6f);
      }
      case FIRST_PERSON_RIGHT_HAND -> {
        ItemRenderTransforms.applyFirstPersonPose(
            matrices, 0.4, 0.25, 0.3, 10.0f, 80.0f, 10.0f, 0.6f);
      }
      default -> ItemRenderTransforms.applyGroundPose(matrices, 0.5, 0.2, 0.5, 0.8f);
    }

    BakedGeoModel bakedModel =
        renderer.getGeoModel().getBakedModel(renderer.getGeoModel().getModelResource(dummyEntity));
    RenderLayer renderType =
        renderer.getRenderType(
            dummyEntity, renderer.getTextureLocation(dummyEntity), vertexConsumers, 1.0f);
    var buffer = vertexConsumers.getBuffer(renderType);

    renderer.actuallyRender(
        matrices,
        dummyEntity,
        bakedModel,
        renderType,
        vertexConsumers,
        buffer,
        false,
        1.0f,
        light,
        overlay,
        tintColor);

    matrices.pop();
  }
}
