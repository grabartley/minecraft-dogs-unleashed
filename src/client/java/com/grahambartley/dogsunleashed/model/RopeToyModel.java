package com.grahambartley.dogsunleashed.model;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.item.RopeToyItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class RopeToyModel extends GeoModel<RopeToyItem> {

  public static final Identifier GEOMETRY = Identifier.of(MOD_ID, "geo/rope_toy.geo.json");
  public static final Identifier TEXTURE = Identifier.of(MOD_ID, "textures/item/rope_toy.png");
  public static final Identifier ANIMATIONS =
      Identifier.of(MOD_ID, "animations/rope_toy.animation.json");

  @Override
  public Identifier getModelResource(final RopeToyItem animatable) {
    return GEOMETRY;
  }

  @Override
  public Identifier getTextureResource(final RopeToyItem animatable) {
    return TEXTURE;
  }

  @Override
  public Identifier getAnimationResource(final RopeToyItem animatable) {
    return ANIMATIONS;
  }
}
