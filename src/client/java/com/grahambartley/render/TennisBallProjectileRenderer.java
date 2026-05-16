package com.grahambartley.render;

import com.grahambartley.entity.TennisBallProjectileEntity;
import com.grahambartley.model.TennisBallProjectileModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TennisBallProjectileRenderer extends GeoEntityRenderer<TennisBallProjectileEntity> {

  private static final float BALL_SCALE = 0.5f;

  public TennisBallProjectileRenderer(EntityRendererFactory.Context context) {
    super(context, new TennisBallProjectileModel());
  }

  @Override
  public void preRender(
      MatrixStack poseStack,
      TennisBallProjectileEntity animatable,
      BakedGeoModel model,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour) {
    if (!isReRender) {
      poseStack.scale(BALL_SCALE, BALL_SCALE, BALL_SCALE);
    }
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
