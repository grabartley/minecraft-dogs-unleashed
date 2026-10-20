package com.grahambartley.dogsunleashed.render.layer;

import com.google.common.collect.MapMaker;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.costume.CostumeAssets;
import com.grahambartley.dogsunleashed.render.costume.CostumePartFit;
import com.grahambartley.dogsunleashed.render.costume.CostumePartsAnimator;
import com.grahambartley.dogsunleashed.render.costume.WornCostume;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.SlotAnchor;
import java.util.EnumMap;
import java.util.Map;
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

public class DogCostumePartsLayer extends GeoRenderLayer<UnleashedDogEntity> {

  private static final String PART_PREFIX = "costume_";
  private static final int OPAQUE_WHITE = 0xFFFFFFFF;

  private final Map<BakedGeoModel, Map<Slot, Optional<SlotAnchor>>> anchors =
      new MapMaker().weakKeys().makeMap();

  private @Nullable BakedGeoModel frameParts;
  private @Nullable RenderLayer frameLayer;
  private @Nullable BakedGeoModel frameModel;
  private RigProfile frameProfile = RigProfile.EMPTY;
  private float frameYaw;

  public DogCostumePartsLayer(final GeoRenderer<UnleashedDogEntity> renderer) {
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
    final Optional<CostumeAssets> costume = WornCostume.on(animatable);
    this.frameParts =
        costume
            .map(
                assets -> {
                  final BakedGeoModel parts =
                      GeckoLibCache.getBakedModels().get(assets.partsModel());
                  return parts == null
                      ? null
                      : CostumePartsAnimator.shared()
                          .animate(animatable, assets, parts, partialTick);
                })
            .orElse(null);
    this.frameLayer =
        this.frameParts == null
            ? null
            : RenderLayer.getEntityCutoutNoCull(costume.get().partsTexture());
    this.frameModel = bakedModel;
    this.frameProfile = RigProfiles.of(animatable.getRigSourceBreed());
    this.frameYaw = animatable.getRigSourceBreed().renderTransforms().bodyYawOffsetDegrees();
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
    if (this.frameParts == null || this.frameLayer == null) {
      return;
    }
    for (final GeoBone part : this.frameParts.topLevelBones()) {
      final Optional<SlotAnchor> anchor = this.anchorFor(part.getName());
      if (anchor.isEmpty() || !bone.getName().equals(anchor.get().boneName())) {
        continue;
      }
      poseStack.push();
      poseStack.multiplyPositionMatrix(CostumePartFit.partToBone(anchor.get(), this.frameYaw));
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

  private Optional<SlotAnchor> anchorFor(final String partName) {
    if (!partName.startsWith(PART_PREFIX)) {
      return Optional.empty();
    }
    final Optional<Slot> slot = RigProfile.slotOf(partName.substring(PART_PREFIX.length()));
    if (slot.isEmpty()) {
      return Optional.empty();
    }
    return this.anchors
        .computeIfAbsent(this.frameModel, model -> new EnumMap<>(Slot.class))
        .computeIfAbsent(
            slot.get(), key -> SlotAnchor.locate(this.frameModel, this.frameProfile.bonesIn(key)));
  }
}
