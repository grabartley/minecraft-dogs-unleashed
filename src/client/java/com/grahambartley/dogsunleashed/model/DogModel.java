package com.grahambartley.dogsunleashed.model;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.rig.DogEarShape;
import com.grahambartley.dogsunleashed.entity.rig.DogProportions;
import com.grahambartley.dogsunleashed.entity.rig.DogProportions.BoneAdjustment;
import com.grahambartley.dogsunleashed.entity.rig.DogRig;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import com.grahambartley.dogsunleashed.entity.variant.UnleashedDogCoat;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.render.coat.CoatPigments;
import com.grahambartley.dogsunleashed.render.coat.CoatPigments.Donor;
import com.grahambartley.dogsunleashed.render.coat.CoatRecipe;
import com.grahambartley.dogsunleashed.render.coat.DogCoatIds;
import com.grahambartley.dogsunleashed.render.coat.DogCoatTextures;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class DogModel extends GeoModel<UnleashedDogEntity> {

  private static final List<String> EAR_PIVOTS = List.of("ear1", "ear2");

  @Override
  public Identifier getModelResource(final UnleashedDogEntity animatable) {
    final String model =
        usesSharedRig(animatable) ? DogRig.MODEL_ID : animatable.getRigSourceBreed().serializedId();
    return Identifier.of(MOD_ID, "geo/" + model + ".geo.json");
  }

  @Override
  public Identifier getTextureResource(final UnleashedDogEntity animatable) {
    if (usesSharedRig(animatable)) {
      final Identifier composited = compositedTexture(animatable);
      if (composited != null) {
        return composited;
      }
    }
    return Identifier.of(MOD_ID, "textures/entity/" + flatTextureName(animatable) + ".png");
  }

  @Override
  public Identifier getAnimationResource(final UnleashedDogEntity animatable) {
    if (usesSharedRig(animatable)) {
      return Identifier.of(MOD_ID, "animations/" + DogRig.MODEL_ID + ".animation.json");
    }
    return Identifier.of(MOD_ID, animatable.getRigSourceBreed().animationPath());
  }

  /**
   * Applies this dog's own proportions on top of whatever the animation just posed. GeckoLib resets
   * every bone before this runs, so the adjustment is reapplied from a clean state each frame and
   * two differently proportioned dogs render correctly side by side.
   */
  @Override
  public void setCustomAnimations(
      final UnleashedDogEntity animatable,
      final long instanceId,
      final AnimationState<UnleashedDogEntity> animationState) {
    if (!usesSharedRig(animatable)) {
      return;
    }

    final DogProportions proportions = DogProportions.blend(compositionOf(animatable));
    proportions
        .bones()
        .forEach(
            (boneName, adjustment) -> getBone(boneName).ifPresent(bone -> apply(bone, adjustment)));

    final DogEarShape inherited =
        DogEarShape.inherit(compositionOf(animatable), animatable.getUuid());
    for (final String pivot : EAR_PIVOTS) {
      for (final DogEarShape shape : DogEarShape.values()) {
        getBone(shape.boneName(pivot)).ifPresent(bone -> bone.setHidden(shape != inherited));
      }
    }
  }

  private static void apply(final GeoBone bone, final BoneAdjustment adjustment) {
    bone.setPosX(bone.getPosX() + adjustment.offsetX());
    bone.setPosY(bone.getPosY() + adjustment.offsetY());
    bone.setPosZ(bone.getPosZ() + adjustment.offsetZ());
    bone.setScaleX(bone.getScaleX() * adjustment.scaleX());
    bone.setScaleY(bone.getScaleY() * adjustment.scaleY());
    bone.setScaleZ(bone.getScaleZ() * adjustment.scaleZ());
  }

  public static boolean usesSharedRig(final UnleashedDogEntity animatable) {
    return DogRig.supports(animatable.getGenome(), animatable.getRigSourceBreed());
  }

  public static List<BreedShare> compositionOf(final UnleashedDogEntity animatable) {
    final DogGenome genome = animatable.getGenome();
    if (genome != null && !genome.composition().isEmpty()) {
      return genome.composition();
    }
    return List.of(new BreedShare(animatable.getRigSourceBreed(), 1.0f));
  }

  private static Identifier compositedTexture(final UnleashedDogEntity animatable) {
    final String layoutCoatId = DogCoatIds.layoutOf(animatable);
    final List<BreedShare> composition = CoatPigments.normalisedShares(compositionOf(animatable));
    if (composition.isEmpty()) {
      return null;
    }

    final UnleashedDogBreed layoutBreed = animatable.getRigSourceBreed();
    final List<Donor> donors = new ArrayList<>(composition.size());
    for (final BreedShare share : composition) {
      final String donorCoatId =
          share.breed() == layoutBreed ? layoutCoatId : DogCoatIds.donorFor(share.breed());
      final CoatRecipe recipe = DogCoatTextures.recipe(donorCoatId);
      if (recipe != null) {
        donors.add(new Donor(recipe, share.share()));
      }
    }
    if (donors.isEmpty()) {
      return null;
    }

    final DogEarShape earShape = DogEarShape.inherit(composition, animatable.getUuid());
    final String earDonorCoatId =
        earShape.breed() == layoutBreed ? layoutCoatId : DogCoatIds.donorFor(earShape.breed());
    return DogCoatTextures.composited(layoutCoatId, donors, earShape, earDonorCoatId);
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
