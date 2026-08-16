package com.grahambartley.dogsunleashed.render.layer;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
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

public class DogCollarLayer extends GeoRenderLayer<UnleashedDogEntity> {

  public DogCollarLayer(final GeoRenderer<UnleashedDogEntity> entityRendererIn) {
    super(entityRendererIn);
  }

  protected Identifier getCollarTexture(final UnleashedDogEntity animatable) {
    return Identifier.of(
        MOD_ID, "textures/entity/" + animatable.getRigSourceBreed().serializedId() + "_collar.png");
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

    if (!animatable.isTamed() || animatable.isInvisible()) {
      return;
    }

    final DyeColor collarColor = animatable.getCollarColor();
    final int color = collarColor.getEntityColor();

    final RenderLayer collarRenderType =
        RenderLayer.getEntityCutoutNoCull(this.getCollarTexture(animatable));
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
