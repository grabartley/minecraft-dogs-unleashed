package com.grahambartley.render;

import com.grahambartley.block.entity.TennisBallBlockEntity;
import com.grahambartley.model.TennisBallModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class TennisBallBlockEntityRenderer extends GeoBlockRenderer<TennisBallBlockEntity> {

  private static final float BALL_SCALE = 2.0f;

  public TennisBallBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    super(new TennisBallModel());
  }

  @Override
  public void preRender(
      MatrixStack poseStack,
      TennisBallBlockEntity animatable,
      BakedGeoModel model,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour) {
    poseStack.translate(0.5, 0, 0.5);
    poseStack.scale(BALL_SCALE, BALL_SCALE, BALL_SCALE);
    poseStack.translate(-0.5, 0, -0.5);
    super.preRender(
        poseStack,
        animatable,
        model,
        bufferSource,
        buffer,
        isReRender,
        partialTick,
        packedLight,
        packedOverlay,
        colour);
  }
}
