package com.grahambartley.render;

import com.grahambartley.entity.DachshundEntity;
import com.grahambartley.model.DachshundModel;
import com.grahambartley.render.layer.DachshundCollarLayer;
import com.grahambartley.render.layer.DogCarryFetchItemLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DachshundRenderer extends GeoEntityRenderer<DachshundEntity> {

  public DachshundRenderer(EntityRendererFactory.Context context) {
    super(context, new DachshundModel());
    this.addRenderLayer(new DachshundCollarLayer(this));
    this.addRenderLayer(new DogCarryFetchItemLayer<>(this));
  }

  @Override
  public float getMotionAnimThreshold(DachshundEntity animatable) {
    return 0.005f;
  }

  @Override
  public void preRender(
      MatrixStack poseStack,
      DachshundEntity animatable,
      BakedGeoModel model,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour) {
    if (!isReRender) {
      final float scale = animatable.isBaby() ? 0.75f : 1.3f;
      poseStack.scale(scale, scale, scale);
      poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
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
