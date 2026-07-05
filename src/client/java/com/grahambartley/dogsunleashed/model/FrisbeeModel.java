package com.grahambartley.dogsunleashed.model;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.block.entity.FrisbeeBlockEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class FrisbeeModel extends GeoModel<FrisbeeBlockEntity> {

  @Override
  public Identifier getModelResource(FrisbeeBlockEntity animatable) {
    return Identifier.of(MOD_ID, "geo/frisbee.geo.json");
  }

  @Override
  public Identifier getTextureResource(FrisbeeBlockEntity animatable) {
    return Identifier.of(MOD_ID, "textures/block/frisbee.png");
  }

  @Override
  public Identifier getAnimationResource(FrisbeeBlockEntity animatable) {
    return Identifier.of(MOD_ID, "animations/frisbee.animation.json");
  }
}
