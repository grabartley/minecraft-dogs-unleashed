package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.costume.CostumeAssets;
import com.grahambartley.dogsunleashed.render.costume.WornCostume;
import com.grahambartley.dogsunleashed.render.overlay.ProjectedOverlays;
import java.util.List;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

public class DogCostumeSuitLayer extends TranslucentOverlayLayer {

  private final DogCollarLayer collar;

  public DogCostumeSuitLayer(
      final GeoRenderer<UnleashedDogEntity> renderer, final DogCollarLayer collar) {
    super(renderer);
    this.collar = collar;
  }

  @Override
  protected List<Identifier> overlaysOf(final UnleashedDogEntity dog) {
    return WornCostume.on(dog)
        .map(CostumeAssets::sheet)
        .flatMap(sheet -> ProjectedOverlays.shared().textureOf(dog.getRigSourceBreed(), sheet))
        .stream()
        .toList();
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
    final List<Identifier> suit = this.overlaysOf(animatable);
    if (suit.isEmpty()) {
      return;
    }
    this.drawOverlays(
        suit,
        poseStack,
        animatable,
        bakedModel,
        bufferSource,
        partialTick,
        packedLight,
        packedOverlay,
        WornCostume.tintOf(animatable));
    this.collar.render(
        poseStack,
        animatable,
        bakedModel,
        renderType,
        bufferSource,
        buffer,
        partialTick,
        packedLight,
        packedOverlay);
  }
}
