package com.grahambartley.dogsunleashed.render;

import com.google.common.collect.Sets;
import com.grahambartley.dogsunleashed.config.SyncedServerConfig;
import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.DogSledHarness;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.model.DogModel;
import com.grahambartley.dogsunleashed.render.cosmetic.HatFits;
import com.grahambartley.dogsunleashed.render.cosmetic.MobHeadFits;
import com.grahambartley.dogsunleashed.render.cross.CrossBreedRendering;
import com.grahambartley.dogsunleashed.render.fur.DrawnFur;
import com.grahambartley.dogsunleashed.render.fur.FurCover;
import com.grahambartley.dogsunleashed.render.growth.AnchoredPlacement;
import com.grahambartley.dogsunleashed.render.growth.GrowthRigScale;
import com.grahambartley.dogsunleashed.render.layer.DogArmourLayer;
import com.grahambartley.dogsunleashed.render.layer.DogCarryFetchItemLayer;
import com.grahambartley.dogsunleashed.render.layer.DogCollarLayer;
import com.grahambartley.dogsunleashed.render.layer.DogCosmeticLayer;
import com.grahambartley.dogsunleashed.render.layer.DogCostumePartsLayer;
import com.grahambartley.dogsunleashed.render.layer.DogCostumeSuitLayer;
import com.grahambartley.dogsunleashed.render.layer.DogFurOverlayLayer;
import com.grahambartley.dogsunleashed.render.layer.DogMobHeadArtLayer;
import com.grahambartley.dogsunleashed.render.layer.DogMobHeadLayer;
import com.grahambartley.dogsunleashed.render.layer.DogPendantLayer;
import com.grahambartley.dogsunleashed.render.layer.DogSaddlebagsLayer;
import com.grahambartley.dogsunleashed.render.layer.DogSheetOverlayLayer;
import com.grahambartley.dogsunleashed.render.layer.UndeadEyeGlowLayer;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.render.sled.HarnessRing;
import com.grahambartley.dogsunleashed.render.sled.HarnessRumps;
import com.grahambartley.dogsunleashed.riding.SeatTransform;
import com.grahambartley.dogsunleashed.riding.SeatedRiders;
import com.grahambartley.dogsunleashed.screen.DogCommandWheelScreen;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.RenderUtil;

public class DogRenderer extends GeoEntityRenderer<UnleashedDogEntity> {

  private static final float MODEL_UNITS_PER_BLOCK = 16.0f;

  private RigProfile frameProfile = RigProfile.EMPTY;
  private DogFurStage frameStage = DogFurStage.NATURAL;
  private FurCover frameFurCover = FurCover.NONE;
  private Set<String> frameHiddenEars = Set.of();
  private final HatFits hatFits = new HatFits();
  private final MobHeadFits mobHeadFits = new MobHeadFits();
  private @Nullable GrowthRigScale frameGrowth;
  private final Matrix4f frameOrigin = new Matrix4f();
  private final Map<String, SeatTransform> frameSeats = new HashMap<>();
  private @Nullable String frameHarnessRing;

  public DogRenderer(EntityRendererFactory.Context context) {
    this(context, new DogModel());
    this.addDogLayers(context);
  }

  protected final void addDogLayers(final EntityRendererFactory.Context context) {
    final DogCollarLayer collar = new DogCollarLayer(this);
    this.addRenderLayer(collar);
    this.addRenderLayer(new DogCarryFetchItemLayer<>(this, this.frameOrigin));
    this.addRenderLayer(new UndeadEyeGlowLayer(this));
    this.addRenderLayer(new DogFurOverlayLayer(this));
    this.addRenderLayer(new DogSheetOverlayLayer(this));
    this.addRenderLayer(new DogCostumeSuitLayer(this, collar));
    this.addRenderLayer(new DogCostumePartsLayer(this));
    this.addRenderLayer(new DogArmourLayer(this));
    this.addRenderLayer(new DogPendantLayer(this));
    this.addRenderLayer(new DogSaddlebagsLayer(this));
    this.addRenderLayer(new DogCosmeticLayer(this, this.hatFits));
    this.addRenderLayer(new DogMobHeadLayer(this, this.mobHeadFits, context));
    this.addRenderLayer(new DogMobHeadArtLayer(this));
  }

  protected DogRenderer(final EntityRendererFactory.Context context, final DogModel model) {
    super(context, model);
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
    this.frameHarnessRing =
        animatable.getComponents().get(DogSledHarness.class).isHitched()
            ? HarnessRing.boneOf(model, this.frameProfile).orElse(null)
            : null;
    this.frameStage = animatable.getFur().stage();
    this.frameFurCover = FurCover.on(animatable);
    this.frameHiddenEars =
        CrossBreedRendering.hiddenEars(
            animatable,
            this.frameProfile,
            Sets.union(
                this.hatFits.earsHiddenOn(animatable, model, this.frameProfile),
                this.mobHeadFits.earsHiddenOn(animatable, model, this.frameProfile)));
    this.frameGrowth =
        animatable.getGrowth().applies()
            ? GrowthRigScale.of(model, this.frameProfile, animatable.getGrowth().stage())
            : CrossBreedRendering.shapeOf(animatable, model, this.frameProfile);
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

  private float frameScale(
      final UnleashedDogEntity animatable, final UnleashedDogBreed.RenderTransforms transforms) {
    return DogFrameScale.of(
        transforms,
        animatable.isBaby(),
        animatable.getGrowth().applies()
            ? animatable.getGrowth().stage().renderScaleMultiplier()
            : null,
        this.frameGrowth == null ? 1.0f : this.frameGrowth.heightScale());
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
        || !DrawnFur.draws(
            this.frameProfile,
            this.frameStage,
            this.frameFurCover,
            bone.getName(),
            bone.getParent() == null ? null : bone.getParent().getName())) {
      return;
    }
    final Optional<AnchoredPlacement.Shift> shift =
        this.frameGrowth == null ? Optional.empty() : this.frameGrowth.shiftOf(bone.getName());
    final float growthScale =
        this.frameGrowth == null ? 1.0f : this.frameGrowth.localScale(bone.getName());
    final boolean scaled = shift.isPresent() || growthScale != 1.0f;
    if (shift.isPresent()) {
      poseStack.push();
      final Vector3f to = shift.get().to().div(MODEL_UNITS_PER_BLOCK);
      final Vector3f from = shift.get().from().div(MODEL_UNITS_PER_BLOCK);
      poseStack.translate(to.x, to.y, to.z);
      poseStack.scale(shift.get().scale(), shift.get().scale(), shift.get().scale());
      poseStack.translate(-from.x, -from.y, -from.z);
    } else if (growthScale != 1.0f) {
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
    if (!isReRender && bone.getName().equals(this.frameHarnessRing)) {
      this.captureHarnessRing(poseStack, animatable, bone);
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
    if (scaled) {
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

  private void captureHarnessRing(
      final MatrixStack poseStack, final UnleashedDogEntity animatable, final GeoBone bone) {
    poseStack.push();
    RenderUtil.translateMatrixToBone(poseStack, bone);
    RenderUtil.translateToPivotPoint(poseStack, bone);
    final Vector3f ring = poseStack.peek().getPositionMatrix().transformPosition(new Vector3f());
    poseStack.pop();
    final Vec3d camera = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
    HarnessRumps.record(animatable, camera.add(ring.x, ring.y, ring.z));
  }
}
