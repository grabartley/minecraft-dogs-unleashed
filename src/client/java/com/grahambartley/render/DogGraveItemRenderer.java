package com.grahambartley.render;

import com.grahambartley.DogsUnleashed;
import com.grahambartley.ModBlocks;
import com.grahambartley.ModComponents;
import com.grahambartley.block.entity.DogGraveBlockEntity;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class DogGraveItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

  private final DogGraveBlockEntityRenderer renderer;
  private final DogGraveBlockEntity dummyEntity;

  public DogGraveItemRenderer() {
    this.dummyEntity =
        new DogGraveBlockEntity(BlockPos.ORIGIN, ModBlocks.DOG_GRAVE.getDefaultState());
    this.renderer = new DogGraveBlockEntityRenderer(null);
  }

  @Override
  public void render(
      ItemStack stack,
      ModelTransformationMode mode,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      int overlay) {

    final DyeColor flowerColor =
        stack.getOrDefault(ModComponents.DOG_GRAVE_FLOWER_COLOR, DyeColor.RED);
    dummyEntity.setFlowerColor(flowerColor);

    matrices.push();

    switch (mode) {
      case GUI -> {
        ItemRenderTransforms.applyDisplayPose(matrices, 0.5, 0.25, 0.0, 30.0f, 45.0f, 0.6f);
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

    final BakedGeoModel bakedModel =
        renderer.getGeoModel().getBakedModel(renderer.getGeoModel().getModelResource(dummyEntity));
    final RenderLayer renderType =
        renderer.getRenderType(
            dummyEntity, renderer.getTextureLocation(dummyEntity), vertexConsumers, 1.0f);
    final var buffer = vertexConsumers.getBuffer(renderType);

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
        0xFFFFFFFF);

    // Manually render flower layer with color
    final int flowerTint = flowerColor.getEntityColor();
    final RenderLayer flowerRenderType =
        RenderLayer.getEntityCutoutNoCull(
            Identifier.of(DogsUnleashed.MOD_ID, "textures/block/dog_grave_flower.png"));
    final var flowerBuffer = vertexConsumers.getBuffer(flowerRenderType);

    renderer.actuallyRender(
        matrices,
        dummyEntity,
        bakedModel,
        flowerRenderType,
        vertexConsumers,
        flowerBuffer,
        true, // isReRender = true
        1.0f,
        light,
        overlay,
        flowerTint);

    matrices.pop();
  }
}
