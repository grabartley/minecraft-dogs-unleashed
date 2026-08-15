package com.grahambartley.dogsunleashed.model;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import com.grahambartley.dogsunleashed.entity.variant.UnleashedDogCoat;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class DogModel extends GeoModel<UnleashedDogEntity> {

  @Override
  public Identifier getModelResource(UnleashedDogEntity animatable) {
    return Identifier.of(MOD_ID, "geo/" + animatable.getBreedId() + ".geo.json");
  }

  @Override
  public Identifier getTextureResource(UnleashedDogEntity animatable) {
    final StringBuilder fileName = new StringBuilder(animatable.getBreedId());
    final UnleashedDogCoat coat = animatable.getCoatVariant();
    if (coat != null) {
      fileName.append("_").append(coat.getTexturePrefix());
    }
    final HuskyEyeColor eyeColor = animatable.getEyeColorVariant();
    if (eyeColor != null) {
      fileName.append("_").append(eyeColor.textureSuffix());
    }
    return Identifier.of(MOD_ID, "textures/entity/" + fileName + ".png");
  }

  @Override
  public Identifier getAnimationResource(UnleashedDogEntity animatable) {
    return Identifier.of(MOD_ID, animatable.getBreed().animationPath());
  }
}
