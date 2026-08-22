package com.grahambartley.dogsunleashed.model;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.block.entity.DogHouseBlockEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class DogHouseModel extends GeoModel<DogHouseBlockEntity> {

  private static final Identifier MODEL =
      Identifier.of(DogsUnleashed.MOD_ID, "geo/dog_house.geo.json");
  private static final Identifier TEXTURE =
      Identifier.of(DogsUnleashed.MOD_ID, "textures/block/dog_house.png");
  private static final Identifier ANIMATIONS =
      Identifier.of(DogsUnleashed.MOD_ID, "animations/dog_house.animation.json");

  @Override
  public Identifier getModelResource(DogHouseBlockEntity blockEntity) {
    return MODEL;
  }

  @Override
  public Identifier getTextureResource(DogHouseBlockEntity blockEntity) {
    return TEXTURE;
  }

  @Override
  public Identifier getAnimationResource(DogHouseBlockEntity blockEntity) {
    return ANIMATIONS;
  }
}
