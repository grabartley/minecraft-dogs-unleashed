package com.grahambartley.model;

import static com.grahambartley.DogsUnleashed.MOD_ID;

import com.grahambartley.block.entity.TennisBallBlockEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class TennisBallModel extends GeoModel<TennisBallBlockEntity> {

  @Override
  public Identifier getModelResource(TennisBallBlockEntity animatable) {
    return Identifier.of(MOD_ID, "geo/tennis_ball.geo.json");
  }

  @Override
  public Identifier getTextureResource(TennisBallBlockEntity animatable) {
    return Identifier.of(MOD_ID, "textures/block/tennis_ball.png");
  }

  @Override
  public Identifier getAnimationResource(TennisBallBlockEntity animatable) {
    return Identifier.of(MOD_ID, "animations/tennis_ball.animation.json");
  }
}
