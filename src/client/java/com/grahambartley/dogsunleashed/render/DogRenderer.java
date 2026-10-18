package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.config.SyncedServerConfig;
import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.model.DogModel;
import com.grahambartley.dogsunleashed.render.fur.FurLook;
import com.grahambartley.dogsunleashed.render.growth.GrowthRigScale;
import com.grahambartley.dogsunleashed.render.layer.DogArmourLayer;
import com.grahambartley.dogsunleashed.render.layer.DogCarryFetchItemLayer;
import com.grahambartley.dogsunleashed.render.layer.DogCollarLayer;
import com.grahambartley.dogsunleashed.render.layer.DogFurOverlayLayer;
import com.grahambartley.dogsunleashed.render.layer.DogPendantLayer;
import com.grahambartley.dogsunleashed.render.layer.UndeadEyeGlowLayer;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfiles;
import com.grahambartley.dogsunleashed.screen.DogCommandWheelScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DogRenderer extends GeoEntityRenderer<UnleashedDogEntity> {

  private static final float MODEL_UNITS_PER_BLOCK = 16.0f;

  private RigProfile frameProfile = RigProfile.EMPTY;
  private DogFurStage frameStage = DogFurStage.NATURAL;
  private @Nullable GrowthRigScale frameGrowth;

  public DogRenderer(EntityRendererFactory.Context context) {
    super(context, new DogModel());
    this.addRenderLayer(new DogCollarLayer(this));
    this.addRenderLayer(new DogCarryFetchItemLayer<>(this));
    this.addRenderLayer(new UndeadEyeGlowLayer(this));
    this.addRenderLayer(new DogFurOverlayLayer(this));
    this.addRenderLayer(new DogArmourLayer(this));
    this.addRenderLayer(new DogPendantLayer(this));
  }

  @Override
  public float getMotionAnimThreshold(UnleashedDogEntity animatable) {
    return 0.005f;
  }

  @Override
  public boolean hasLabel(UnleashedDogEntity animatable) {
    return SyncedServerConfig.get().showDogNames()
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
    if (!FurLook.shows(this.frameProfile, bone.getName(), this.frameStage)) {
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
}
