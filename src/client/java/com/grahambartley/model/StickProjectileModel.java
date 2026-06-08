package com.grahambartley.model;

import static com.grahambartley.DogsUnleashed.MOD_ID;

import com.grahambartley.entity.StickProjectileEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class StickProjectileModel extends GeoModel<StickProjectileEntity> {

  @Override
  public Identifier getModelResource(StickProjectileEntity animatable) {
    return Identifier.of(MOD_ID, "geo/stick.geo.json");
  }

  @Override
  public Identifier getTextureResource(StickProjectileEntity animatable) {
    return Identifier.of(MOD_ID, "textures/block/stick.png");
  }

  @Override
  public Identifier getAnimationResource(StickProjectileEntity animatable) {
    return Identifier.of(MOD_ID, "animations/stick.animation.json");
  }
}
