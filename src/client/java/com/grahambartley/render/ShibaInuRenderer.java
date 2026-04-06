package com.grahambartley.render;

import com.grahambartley.entity.ShibaInuEntity;
import com.grahambartley.model.ShibaInuModel;
import com.grahambartley.render.layer.DogCarryBallLayer;
import com.grahambartley.render.layer.ShibaInuCollarLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ShibaInuRenderer extends GeoEntityRenderer<ShibaInuEntity> {

  public ShibaInuRenderer(EntityRendererFactory.Context context) {
    super(context, new ShibaInuModel());
    this.addRenderLayer(new ShibaInuCollarLayer(this));
    this.addRenderLayer(new DogCarryBallLayer<>(this));
  }

  @Override
  public float getMotionAnimThreshold(ShibaInuEntity animatable) {
    return 0.005f;
  }

  @Override
  public void preRender(
      MatrixStack poseStack,
      ShibaInuEntity animatable,
      BakedGeoModel model,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour) {
    if (!isReRender) {
      final float scale = animatable.isBaby() ? 0.75f : 1.5f;
      poseStack.scale(scale, scale, scale);
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
