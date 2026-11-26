package com.grahambartley.dogsunleashed.model;

import net.minecraft.util.Identifier;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;

public abstract class PlanksGeoModel<T extends GeoAnimatable> extends GeoModel<T> {

  private final PlanksBoneHider hider = new PlanksBoneHider();

  @Override
  public BakedGeoModel getBakedModel(final Identifier location) {
    return this.hider.prepare(super.getBakedModel(location));
  }
}
