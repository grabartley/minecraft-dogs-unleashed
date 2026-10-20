package com.grahambartley.dogsunleashed.render.costume;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class CostumePartsModel extends GeoModel<UnleashedDogEntity> {

  private final CostumeAssets assets;

  public CostumePartsModel(final CostumeAssets assets) {
    this.assets = assets;
  }

  @Override
  public Identifier getModelResource(final UnleashedDogEntity animatable) {
    return this.assets.partsModel();
  }

  @Override
  public Identifier getTextureResource(final UnleashedDogEntity animatable) {
    return this.assets.partsTexture();
  }

  @Override
  public Identifier getAnimationResource(final UnleashedDogEntity animatable) {
    return this.assets.partsAnimation();
  }
}
