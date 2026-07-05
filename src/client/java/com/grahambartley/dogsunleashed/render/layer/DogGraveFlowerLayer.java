package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.block.entity.DogGraveBlockEntity;
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

public class DogGraveFlowerLayer extends GeoRenderLayer<DogGraveBlockEntity> {

  private static final Identifier FLOWER_TEXTURE =
      Identifier.of(DogsUnleashed.MOD_ID, "textures/block/dog_grave_flower.png");

  public DogGraveFlowerLayer(GeoRenderer<DogGraveBlockEntity> renderer) {
    super(renderer);
  }

  @Override
  public void render(
      MatrixStack poseStack,
      DogGraveBlockEntity animatable,
      BakedGeoModel bakedModel,
      RenderLayer renderType,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      float partialTick,
      int packedLight,
      int packedOverlay) {

    final DyeColor flowerColor = animatable.getFlowerColor();
    final int color = flowerColor.getEntityColor();

    final RenderLayer flowerRenderType = RenderLayer.getEntityCutoutNoCull(FLOWER_TEXTURE);
    this.getRenderer()
        .reRender(
            bakedModel,
            poseStack,
            bufferSource,
            animatable,
            flowerRenderType,
            bufferSource.getBuffer(flowerRenderType),
            partialTick,
            packedLight,
            OverlayTexture.DEFAULT_UV,
            color);
  }
}
