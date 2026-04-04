package com.grahambartley.model;

import static com.grahambartley.DogsUnleashed.MOD_ID;

import com.grahambartley.entity.HuskyEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class HuskyModel extends GeoModel<HuskyEntity> {

  @Override
  public Identifier getModelResource(HuskyEntity animatable) {
    return Identifier.of(MOD_ID, "geo/husky.geo.json");
  }

  @Override
  public Identifier getTextureResource(HuskyEntity animatable) {
    final String fileName =
        "husky_"
            + animatable.getCoatVariant().textureCoatPrefix()
            + "_"
            + animatable.getEyeColorVariant().textureSuffix()
            + ".png";
    return Identifier.of(MOD_ID, "textures/entity/" + fileName);
  }

  @Override
  public Identifier getAnimationResource(HuskyEntity animatable) {
    return Identifier.of(MOD_ID, "animations/husky.animation.json");
  }
}
