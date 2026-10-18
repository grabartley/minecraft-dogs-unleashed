package com.grahambartley.dogsunleashed.render.layer;

import com.google.common.collect.MapMaker;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmeticItem;
import com.grahambartley.dogsunleashed.render.armour.ArmourShell;
import com.grahambartley.dogsunleashed.render.cosmetic.BandanaPrints;
import com.grahambartley.dogsunleashed.render.cosmetic.CosmeticAssets;
import com.grahambartley.dogsunleashed.render.cosmetic.CosmeticFit;
import com.grahambartley.dogsunleashed.render.rig.CharmMount;
import com.grahambartley.dogsunleashed.render.rig.CollarBandLocator.CollarSpot;
import com.grahambartley.dogsunleashed.render.rig.CollarSpots;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.HeadTopLocator;
import com.grahambartley.dogsunleashed.render.rig.HeadTopLocator.HeadTop;
import com.grahambartley.dogsunleashed.render.rig.LivePose;
import com.grahambartley.dogsunleashed.render.rig.PoseClearance;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.rig.SlotBounds;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class DogCosmeticLayer extends GeoRenderLayer<UnleashedDogEntity> {

  private static final int OPAQUE = 0xFF000000;

  private record Collar(CollarSpot spot, CharmMount.Sides sides, float chestWidth) {}

  private final Map<BakedGeoModel, Optional<HeadTop>> heads = new MapMaker().weakKeys().makeMap();
  private final Map<BakedGeoModel, Optional<Collar>> collars = new MapMaker().weakKeys().makeMap();
  private final Map<BakedGeoModel, CubeGeometry.Bounds> cosmeticBounds =
      new MapMaker().weakKeys().makeMap();

  private BakedGeoModel frameModel;
  private ItemStack frameStack = ItemStack.EMPTY;
  private Optional<DogCosmetic> frameCosmetic = Optional.empty();

  public DogCosmeticLayer(final GeoRenderer<UnleashedDogEntity> renderer) {
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
    this.frameModel = bakedModel;
    this.frameStack = animatable.getEquipmentHolder().getStack(DogEquipmentSlot.COSMETIC);
    this.frameCosmetic =
        animatable.isInvisible() ? Optional.empty() : DogCosmeticItem.cosmeticOf(this.frameStack);
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
    if (this.frameCosmetic.isEmpty()) {
      return;
    }
    final DogCosmetic cosmetic = this.frameCosmetic.get();
    final BakedGeoModel model = GeckoLibCache.getBakedModels().get(CosmeticAssets.model(cosmetic));
    if (model == null) {
      return;
    }
    final RigProfile profile = RigProfiles.of(animatable.getRigSourceBreed());
    poseStack.push();
    final boolean placed =
        switch (cosmetic.mount()) {
          case HEAD -> this.placeOnHead(poseStack, bone, profile, cosmetic, model);
          case COLLAR -> this.placeOnCollar(poseStack, animatable, bone, profile, cosmetic, model);
        };
    if (placed) {
      this.draw(
          poseStack,
          animatable,
          model,
          RenderLayer.getEntityCutoutNoCull(CosmeticAssets.texture(cosmetic)),
          this.tintOf(cosmetic),
          bufferSource,
          partialTick,
          packedLight,
          packedOverlay);
      if (cosmetic.takesBannerPatterns()) {
        this.drawPatterns(
            poseStack, animatable, model, bufferSource, partialTick, packedLight, packedOverlay);
      }
    }
    poseStack.pop();
  }

  private boolean placeOnHead(
      final MatrixStack poseStack,
      final GeoBone bone,
      final RigProfile profile,
      final DogCosmetic cosmetic,
      final BakedGeoModel model) {
    final Optional<HeadTop> top =
        this.heads.computeIfAbsent(this.frameModel, dog -> HeadTopLocator.locate(dog, profile));
    if (top.isEmpty() || !bone.getName().equals(top.get().boneName())) {
      return false;
    }
    final float width =
        CosmeticFit.hatWidth(cosmetic, top.get().headWidth(), top.get().clearWidth());
    final float scale = width / this.widthOf(model);
    final Vector3f point = top.get().point();
    poseStack.translate(point.x, point.y, point.z);
    poseStack.multiply(new Quaternionf(top.get().restRotation()).conjugate());
    poseStack.scale(scale, scale, scale);
    return true;
  }

  private boolean placeOnCollar(
      final MatrixStack poseStack,
      final UnleashedDogEntity animatable,
      final GeoBone bone,
      final RigProfile profile,
      final DogCosmetic cosmetic,
      final BakedGeoModel model) {
    final Optional<Collar> collar =
        this.collars.computeIfAbsent(this.frameModel, dog -> collarOf(dog, animatable, profile));
    if (collar.isEmpty() || !bone.getName().equals(collar.get().spot().boneName())) {
      return false;
    }
    final CollarSpot spot = collar.get().spot();
    final float scale =
        CosmeticFit.collarWidth(cosmetic, collar.get().chestWidth()) / this.widthOf(model);
    final Vector3f clearance =
        CharmMount.clearanceInBone(
            PoseClearance.liveBoxesOf(this.frameModel, collar.get().sides().body()),
            ArmourShell.boxes(this.frameModel, animatable, collar.get().sides().head()),
            () -> PoseClearance.liveBoxesOf(this.frameModel, collar.get().sides().head()),
            LivePose.of(bone),
            CharmMount.placedToBone(spot, scale),
            this.boundsOf(model),
            spot.facesNegativeZ());
    poseStack.translate(clearance.x, clearance.y, clearance.z);
    CharmMount.place(poseStack, spot, scale);
    return true;
  }

  private static Optional<Collar> collarOf(
      final BakedGeoModel dog, final UnleashedDogEntity animatable, final RigProfile profile) {
    final Optional<String> mouth = profile.mouthBone();
    if (mouth.isEmpty()) {
      return Optional.empty();
    }
    final CubeGeometry.Bounds chest = SlotBounds.of(dog, profile.bonesIn(Slot.TORSO));
    if (chest.isEmpty()) {
      return Optional.empty();
    }
    return CollarSpots.locate(dog, animatable.getRigSourceBreed(), mouth.get())
        .map(
            spot ->
                new Collar(
                    spot,
                    CharmMount.sidesOf(dog, spot, mouth.get()),
                    chest.max().x - chest.min().x));
  }

  private void drawPatterns(
      final MatrixStack poseStack,
      final UnleashedDogEntity animatable,
      final BakedGeoModel model,
      final VertexConsumerProvider bufferSource,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    final BannerPatternsComponent patterns =
        this.frameStack.getOrDefault(
            DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT);
    for (final BannerPatternsComponent.Layer layer : patterns.layers()) {
      final Optional<Identifier> print = BandanaPrints.shared().textureFor(layer.pattern());
      if (print.isPresent()) {
        this.draw(
            poseStack,
            animatable,
            model,
            RenderLayer.getEntityTranslucent(print.get()),
            layer.color().getEntityColor(),
            bufferSource,
            partialTick,
            packedLight,
            packedOverlay);
      }
    }
  }

  private void draw(
      final MatrixStack poseStack,
      final UnleashedDogEntity animatable,
      final BakedGeoModel model,
      final RenderLayer layer,
      final int tint,
      final VertexConsumerProvider bufferSource,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    final VertexConsumer consumer = bufferSource.getBuffer(layer);
    for (final GeoBone cosmeticBone : model.topLevelBones()) {
      this.getRenderer()
          .renderRecursively(
              poseStack,
              animatable,
              cosmeticBone,
              layer,
              bufferSource,
              consumer,
              true,
              partialTick,
              packedLight,
              packedOverlay,
              OPAQUE | tint);
    }
  }

  private int tintOf(final DogCosmetic cosmetic) {
    return cosmetic.isDyeable()
        ? DyedColorComponent.getColor(this.frameStack, cosmetic.undyedColor())
        : 0xFFFFFF;
  }

  private float widthOf(final BakedGeoModel model) {
    final CubeGeometry.Bounds bounds = this.boundsOf(model);
    return Math.max(bounds.max().x - bounds.min().x, 1.0e-3f);
  }

  private CubeGeometry.Bounds boundsOf(final BakedGeoModel model) {
    return this.cosmeticBounds.computeIfAbsent(
        model,
        cosmeticModel -> {
          final CubeGeometry.Bounds bounds = new CubeGeometry.Bounds();
          for (final GeoBone cosmeticBone : cosmeticModel.topLevelBones()) {
            final CubeGeometry.Bounds part = CubeGeometry.posedBoundsOf(cosmeticBone);
            if (!part.isEmpty()) {
              bounds.include(part.min());
              bounds.include(part.max());
            }
          }
          return bounds;
        });
  }
}
