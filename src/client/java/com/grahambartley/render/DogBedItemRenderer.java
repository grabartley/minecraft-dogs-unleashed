package com.grahambartley.render;

import com.grahambartley.ModBlocks;
import com.grahambartley.ModComponents;
import com.grahambartley.block.entity.DogBedBlockEntity;
import com.grahambartley.model.DogBedModel;
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
        matrices.translate(0.5, 0.3, 0);
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(30));
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(45));
        matrices.scale(1.2f, 1.2f, 1.2f);
      }
      case GROUND -> {
        matrices.translate(0.5, 0.2, 0.5);
        matrices.scale(0.8f, 0.8f, 0.8f);
      }
      case FIXED -> {
        matrices.translate(0.5, 0.2, 0.5);
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(30));
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(45));
        matrices.scale(1.0f, 1.0f, 1.0f);
      }
      case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
        matrices.translate(0.5, 0.35, 0.5);
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(75));
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(45));
        matrices.scale(0.8f, 0.8f, 0.8f);
      }
      case FIRST_PERSON_LEFT_HAND -> {
        matrices.translate(0.6, 0.25, 0.3);
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(10));
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(-80));
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(-10));
        matrices.scale(0.6f, 0.6f, 0.6f);
      }
      case FIRST_PERSON_RIGHT_HAND -> {
        matrices.translate(0.4, 0.25, 0.3);
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(10));
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Y.rotationDegrees(80));
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(10));
        matrices.scale(0.6f, 0.6f, 0.6f);
      }
      default -> {
        matrices.translate(0.5, 0.2, 0.5);
        matrices.scale(0.8f, 0.8f, 0.8f);
      }
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
