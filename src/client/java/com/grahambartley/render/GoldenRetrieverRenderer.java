package com.grahambartley.render;

import com.grahambartley.entity.GoldenRetrieverEntity;
import com.grahambartley.model.GoldenRetrieverModel;
import com.grahambartley.render.layer.DogCarryBallLayer;
import com.grahambartley.render.layer.GoldenRetrieverCollarLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GoldenRetrieverRenderer extends GeoEntityRenderer<GoldenRetrieverEntity> {

  public GoldenRetrieverRenderer(EntityRendererFactory.Context context) {
    super(context, new GoldenRetrieverModel());
    this.addRenderLayer(new GoldenRetrieverCollarLayer(this));
    this.addRenderLayer(new DogCarryBallLayer<>(this));
  }

  @Override
  public float getMotionAnimThreshold(GoldenRetrieverEntity animatable) {
    return 0.005f;
  }

  @Override
  public void preRender(
      MatrixStack poseStack,
      GoldenRetrieverEntity animatable,
      BakedGeoModel model,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour) {
    if (!isReRender) {
      final float scale = animatable.isBaby() ? 0.85f : 1.7f;
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
