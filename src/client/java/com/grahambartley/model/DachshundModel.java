package com.grahambartley.model;

import static com.grahambartley.DogsUnleashed.MOD_ID;

import com.grahambartley.entity.DachshundEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class DachshundModel extends GeoModel<DachshundEntity> {

  @Override
  public Identifier getModelResource(DachshundEntity animatable) {
    return Identifier.of(MOD_ID, "geo/dachshund.geo.json");
  }

  @Override
  public Identifier getTextureResource(DachshundEntity animatable) {
    return Identifier.of(MOD_ID, "textures/entity/dachshund.png");
  }

  @Override
  public Identifier getAnimationResource(DachshundEntity animatable) {
    return Identifier.of(MOD_ID, "animations/dachshund.animation.json");
  }
}
