package com.grahambartley.dogsunleashed.render.layer;

import com.google.common.collect.MapMaker;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.render.costume.CostumeAssets;
import com.grahambartley.dogsunleashed.render.costume.CostumePartFit;
import com.grahambartley.dogsunleashed.render.overlay.ProjectedOverlays;
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
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class DogCostumeLayer extends GeoRenderLayer<UnleashedDogEntity> {

  public static final String PART_PREFIX = "costume_";
  private static final int OPAQUE_WHITE = 0xFFFFFFFF;

  private final DogCollarLayer collar;
  private final Map<BakedGeoModel, Map<Slot, Optional<SlotAnchor>>> anchors =
      new MapMaker().weakKeys().makeMap();

  private Optional<CostumeAssets> frameCostume = Optional.empty();
  private BakedGeoModel frameModel;
  private RigProfile frameProfile = RigProfile.EMPTY;
  private float frameYaw;

  public DogCostumeLayer(
      final GeoRenderer<UnleashedDogEntity> renderer, final DogCollarLayer collar) {
    super(renderer);
    this.collar = collar;
  }

  public static Optional<CostumeAssets> worn(final UnleashedDogEntity dog) {
    if (dog.isInvisible() || !dog.getArmour().worn().isEmpty()) {
      return Optional.empty();
    }
    final ItemStack costume = dog.getEquipmentHolder().getStack(DogEquipmentSlot.COSTUME);
    return costume.isEmpty()
        ? Optional.empty()
        : Optional.of(CostumeAssets.of(Registries.ITEM.getId(costume.getItem())));
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
    this.frameCostume = worn(animatable);
    this.frameModel = bakedModel;
    this.frameProfile = RigProfiles.of(animatable.getRigSourceBreed());
    this.frameYaw = animatable.getRigSourceBreed().renderTransforms().bodyYawOffsetDegrees();
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
    if (this.frameCostume.isEmpty()) {
      return;
    }
    final Optional<Identifier> suit =
        ProjectedOverlays.shared()
            .textureOf(animatable.getRigSourceBreed(), this.frameCostume.get().sheet());
    if (suit.isEmpty()) {
      return;
    }
    final RenderLayer layer = RenderLayer.getEntityTranslucent(suit.get());
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
    if (this.frameCostume.isEmpty()) {
      return;
    }
    final BakedGeoModel parts =
        GeckoLibCache.getBakedModels().get(this.frameCostume.get().partsModel());
    if (parts == null) {
      return;
    }
    final RenderLayer layer =
        RenderLayer.getEntityCutoutNoCull(this.frameCostume.get().partsTexture());
    for (final GeoBone part : parts.topLevelBones()) {
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
              layer,
              bufferSource,
              bufferSource.getBuffer(layer),
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
