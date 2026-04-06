package com.grahambartley.model;

import static com.grahambartley.DogsUnleashed.MOD_ID;

import com.grahambartley.entity.ShibaInuEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class ShibaInuModel extends GeoModel<ShibaInuEntity> {

  @Override
  public Identifier getModelResource(ShibaInuEntity animatable) {
    return Identifier.of(MOD_ID, "geo/shibainu.geo.json");
  }

  @Override
  public Identifier getTextureResource(ShibaInuEntity animatable) {
    final String fileName = "shibainu_" + animatable.getCoatVariant().getTexturePrefix() + ".png";
    return Identifier.of(MOD_ID, "textures/entity/" + fileName);
  }

  @Override
  public Identifier getAnimationResource(ShibaInuEntity animatable) {
    return Identifier.of(MOD_ID, "animations/shibainu.animation.json");
  }
}
