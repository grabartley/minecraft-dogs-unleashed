package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.rig.DogProportions;
import com.grahambartley.dogsunleashed.entity.rig.DogProportions.BoneAdjustment;
import com.grahambartley.dogsunleashed.model.DogModel;
import com.grahambartley.dogsunleashed.render.layer.DogCarryFetchItemLayer;
import com.grahambartley.dogsunleashed.render.layer.DogCollarLayer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DogRenderer extends GeoEntityRenderer<UnleashedDogEntity> {

  public DogRenderer(EntityRendererFactory.Context context) {
    super(context, new DogModel());
    this.addRenderLayer(new DogCollarLayer(this));
    this.addRenderLayer(new DogCarryFetchItemLayer<>(this));
  }

  @Override
  public float getMotionAnimThreshold(UnleashedDogEntity animatable) {
    return 0.005f;
  }

  @Override
  public boolean hasLabel(UnleashedDogEntity animatable) {
    return DogsUnleashed.SERVER_CONFIG.showDogNames() && super.hasLabel(animatable);
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
    if (!isReRender) {
      final UnleashedDogBreed.RenderTransforms transforms =
          animatable.getRigSourceBreed().renderTransforms();
      final float scale = overallScale(animatable, transforms);
      poseStack.scale(scale, scale, scale);
      if (transforms.bodyYawOffsetDegrees() != 0.0f) {
        poseStack.multiply(
            RotationAxis.POSITIVE_Y.rotationDegrees(transforms.bodyYawOffsetDegrees()));
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

  /** A cross sits between its ancestors in overall size, not at whichever one dominates. */
  private static float overallScale(
      final UnleashedDogEntity animatable, final UnleashedDogBreed.RenderTransforms transforms) {
    if (DogModel.usesSharedRig(animatable)) {
      final DogProportions proportions = DogProportions.blend(DogModel.compositionOf(animatable));
      return animatable.isBaby() ? proportions.babyScale() : proportions.adultScale();
    }
    return animatable.isBaby() ? transforms.babyScale() : transforms.adultScale();
  }

  /**
   * Applies this dog's per-bone proportions to the matrix for the bone's subtree. This has to
   * happen per render rather than by writing offsets into the bones: GeckoLib caches one baked
   * model per geo file, so every dog on the shared rig shares the same bone objects and a dog
   * standing next to a differently proportioned one would inherit its shape.
   */
  @Override
  public void renderRecursively(
      final MatrixStack poseStack,
      final UnleashedDogEntity animatable,
      final GeoBone bone,
      final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      final VertexConsumer buffer,
      final boolean isReRender,
      final float partialTick,
      final int packedLight,
      final int packedOverlay,
      final int colour) {
    final BoneAdjustment adjustment = adjustmentFor(animatable, bone.getName());
    if (adjustment.isIdentity()) {
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
      return;
    }

    poseStack.push();
    // matches GeckoLib's own bone offset convention, which negates x
    poseStack.translate(
        -adjustment.offsetX() / 16f, adjustment.offsetY() / 16f, adjustment.offsetZ() / 16f);
    if (adjustment.scaleX() != 1 || adjustment.scaleY() != 1 || adjustment.scaleZ() != 1) {
      poseStack.translate(bone.getPivotX() / 16f, bone.getPivotY() / 16f, bone.getPivotZ() / 16f);
      poseStack.scale(adjustment.scaleX(), adjustment.scaleY(), adjustment.scaleZ());
      poseStack.translate(
          -bone.getPivotX() / 16f, -bone.getPivotY() / 16f, -bone.getPivotZ() / 16f);
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
    poseStack.pop();
  }

  private static BoneAdjustment adjustmentFor(
      final UnleashedDogEntity animatable, final String boneName) {
    if (!DogModel.usesSharedRig(animatable)) {
      return BoneAdjustment.NONE;
    }
    return DogProportions.blend(DogModel.compositionOf(animatable)).forBone(boneName);
  }
}
