package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.overlay.OverlayEffects;
import com.grahambartley.dogsunleashed.render.overlay.ProjectedOverlays;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class DogSheetOverlayLayer extends GeoRenderLayer<UnleashedDogEntity> {

  private static final int OPAQUE_WHITE = 0xFFFFFFFF;

  public DogSheetOverlayLayer(final GeoRenderer<UnleashedDogEntity> renderer) {
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
    if (animatable.isInvisible()) {
      return;
    }
    for (final Identifier sheet : OverlayEffects.dogs().activeOn(animatable)) {
      ProjectedOverlays.shared()
          .textureOf(animatable.getRigSourceBreed(), sheet)
          .ifPresent(
              texture -> {
                final RenderLayer layer = RenderLayer.getEntityTranslucent(texture);
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
                        OPAQUE_WHITE);
              });
    }
  }
}
