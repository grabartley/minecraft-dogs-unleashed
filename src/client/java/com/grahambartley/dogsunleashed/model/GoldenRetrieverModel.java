package com.grahambartley.dogsunleashed.model;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.grahambartley.dogsunleashed.entity.GoldenRetrieverEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class GoldenRetrieverModel extends GeoModel<GoldenRetrieverEntity> {

  @Override
  public Identifier getModelResource(GoldenRetrieverEntity animatable) {
    return Identifier.of(MOD_ID, "geo/goldenretriever.geo.json");
  }

  @Override
  public Identifier getTextureResource(GoldenRetrieverEntity animatable) {
    return Identifier.of(MOD_ID, "textures/entity/goldenretriever.png");
  }

  @Override
  public Identifier getAnimationResource(GoldenRetrieverEntity animatable) {
    return Identifier.of(MOD_ID, "animations/goldenretriever.animation.json");
  }
}
