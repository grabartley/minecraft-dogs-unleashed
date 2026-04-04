package com.grahambartley.model;

import static com.grahambartley.DogsUnleashed.MOD_ID;

import com.grahambartley.entity.HuskyEntity;
import com.grahambartley.entity.variant.HuskyCoat;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class HuskyModel extends GeoModel<HuskyEntity> {

  @Override
  public Identifier getModelResource(HuskyEntity animatable) {
    return Identifier.of(MOD_ID, "geo/husky.geo.json");
  }

  @Override
  public Identifier getTextureResource(HuskyEntity animatable) {
    return Identifier.of(
        MOD_ID, "textures/entity/" + textureNameForCoat(animatable.getCoatVariant()));
  }

  @Override
  public Identifier getAnimationResource(HuskyEntity animatable) {
    return Identifier.of(MOD_ID, "animations/husky.animation.json");
  }

  private String textureNameForCoat(HuskyCoat coat) {
    return switch (coat) {
      case BLACK_WHITE -> "husky_black_white.png";
      case GREY_WHITE -> "husky_grey_white.png";
      case AGOUTI -> "husky_agouti.png";
      case RED_WHITE -> "husky_red_white.png";
      case SABLE -> "husky_sable.png";
      case WHITE -> "husky_white.png";
    };
  }
}
