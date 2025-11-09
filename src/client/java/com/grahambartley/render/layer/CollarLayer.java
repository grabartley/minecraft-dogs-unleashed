package com.grahambartley.render.layer;

import com.grahambartley.entity.UnleashedDogEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public abstract class CollarLayer<T extends UnleashedDogEntity> extends GeoRenderLayer<T> {

  public CollarLayer(final GeoRenderer<T> entityRendererIn) {
    super(entityRendererIn);
  }

  protected abstract Identifier getCollarTexture();

  @Override
  public void render(
      final MatrixStack poseStack,
      final T animatable,
      final BakedGeoModel bakedModel,
      final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      final VertexConsumer buffer,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {

    if (!animatable.isTamed() || animatable.isInvisible()) {
      return;
    }

    final DyeColor collarColor = animatable.getCollarColor();
    final int color = collarColor.getEntityColor();

    final RenderLayer collarRenderType = RenderLayer.getEntityCutoutNoCull(this.getCollarTexture());
    this.getRenderer()
        .reRender(
            bakedModel,
            poseStack,
            bufferSource,
            animatable,
            collarRenderType,
            bufferSource.getBuffer(collarRenderType),
            partialTick,
            packedLight,
            OverlayTexture.DEFAULT_UV,
            color);
  }
}
