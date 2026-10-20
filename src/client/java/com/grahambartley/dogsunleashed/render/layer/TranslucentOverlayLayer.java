package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.List;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public abstract class TranslucentOverlayLayer extends GeoRenderLayer<UnleashedDogEntity> {

  private static final int OPAQUE_WHITE = 0xFFFFFFFF;

  protected TranslucentOverlayLayer(final GeoRenderer<UnleashedDogEntity> renderer) {
    super(renderer);
  }

  protected abstract List<Identifier> overlaysOf(UnleashedDogEntity dog);

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
    if (animatable.isInvisible()) {
      return;
    }
    this.drawOverlays(
        this.overlaysOf(animatable),
        poseStack,
        animatable,
        bakedModel,
        bufferSource,
        partialTick,
        packedLight,
        packedOverlay);
  }

  protected void drawOverlays(
      final List<Identifier> overlays,
      final MatrixStack poseStack,
      final UnleashedDogEntity animatable,
      final BakedGeoModel bakedModel,
      final VertexConsumerProvider bufferSource,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    this.drawOverlays(
        overlays,
        poseStack,
        animatable,
        bakedModel,
        bufferSource,
        partialTick,
        packedLight,
        packedOverlay,
        OPAQUE_WHITE);
  }

  protected void drawOverlays(
      final List<Identifier> overlays,
      final MatrixStack poseStack,
      final UnleashedDogEntity animatable,
      final BakedGeoModel bakedModel,
      final VertexConsumerProvider bufferSource,
      final float partialTick,
      final int packedLight,
      final int packedOverlay,
      final int color) {
    for (final Identifier overlay : overlays) {
      final RenderLayer layer = RenderLayer.getEntityTranslucent(overlay);
      this.getRenderer()
          .reRender(
              bakedModel,
              poseStack,
              bufferSource,
              animatable,
              layer,
              bufferSource.getBuffer(layer),
              partialTick,
              packedLight,
              packedOverlay,
              color);
    }
  }
}
