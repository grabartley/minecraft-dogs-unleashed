package com.grahambartley.render;

import com.grahambartley.entity.StickProjectileEntity;
import com.grahambartley.model.StickProjectileModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class StickProjectileRenderer extends GeoEntityRenderer<StickProjectileEntity> {

  public StickProjectileRenderer(EntityRendererFactory.Context context) {
    super(context, new StickProjectileModel());
  }

  @Override
  public void preRender(
      MatrixStack poseStack,
      StickProjectileEntity animatable,
      BakedGeoModel model,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour) {
    if (!isReRender) {
      poseStack.scale(0.85f, 0.85f, 0.85f);
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
