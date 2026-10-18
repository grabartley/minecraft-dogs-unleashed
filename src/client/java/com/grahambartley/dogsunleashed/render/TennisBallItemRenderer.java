package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.block.entity.TennisBallBlockEntity;
import com.grahambartley.dogsunleashed.model.TennisBallModel;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class TennisBallItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

  static final double BALL_RADIUS = 6.0 / 16.0;

  static final double LIFT_TO_BLOCK_CENTRE = 0.5 - BALL_RADIUS;

  private GeoBlockRenderer<TennisBallBlockEntity> renderer;
  private TennisBallBlockEntity dummyEntity;

  @Override
  public void render(
      ItemStack stack,
      ModelTransformationMode mode,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      int overlay) {
    if (this.renderer == null) {
      this.renderer = new GeoBlockRenderer<>(new TennisBallModel());
      this.dummyEntity =
          new TennisBallBlockEntity(BlockPos.ORIGIN, ModBlocks.TENNIS_BALL.getDefaultState());
    }
    matrices.push();
    matrices.translate(0.0, LIFT_TO_BLOCK_CENTRE, 0.0);
    this.renderer.render(this.dummyEntity, 1.0f, matrices, vertexConsumers, light, overlay);
    matrices.pop();
  }
}
