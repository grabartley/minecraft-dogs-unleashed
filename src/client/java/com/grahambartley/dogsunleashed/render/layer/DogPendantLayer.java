package com.grahambartley.dogsunleashed.render.layer;

import com.google.common.collect.MapMaker;
import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.pendant.PendantType;
import com.grahambartley.dogsunleashed.render.rig.CollarBandLocator;
import com.grahambartley.dogsunleashed.render.rig.CollarBandLocator.CollarSpot;
import com.grahambartley.dogsunleashed.render.rig.CollarTextureMask;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class DogPendantLayer extends GeoRenderLayer<UnleashedDogEntity> {

  private static final int OPAQUE = 0xFF000000;
  private static final float CHARM_SCALE = 0.25f;
  private static final float CHARM_HALF_DEPTH = 0.5f / 16.0f;

  private final Map<BakedGeoModel, Optional<CollarSpot>> spots =
      new MapMaker().weakKeys().makeMap();

  private Optional<CollarSpot> frameSpot = Optional.empty();

  public DogPendantLayer(final GeoRenderer<UnleashedDogEntity> renderer) {
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
    this.frameSpot =
        animatable.getPendants().worn() == null || animatable.isInvisible()
            ? Optional.empty()
            : this.spots.computeIfAbsent(bakedModel, model -> locate(model, animatable));
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
    final Optional<CollarSpot> spot = this.frameSpot;
    final PendantType pendant = animatable.getPendants().worn();
    if (spot.isEmpty() || pendant == null || !bone.getName().equals(spot.get().boneName())) {
      return;
    }
    final PendantCharm charm = PendantCharm.of(pendant.family());
    final BakedGeoModel charmModel = GeckoLibCache.getBakedModels().get(charm.model());
    if (charmModel == null) {
      return;
    }
    poseStack.push();
    poseStack.translate(spot.get().point().x, spot.get().point().y, spot.get().point().z);
    poseStack.multiply(new Quaternionf(spot.get().restRotation()).conjugate());
    if (!spot.get().facesNegativeZ()) {
      poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));
    }
    poseStack.scale(CHARM_SCALE, CHARM_SCALE, CHARM_SCALE);
    poseStack.translate(0.0f, 0.0f, -CHARM_HALF_DEPTH);
    final RenderLayer charmLayer = RenderLayer.getEntityCutoutNoCull(charm.texture());
    final VertexConsumer charmBuffer = bufferSource.getBuffer(charmLayer);
    for (final GeoBone charmBone : charmModel.topLevelBones()) {
      this.getRenderer()
          .renderRecursively(
              poseStack,
              animatable,
              charmBone,
              charmLayer,
              bufferSource,
              charmBuffer,
              true,
              partialTick,
              packedLight,
              packedOverlay,
              OPAQUE | pendant.tint());
    }
    poseStack.pop();
  }

  private static Optional<CollarSpot> locate(
      final BakedGeoModel model, final UnleashedDogEntity animatable) {
    final Identifier collarTexture =
        Identifier.of(
            DogsUnleashed.MOD_ID,
            "textures/entity/" + animatable.getRigSourceBreed().serializedId() + "_collar.png");
    return CollarTextureMask.load(MinecraftClient.getInstance().getResourceManager(), collarTexture)
        .flatMap(
            mask ->
                CollarBandLocator.locate(
                    model, animatable.getRigSourceBreed().mouthAnchorBoneName(), mask));
  }
}
