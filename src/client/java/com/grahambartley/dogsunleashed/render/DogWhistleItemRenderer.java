package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.item.DogWhistleItem;
import com.grahambartley.dogsunleashed.model.DogWhistleModel;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class DogWhistleItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

  private static final float HELD_SCALE = 0.34f;

  private GeoItemRenderer<DogWhistleItem> renderer;

  @Override
  public void render(
      ItemStack stack,
      ModelTransformationMode mode,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      int overlay) {

    if (this.renderer == null) {
      this.renderer = new GeoItemRenderer<>(new DogWhistleModel());
    }

    matrices.push();

    switch (mode) {
      case GUI ->
          ItemRenderTransforms.applyGeoItemPose(
              matrices, 0.5, 0.44, 0.5, 12.0f, 200.0f, 0.0f, 0.58f);
      case GROUND ->
          ItemRenderTransforms.applyGeoItemPose(matrices, 0.5, 0.45, 0.5, 0.0f, 200.0f, 0.0f, 0.5f);
      case FIXED ->
          ItemRenderTransforms.applyGeoItemPose(
              matrices, 0.5, 0.44, 0.5, 0.0f, 200.0f, 0.0f, 0.55f);
      case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND ->
          ItemRenderTransforms.applyGeoItemPose(matrices, 0.5, 0.5, 0.5, 70.0f, 20.0f, 0.0f, 0.5f);
      case FIRST_PERSON_LEFT_HAND ->
          ItemRenderTransforms.applyGeoItemPose(
              matrices, 0.5, 0.5, 0.5, -28.0f, 110.0f, 0.0f, HELD_SCALE);
      case FIRST_PERSON_RIGHT_HAND ->
          ItemRenderTransforms.applyGeoItemPose(
              matrices, 0.5, 0.5, 0.5, -28.0f, 250.0f, 0.0f, HELD_SCALE);
      default ->
          ItemRenderTransforms.applyGeoItemPose(matrices, 0.5, 0.45, 0.5, 0.0f, 200.0f, 0.0f, 0.5f);
    }

    this.renderer.render(stack, mode, matrices, vertexConsumers, light, overlay);

    matrices.pop();
  }
}
