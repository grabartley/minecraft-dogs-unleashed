package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.entity.StickProjectileEntity;
import com.grahambartley.dogsunleashed.model.StickProjectileModel;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class StickProjectileRenderer extends GeoEntityRenderer<StickProjectileEntity> {
  private static final float PROJECTILE_RENDER_SCALE = 0.85f;

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
      poseStack.scale(PROJECTILE_RENDER_SCALE, PROJECTILE_RENDER_SCALE, PROJECTILE_RENDER_SCALE);
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
