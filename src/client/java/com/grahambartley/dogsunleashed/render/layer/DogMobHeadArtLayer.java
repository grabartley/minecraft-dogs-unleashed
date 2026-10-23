package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.cosmetic.MobHeadArt;
import java.util.Optional;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class DogMobHeadArtLayer extends GeoRenderLayer<UnleashedDogEntity> {

  private static final int OPAQUE_WHITE = 0xFFFFFFFF;

  private @Nullable BakedGeoModel frameArt;
  private @Nullable RenderLayer frameLayer;

  public DogMobHeadArtLayer(final GeoRenderer<UnleashedDogEntity> renderer) {
    super(renderer);
  }

  @Override
  public void preRender(
      final MatrixStack poseStack,
      final UnleashedDogEntity animatable,
      final BakedGeoModel bakedModel,
      final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      final VertexConsumer buffer,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    final Optional<MobHeadArt> art = MobHeadArt.on(animatable);
    this.frameArt = art.map(worn -> GeckoLibCache.getBakedModels().get(worn.model())).orElse(null);
    this.frameLayer =
        art.map(worn -> RenderLayer.getEntityCutoutNoCull(worn.texture())).orElse(null);
  }

  @Override
  public void renderForBone(
      final MatrixStack poseStack,
      final UnleashedDogEntity animatable,
      final GeoBone bone,
      final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      final VertexConsumer buffer,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    if (this.frameArt == null || this.frameLayer == null) {
      return;
    }
    for (final GeoBone part : this.frameArt.topLevelBones()) {
      if (!part.getName().equals(bone.getName())) {
        continue;
      }
      poseStack.push();
      this.getRenderer()
          .renderRecursively(
              poseStack,
              animatable,
              part,
              this.frameLayer,
              bufferSource,
              bufferSource.getBuffer(this.frameLayer),
              true,
              partialTick,
              packedLight,
              packedOverlay,
              OPAQUE_WHITE);
      poseStack.pop();
    }
  }
}
