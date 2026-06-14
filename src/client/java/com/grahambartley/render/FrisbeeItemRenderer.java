package com.grahambartley.render;

import com.grahambartley.ModBlocks;
import com.grahambartley.ModComponents;
import com.grahambartley.block.entity.FrisbeeBlockEntity;
import com.grahambartley.model.FrisbeeModel;
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

public class FrisbeeItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

  private final GeoBlockRenderer<FrisbeeBlockEntity> renderer;
  private final FrisbeeBlockEntity dummyEntity;

  public FrisbeeItemRenderer() {
    this.dummyEntity = new FrisbeeBlockEntity(BlockPos.ORIGIN, ModBlocks.FRISBEE.getDefaultState());
    this.renderer = new GeoBlockRenderer<>(new FrisbeeModel());
  }

  @Override
  public void render(
      ItemStack stack,
      ModelTransformationMode mode,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      int overlay) {

    DyeColor color = stack.getOrDefault(ModComponents.FRISBEE_COLOR, DyeColor.WHITE);
    dummyEntity.setColor(color);
    int tintColor = color.getEntityColor() | 0xFF000000;

    matrices.push();

    switch (mode) {
      case GUI -> {
        ItemRenderTransforms.applyDisplayPose(matrices, 0.5, 0.45, 0.0, 25.0f, 45.0f, 0.4f);
      }
      case GROUND -> ItemRenderTransforms.applyGroundPose(matrices, 0.5, 0.18, 0.5, 0.28f);
      case FIXED -> {
        ItemRenderTransforms.applyDisplayPose(matrices, 0.5, 0.35, 0.5, 25.0f, 45.0f, 0.35f);
      }
      case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
        ItemRenderTransforms.applyDisplayPose(matrices, 0.5, 0.42, 0.5, 75.0f, 45.0f, 0.45f);
      }
      case FIRST_PERSON_LEFT_HAND -> {
        ItemRenderTransforms.applyFirstPersonPose(
            matrices, 0.6, 0.3, 0.35, 10.0f, -80.0f, -10.0f, 0.5f);
      }
      case FIRST_PERSON_RIGHT_HAND -> {
        ItemRenderTransforms.applyFirstPersonPose(
            matrices, 0.4, 0.3, 0.35, 10.0f, 80.0f, 10.0f, 0.5f);
      }
      default -> ItemRenderTransforms.applyGroundPose(matrices, 0.5, 0.18, 0.5, 0.28f);
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
