package com.grahambartley.model;

import static com.grahambartley.DogsUnleashed.MOD_ID;

import com.grahambartley.block.entity.StickBlockEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class StickModel extends GeoModel<StickBlockEntity> {

  @Override
  public Identifier getModelResource(StickBlockEntity animatable) {
    return Identifier.of(MOD_ID, "geo/stick.geo.json");
  }

  @Override
  public Identifier getTextureResource(StickBlockEntity animatable) {
    return Identifier.of(MOD_ID, "textures/block/stick.png");
  }

  @Override
  public Identifier getAnimationResource(StickBlockEntity animatable) {
    return Identifier.of(MOD_ID, "animations/stick.animation.json");
  }
}
