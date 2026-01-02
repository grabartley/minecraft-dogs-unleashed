package com.grahambartley.model;

import static com.grahambartley.DogsUnleashed.MOD_ID;

import com.grahambartley.block.entity.DogBedBlockEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class DogBedModel extends GeoModel<DogBedBlockEntity> {

  @Override
  public Identifier getModelResource(DogBedBlockEntity animatable) {
    return Identifier.of(MOD_ID, "geo/dog_bed.geo.json");
  }

  @Override
  public Identifier getTextureResource(DogBedBlockEntity animatable) {
    return Identifier.of(MOD_ID, "textures/block/dog_bed.png");
  }

  @Override
  public Identifier getAnimationResource(DogBedBlockEntity animatable) {
    return Identifier.of(MOD_ID, "animations/dog_bed.animation.json");
  }
}
