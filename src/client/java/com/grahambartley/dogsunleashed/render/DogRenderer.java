package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.rig.DogProportions;
import com.grahambartley.dogsunleashed.model.DogModel;
import com.grahambartley.dogsunleashed.render.layer.DogCarryFetchItemLayer;
import com.grahambartley.dogsunleashed.render.layer.DogCollarLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.cache.object.BakedGeoModel;
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
}
