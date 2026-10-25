package com.grahambartley.dogsunleashed.model;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import com.grahambartley.dogsunleashed.entity.variant.UnleashedDogCoat;
import com.grahambartley.dogsunleashed.render.coat.UndeadCoatTextures;
import com.grahambartley.dogsunleashed.render.coat.UndeadEyeGlows;
import com.grahambartley.dogsunleashed.render.cross.CrossBreedRendering;
import com.grahambartley.dogsunleashed.render.rig.RigModels;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;

public class DogModel extends GeoModel<UnleashedDogEntity> {

  private static final double NEVER_UPDATED = -1.0;
  private static final SharedBonePoses<BakedGeoModel> SHARED_BONES = new SharedBonePoses<>();

  @Override
  public void handleAnimations(
      final UnleashedDogEntity animatable,
      final long instanceId,
      final AnimationState<UnleashedDogEntity> animationState,
      final float partialTick) {
    final BakedGeoModel bones = this.getBakedModel(this.getModelResource(animatable));
    if (!SHARED_BONES.lastPosedBy(bones, this, instanceId)) {
      animatable.getAnimatableInstanceCache().getManagerForId(instanceId).updatedAt(NEVER_UPDATED);
    }
    super.handleAnimations(animatable, instanceId, animationState, partialTick);
    SHARED_BONES.posedBy(bones, this, instanceId);
  }

  @Override
  public Identifier getModelResource(final UnleashedDogEntity animatable) {
    return RigModels.idOf(animatable.getRigSourceBreed());
  }

  @Override
  public Identifier getTextureResource(final UnleashedDogEntity animatable) {
    final Identifier living =
        CrossBreedRendering.coatOf(
            animatable,
            Identifier.of(MOD_ID, "textures/entity/" + flatTextureName(animatable) + ".png"));
    if (!animatable.isUndead()) {
      return living;
    }
    return UndeadCoatTextures.undeadOf(
        living, UndeadEyeGlows.shared().eyesOf(animatable.getRigSourceBreed()));
  }

  @Override
  public Identifier getAnimationResource(final UnleashedDogEntity animatable) {
    return animatable.getRigSourceBreed().animationId();
  }

  private static String flatTextureName(final UnleashedDogEntity animatable) {
    final StringBuilder fileName = new StringBuilder(animatable.getRigSourceBreed().serializedId());
    final UnleashedDogCoat coat = animatable.getCoatVariant();
    if (coat != null) {
      fileName.append("_").append(coat.getTexturePrefix());
    }
    final HuskyEyeColor eyeColor = animatable.getEyeColorVariant();
    if (eyeColor != null) {
      fileName.append("_").append(eyeColor.textureSuffix());
    }
    return fileName.toString();
  }
}
