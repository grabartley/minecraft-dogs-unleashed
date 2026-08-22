package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.block.entity.DogHouseBlockEntity;
import com.grahambartley.dogsunleashed.model.DogHouseModel;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** The house is two blocks tall, so every pose is scaled down harder than a one-cell prop. */
public class DogHouseItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

  private final GeoBlockRenderer<DogHouseBlockEntity> renderer;
  private final DogHouseBlockEntity dummyEntity;

  public DogHouseItemRenderer() {
    this.dummyEntity =
        new DogHouseBlockEntity(BlockPos.ORIGIN, ModBlocks.DOG_HOUSE.getDefaultState());
    this.renderer = new GeoBlockRenderer<>(new DogHouseModel());
  }

  @Override
  public void render(
      ItemStack stack,
      ModelTransformationMode mode,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      int overlay) {

    matrices.push();

    switch (mode) {
      case GUI ->
          ItemRenderTransforms.applyDisplayPose(matrices, 0.5, 0.06, 0.0, 20.0f, 35.0f, 0.62f);
      case GROUND -> ItemRenderTransforms.applyGroundPose(matrices, 0.5, 0.15, 0.5, 0.4f);
      case FIXED ->
          ItemRenderTransforms.applyDisplayPose(matrices, 0.5, 0.1, 0.5, 15.0f, 35.0f, 0.5f);
      case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND ->
          ItemRenderTransforms.applyDisplayPose(matrices, 0.5, 0.25, 0.5, 60.0f, 45.0f, 0.4f);
      case FIRST_PERSON_LEFT_HAND ->
          ItemRenderTransforms.applyFirstPersonPose(
              matrices, 0.6, 0.2, 0.3, 10.0f, -80.0f, -10.0f, 0.35f);
      case FIRST_PERSON_RIGHT_HAND ->
          ItemRenderTransforms.applyFirstPersonPose(
              matrices, 0.4, 0.2, 0.3, 10.0f, 80.0f, 10.0f, 0.35f);
      default -> ItemRenderTransforms.applyGroundPose(matrices, 0.5, 0.15, 0.5, 0.4f);
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
}
