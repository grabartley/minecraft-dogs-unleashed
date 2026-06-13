package com.grahambartley.render;

import com.grahambartley.entity.FrisbeeProjectileEntity;
import com.grahambartley.model.FrisbeeProjectileModel;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FrisbeeProjectileRenderer extends GeoEntityRenderer<FrisbeeProjectileEntity> {

  private static final float FRISBEE_SCALE = 0.5f;

  public FrisbeeProjectileRenderer(EntityRendererFactory.Context context) {
    super(context, new FrisbeeProjectileModel());
  }

  @Override
  public void preRender(
      MatrixStack poseStack,
      FrisbeeProjectileEntity animatable,
      BakedGeoModel model,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour) {
    if (!isReRender) {
      poseStack.scale(FRISBEE_SCALE, FRISBEE_SCALE, FRISBEE_SCALE);
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

  @Override
  public void actuallyRender(
      MatrixStack poseStack,
      FrisbeeProjectileEntity animatable,
      BakedGeoModel model,
      RenderLayer renderType,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour) {

    DyeColor frisbeeColor = animatable.getFrisbeeColor();
    int tintColor = frisbeeColor.getEntityColor() | 0xFF000000;

    super.actuallyRender(
        poseStack,
        animatable,
        model,
        renderType,
        bufferSource,
        buffer,
        isReRender,
        partialTick,
        packedLight,
        packedOverlay,
        tintColor);
  }
}
