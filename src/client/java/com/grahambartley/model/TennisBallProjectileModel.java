package com.grahambartley.model;

import static com.grahambartley.DogsUnleashed.MOD_ID;

import com.grahambartley.entity.TennisBallProjectileEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class TennisBallProjectileModel extends GeoModel<TennisBallProjectileEntity> {

  @Override
  public Identifier getModelResource(TennisBallProjectileEntity animatable) {
    return Identifier.of(MOD_ID, "geo/tennis_ball.geo.json");
  }

  @Override
  public Identifier getTextureResource(TennisBallProjectileEntity animatable) {
    return Identifier.of(MOD_ID, "textures/block/tennis_ball.png");
  }

  @Override
  public Identifier getAnimationResource(TennisBallProjectileEntity animatable) {
    return Identifier.of(MOD_ID, "animations/tennis_ball.animation.json");
  }
}
