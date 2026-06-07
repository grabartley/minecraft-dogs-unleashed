package com.grahambartley.model;

import static com.grahambartley.DogsUnleashed.MOD_ID;

import com.grahambartley.entity.BeagleEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class BeagleModel extends GeoModel<BeagleEntity> {

  @Override
  public Identifier getModelResource(BeagleEntity animatable) {
    return Identifier.of(MOD_ID, "geo/beagle.geo.json");
  }

  @Override
  public Identifier getTextureResource(BeagleEntity animatable) {
    final String fileName =
        animatable.getBreed().serializedId()
            + "_"
            + animatable.getCoatVariant().getTexturePrefix()
            + ".png";
    return Identifier.of(MOD_ID, "textures/entity/" + fileName);
  }

  @Override
  public Identifier getAnimationResource(BeagleEntity animatable) {
    return Identifier.of(MOD_ID, animatable.getBreed().animationPath());
  }
}
