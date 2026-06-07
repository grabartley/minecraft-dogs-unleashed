package com.grahambartley.render;

import com.grahambartley.entity.HuskyEntity;
import com.grahambartley.model.HuskyModel;
import com.grahambartley.render.layer.DogCarryFetchItemLayer;
import com.grahambartley.render.layer.HuskyCollarLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class HuskyRenderer extends GeoEntityRenderer<HuskyEntity> {

  public HuskyRenderer(EntityRendererFactory.Context context) {
    super(context, new HuskyModel());
    this.addRenderLayer(new HuskyCollarLayer(this));
    this.addRenderLayer(new DogCarryFetchItemLayer<>(this));
  }

  @Override
  public float getMotionAnimThreshold(HuskyEntity animatable) {
    return 0.005f;
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
    if (!isReRender) {
      final float scale = animatable.isBaby() ? 0.5f : 1.3f;
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
