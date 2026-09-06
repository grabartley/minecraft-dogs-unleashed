package com.grahambartley.dogsunleashed.model;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import com.grahambartley.dogsunleashed.entity.variant.UnleashedDogCoat;
import com.grahambartley.dogsunleashed.render.coat.UndeadCoatTextures;
import com.grahambartley.dogsunleashed.render.coat.UndeadDogEyes;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class DogModel extends GeoModel<UnleashedDogEntity> {

  @Override
  public Identifier getModelResource(final UnleashedDogEntity animatable) {
    return Identifier.of(
        MOD_ID, "geo/" + animatable.getRigSourceBreed().serializedId() + ".geo.json");
  }

  @Override
  public Identifier getTextureResource(final UnleashedDogEntity animatable) {
    final Identifier living =
        Identifier.of(MOD_ID, "textures/entity/" + flatTextureName(animatable) + ".png");
    if (!animatable.isUndead()) {
      return living;
    }
    return UndeadCoatTextures.undeadOf(living, UndeadDogEyes.of(animatable.getRigSourceBreed()));
  }

  @Override
  public Identifier getAnimationResource(final UnleashedDogEntity animatable) {
    return Identifier.of(MOD_ID, animatable.getRigSourceBreed().animationPath());
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
