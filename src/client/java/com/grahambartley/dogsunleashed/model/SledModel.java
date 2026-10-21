package com.grahambartley.dogsunleashed.model;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

public class SledModel<T extends GeoAnimatable> extends GeoModel<T> {

  public static final Identifier GEOMETRY = Identifier.of(MOD_ID, "geo/sled.geo.json");
  public static final Identifier TEXTURE = Identifier.of(MOD_ID, "textures/entity/sled.png");
  public static final Identifier ANIMATIONS =
      Identifier.of(MOD_ID, "animations/sled.animation.json");

  @Override
  public Identifier getModelResource(final T animatable) {
    return GEOMETRY;
  }

  @Override
  public Identifier getTextureResource(final T animatable) {
    return TEXTURE;
  }

  @Override
  public Identifier getAnimationResource(final T animatable) {
    return ANIMATIONS;
  }
}
