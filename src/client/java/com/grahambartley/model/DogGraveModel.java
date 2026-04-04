package com.grahambartley.model;

import com.grahambartley.DogsUnleashed;
import com.grahambartley.block.entity.DogGraveBlockEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class DogGraveModel extends GeoModel<DogGraveBlockEntity> {

  private static final Identifier MODEL =
      Identifier.of(DogsUnleashed.MOD_ID, "geo/dog_grave.geo.json");
  private static final Identifier TEXTURE =
      Identifier.of(DogsUnleashed.MOD_ID, "textures/block/dog_grave.png");
  private static final Identifier ANIMATIONS =
      Identifier.of(DogsUnleashed.MOD_ID, "animations/dog_grave.animation.json");

  @Override
  public Identifier getModelResource(DogGraveBlockEntity blockEntity) {
    return MODEL;
  }

  @Override
  public Identifier getTextureResource(DogGraveBlockEntity blockEntity) {
    return TEXTURE;
  }

  @Override
  public Identifier getAnimationResource(DogGraveBlockEntity blockEntity) {
    return ANIMATIONS;
  }
}
