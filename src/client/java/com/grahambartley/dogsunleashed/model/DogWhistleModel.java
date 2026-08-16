package com.grahambartley.dogsunleashed.model;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.item.DogWhistleItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class DogWhistleModel extends GeoModel<DogWhistleItem> {

  @Override
  public Identifier getModelResource(DogWhistleItem animatable) {
    return Identifier.of(MOD_ID, "geo/dog_whistle.geo.json");
  }

  @Override
  public Identifier getTextureResource(DogWhistleItem animatable) {
    return Identifier.of(MOD_ID, "textures/item/dog_whistle.png");
  }

  @Override
  public Identifier getAnimationResource(DogWhistleItem animatable) {
    return Identifier.of(MOD_ID, "animations/dog_whistle.animation.json");
  }
}
