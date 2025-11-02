package com.grahambartley.render;

import com.grahambartley.entity.HuskyEntity;
import com.grahambartley.model.HuskyModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class HuskyRenderer extends GeoEntityRenderer<HuskyEntity> {

  public HuskyRenderer(EntityRendererFactory.Context context) {
    super(context, new HuskyModel());
  }

  @Override
  public float getMotionAnimThreshold(HuskyEntity animatable) {
    return 0.005f;
  }

  @Override
  protected float getDeathMaxRotation(HuskyEntity animatable) {
    return 0f;
  }

  @Override
  public float getShadowRadius(HuskyEntity entity) {
    return entity.isBaby() ? 0.25f : 0.5f;
  }

  @Override
  public void preRender(
      MatrixStack poseStack,
      HuskyEntity animatable,
      BakedGeoModel model,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour) {
    float scale = 1.0f;
    if (animatable.isBaby()) {
      scale = 0.5f;
    }
    poseStack.scale(scale, scale, scale);
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
