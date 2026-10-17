package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.config.SyncedServerConfig;
import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.model.DogModel;
import com.grahambartley.dogsunleashed.render.fur.FurLook;
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
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DogRenderer extends GeoEntityRenderer<UnleashedDogEntity> {

  private RigProfile frameProfile = RigProfile.EMPTY;
  private DogFurStage frameStage = DogFurStage.NATURAL;

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
    if (!isReRender) {
      final UnleashedDogBreed.RenderTransforms transforms =
          animatable.getRigSourceBreed().renderTransforms();
      final float scale = animatable.isBaby() ? transforms.babyScale() : transforms.adultScale();
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
  }
}
