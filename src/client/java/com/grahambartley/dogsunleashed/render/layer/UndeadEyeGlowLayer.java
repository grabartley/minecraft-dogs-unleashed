package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.coat.UndeadDogEyes;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class UndeadEyeGlowLayer extends GeoRenderLayer<UnleashedDogEntity> {

  public UndeadEyeGlowLayer(final GeoRenderer<UnleashedDogEntity> renderer) {
    super(renderer);
  }

  @Override
  public void render(
      final MatrixStack poseStack,
      final UnleashedDogEntity animatable,
      final BakedGeoModel bakedModel,
      final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      final VertexConsumer buffer,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    if (!animatable.isUndead()) {
      return;
    }
    final RenderLayer eyesRenderType =
        RenderLayer.getEyes(UndeadDogEyes.of(animatable.getRigSourceBreed()).glowTexture());
    this.getRenderer()
        .reRender(
            bakedModel,
            poseStack,
            bufferSource,
            animatable,
            eyesRenderType,
            bufferSource.getBuffer(eyesRenderType),
            partialTick,
            packedLight,
            OverlayTexture.DEFAULT_UV,
            0xFFFFFFFF);
  }
}
