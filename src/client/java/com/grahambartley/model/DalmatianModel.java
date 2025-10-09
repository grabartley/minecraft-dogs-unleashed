package com.grahambartley.model;

import static com.grahambartley.DogsUnleashed.MOD_ID;

import com.grahambartley.entity.DalmatianEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class DalmatianModel extends GeoModel<DalmatianEntity> {

  @Override
  public Identifier getModelResource(DalmatianEntity animatable) {
    return Identifier.of(MOD_ID, "geo/dalmatian.geo.json");
  }

  @Override
  public Identifier getTextureResource(DalmatianEntity animatable) {
    return Identifier.of(MOD_ID, "textures/entity/dalmatian.png");
  }

  @Override
  public Identifier getAnimationResource(DalmatianEntity animatable) {
    // No animations for this entity
    return null;
  }
}
