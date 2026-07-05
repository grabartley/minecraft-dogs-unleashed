package com.grahambartley.dogsunleashed.model;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

public abstract class BaseStickModel<T extends GeoAnimatable> extends GeoModel<T> {

  @Override
  public Identifier getModelResource(T animatable) {
    return Identifier.of(MOD_ID, "geo/stick.geo.json");
  }

  @Override
  public Identifier getTextureResource(T animatable) {
    return Identifier.of(MOD_ID, "textures/block/stick.png");
  }

  @Override
  public Identifier getAnimationResource(T animatable) {
    return Identifier.of(MOD_ID, "animations/stick.animation.json");
  }
}
