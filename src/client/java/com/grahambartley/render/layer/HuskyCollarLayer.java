package com.grahambartley.render.layer;

import com.grahambartley.entity.HuskyEntity;
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

public class HuskyCollarLayer extends GeoRenderLayer<HuskyEntity> {

  private static final Identifier COLLAR_TEXTURE =
      Identifier.of("dogs-unleashed", "textures/entity/husky_collar.png");

  public HuskyCollarLayer(GeoRenderer<HuskyEntity> renderer) {
    super(renderer);
  }

  @Override
  public void render(
      MatrixStack poseStack,
      HuskyEntity animatable,
      BakedGeoModel bakedModel,
      RenderLayer renderType,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      float partialTick,
      int packedLight,
      int packedOverlay) {

    if (!animatable.isTamed() || animatable.isInvisible()) {
      return;
    }

    final DyeColor collarColor = animatable.getCollarColor();
    final int color = collarColor.getFireworkColor();

    final float red = (float) (color >> 16 & 255) / 255.0f;
    final float green = (float) (color >> 8 & 255) / 255.0f;
    final float blue = (float) (color & 255) / 255.0f;

    final RenderLayer collarRenderType = RenderLayer.getEntityCutoutNoCull(COLLAR_TEXTURE);

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
            (int) (red * 255) << 16 | (int) (green * 255) << 8 | (int) (blue * 255));
  }
}
