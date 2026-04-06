package com.grahambartley.render;

import com.grahambartley.ModBlocks;
import com.grahambartley.block.entity.TennisBallBlockEntity;
import com.grahambartley.model.TennisBallModel;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class TennisBallItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

  private final GeoBlockRenderer<TennisBallBlockEntity> renderer;
  private final TennisBallBlockEntity dummyEntity;

  public TennisBallItemRenderer() {
    this.dummyEntity =
        new TennisBallBlockEntity(BlockPos.ORIGIN, ModBlocks.TENNIS_BALL.getDefaultState());
    this.renderer = new GeoBlockRenderer<>(new TennisBallModel());
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
      case GUI -> {
        matrices.translate(0.5, 0.5, 0);
        matrices.scale(1.5f, 1.5f, 1.5f);
      }
      case GROUND -> {
        matrices.translate(0.5, 0.3, 0.5);
        matrices.scale(1.0f, 1.0f, 1.0f);
      }
      case FIXED -> {
        matrices.translate(0.5, 0.5, 0.5);
        matrices.scale(1.2f, 1.2f, 1.2f);
      }
      case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
        matrices.translate(0.5, 0.5, 0.5);
        matrices.scale(1.0f, 1.0f, 1.0f);
      }
      case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
        matrices.translate(0.5, 0.5, 0.5);
        matrices.scale(1.0f, 1.0f, 1.0f);
      }
      default -> {
        matrices.translate(0.5, 0.3, 0.5);
        matrices.scale(1.0f, 1.0f, 1.0f);
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
        0xFFFFFFFF);

    matrices.pop();
  }
}
