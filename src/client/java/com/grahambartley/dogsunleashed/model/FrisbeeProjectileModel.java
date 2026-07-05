package com.grahambartley.dogsunleashed.model;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.entity.FrisbeeProjectileEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class FrisbeeProjectileModel extends GeoModel<FrisbeeProjectileEntity> {

  @Override
  public Identifier getModelResource(FrisbeeProjectileEntity animatable) {
    return Identifier.of(MOD_ID, "geo/frisbee.geo.json");
  }

  @Override
  public Identifier getTextureResource(FrisbeeProjectileEntity animatable) {
    return Identifier.of(MOD_ID, "textures/block/frisbee.png");
  }

  @Override
  public Identifier getAnimationResource(FrisbeeProjectileEntity animatable) {
    return Identifier.of(MOD_ID, "animations/frisbee.animation.json");
  }
}
