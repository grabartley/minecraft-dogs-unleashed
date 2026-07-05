package com.grahambartley.dogsunleashed.model;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.entity.ShibaInuEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class ShibaInuModel extends GeoModel<ShibaInuEntity> {

  @Override
  public Identifier getModelResource(ShibaInuEntity animatable) {
    return Identifier.of(MOD_ID, "geo/shibainu.geo.json");
  }

  @Override
  public Identifier getTextureResource(ShibaInuEntity animatable) {
    final String fileName =
        animatable.getBreed().serializedId()
            + "_"
            + animatable.getCoatVariant().getTexturePrefix()
            + ".png";
    return Identifier.of(MOD_ID, "textures/entity/" + fileName);
  }

  @Override
  public Identifier getAnimationResource(ShibaInuEntity animatable) {
    return Identifier.of(MOD_ID, animatable.getBreed().animationPath());
  }
}
