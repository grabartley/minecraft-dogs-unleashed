package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.config.SyncedServerConfig;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmeticItem;
import com.grahambartley.dogsunleashed.model.DogModel;
import com.grahambartley.dogsunleashed.render.cosmetic.HatFit;
import com.grahambartley.dogsunleashed.render.cosmetic.HatFits;
import com.grahambartley.dogsunleashed.render.fur.FurLook;
import com.grahambartley.dogsunleashed.render.growth.GrowthRigScale;
import com.grahambartley.dogsunleashed.render.layer.DogArmourLayer;
import com.grahambartley.dogsunleashed.render.layer.DogCarryFetchItemLayer;
import com.grahambartley.dogsunleashed.render.layer.DogCollarLayer;
import com.grahambartley.dogsunleashed.render.layer.DogCosmeticLayer;
import com.grahambartley.dogsunleashed.render.layer.DogFurOverlayLayer;
import com.grahambartley.dogsunleashed.render.layer.DogPendantLayer;
import com.grahambartley.dogsunleashed.render.layer.DogSheetOverlayLayer;
import com.grahambartley.dogsunleashed.render.layer.UndeadEyeGlowLayer;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.riding.SeatTransform;
import com.grahambartley.dogsunleashed.riding.SeatedRiders;
import com.grahambartley.dogsunleashed.screen.DogCommandWheelScreen;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.RenderUtil;

public class DogRenderer extends GeoEntityRenderer<UnleashedDogEntity> {

  private static final float MODEL_UNITS_PER_BLOCK = 16.0f;

  private RigProfile frameProfile = RigProfile.EMPTY;
  private DogFurStage frameStage = DogFurStage.NATURAL;
  private boolean frameArmoured;
  private Set<String> frameHiddenEars = Set.of();
  private final HatFits hatFits = new HatFits();
  private @Nullable GrowthRigScale frameGrowth;
  private final Matrix4f frameOrigin = new Matrix4f();
  private final Map<String, SeatTransform> frameSeats = new HashMap<>();

  public DogRenderer(EntityRendererFactory.Context context) {
    super(context, new DogModel());
    this.addRenderLayer(new DogCollarLayer(this));
    this.addRenderLayer(new DogCarryFetchItemLayer<>(this, this.frameOrigin));
    this.addRenderLayer(new UndeadEyeGlowLayer(this));
    this.addRenderLayer(new DogFurOverlayLayer(this));
    this.addRenderLayer(new DogSheetOverlayLayer(this));
    this.addRenderLayer(new DogArmourLayer(this));
    this.addRenderLayer(new DogPendantLayer(this));
    this.addRenderLayer(new DogCosmeticLayer(this, this.hatFits));
  }

  @Override
  public void render(
      UnleashedDogEntity entity,
      float entityYaw,
      float partialTick,
      MatrixStack poseStack,
      VertexConsumerProvider bufferSource,
      int packedLight) {
    this.frameOrigin.set(poseStack.peek().getPositionMatrix());
    this.frameSeats.clear();
    super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    if (entity.hasPassengers()) {
      SeatedRiders.drawRiders(entity, Map.copyOf(this.frameSeats), poseStack, partialTick);
    }
  }

  @Override
  public boolean shouldRender(
      UnleashedDogEntity entity, Frustum frustum, double x, double y, double z) {
    return super.shouldRender(entity, frustum, x, y, z)
        || entity.getPassengerList().stream()
            .anyMatch(
                rider ->
                    SeatedRiders.isSeated(rider)
                        && frustum.isVisible(rider.getVisibilityBoundingBox()));
  }

  @Override
  public float getMotionAnimThreshold(UnleashedDogEntity animatable) {
    return 0.005f;
  }

  @Override
  public boolean hasLabel(UnleashedDogEntity animatable) {
    return SyncedServerConfig.get().gameplay().showDogNames()
        && !DogCommandWheelScreen.isOpenFor(animatable.getId())
        && super.hasLabel(animatable);
  }

  @Override
  public void preRender(
      MatrixStack poseStack,
      UnleashedDogEntity animatable,
      BakedGeoModel model,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour) {
    this.frameProfile = RigProfiles.of(animatable.getRigSourceBreed());
    this.frameStage = animatable.getFur().stage();
    this.frameArmoured = !animatable.getArmour().worn().isEmpty();
    this.frameHiddenEars = this.earsHiddenByHat(animatable, model);
    this.frameGrowth =
        animatable.getGrowth().applies()
            ? GrowthRigScale.of(model, this.frameProfile, animatable.getGrowth().stage())
            : null;
    if (!isReRender) {
      final UnleashedDogBreed.RenderTransforms transforms =
          animatable.getRigSourceBreed().renderTransforms();
      final float scale = this.frameScale(animatable, transforms);
      poseStack.scale(scale, scale, scale);
      if (transforms.bodyYawOffsetDegrees() != 0.0f) {
        poseStack.multiply(
            RotationAxis.POSITIVE_Y.rotationDegrees(transforms.bodyYawOffsetDegrees()));
      }
      if (this.frameGrowth != null) {
        poseStack.translate(0.0f, this.frameGrowth.lift(), 0.0f);
      }
    }
    super.preRender(
        poseStack,
        animatable,
        model,
        bufferSource,
        buffer,
        isReRender,
        partialTick,
        packedLight,
        packedOverlay,
        colour);
  }

  private Set<String> earsHiddenByHat(
      final UnleashedDogEntity animatable, final BakedGeoModel model) {
    if (animatable.isInvisible()) {
      return Set.of();
    }
    return DogCosmeticItem.cosmeticOf(
            animatable.getEquipmentHolder().getStack(DogEquipmentSlot.COSMETIC))
        .filter(cosmetic -> cosmetic.mount() == DogCosmetic.Mount.HEAD)
        .flatMap(hat -> this.hatFits.of(model, this.frameProfile, hat))
        .map(HatFit::hiddenEars)
        .orElse(Set.of());
  }

  private float frameScale(
      final UnleashedDogEntity animatable, final UnleashedDogBreed.RenderTransforms transforms) {
    if (this.frameGrowth != null) {
      return transforms.adultScale()
          * animatable.getGrowth().stage().renderScaleMultiplier()
          * this.frameGrowth.heightScale();
    }
    return animatable.isBaby() ? transforms.babyScale() : transforms.adultScale();
  }

  @Override
  public void renderRecursively(
      MatrixStack poseStack,
      UnleashedDogEntity animatable,
      GeoBone bone,
      RenderLayer renderType,
      VertexConsumerProvider bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      int colour) {
    if (this.frameHiddenEars.contains(bone.getName())
        || !FurLook.shows(this.frameProfile, bone.getName(), this.frameStage)
        || (this.frameArmoured
            && FurLook.flattenedByArmour(
                this.frameProfile,
                bone.getName(),
                bone.getParent() == null ? null : bone.getParent().getName()))) {
      return;
    }
    final float growthScale =
        this.frameGrowth == null ? 1.0f : this.frameGrowth.localScale(bone.getName());
    if (growthScale != 1.0f) {
      poseStack.push();
      final float pivotX = bone.getPivotX() / MODEL_UNITS_PER_BLOCK;
      final float pivotY = bone.getPivotY() / MODEL_UNITS_PER_BLOCK;
      final float pivotZ = bone.getPivotZ() / MODEL_UNITS_PER_BLOCK;
      poseStack.translate(pivotX, pivotY, pivotZ);
      poseStack.scale(growthScale, growthScale, growthScale);
      poseStack.translate(-pivotX, -pivotY, -pivotZ);
    }
    if (!isReRender && this.frameProfile.seats().contains(bone.getName())) {
      this.captureSeat(poseStack, animatable, bone);
    }
    super.renderRecursively(
        poseStack,
        animatable,
        bone,
        renderType,
        bufferSource,
        buffer,
        isReRender,
        partialTick,
        packedLight,
        packedOverlay,
        colour);
    if (growthScale != 1.0f) {
      poseStack.pop();
    }
  }

  private void captureSeat(
      final MatrixStack poseStack, final UnleashedDogEntity animatable, final GeoBone bone) {
    poseStack.push();
    RenderUtil.translateMatrixToBone(poseStack, bone);
    RenderUtil.translateToPivotPoint(poseStack, bone);
    RenderUtil.rotateMatrixAroundBone(poseStack, bone);
    this.frameSeats.put(
        bone.getName(),
        SeatTransform.between(
            this.frameOrigin,
            poseStack.peek().getPositionMatrix(),
            animatable.getRigSourceBreed().renderTransforms().bodyYawOffsetDegrees()));
    poseStack.pop();
  }
}
