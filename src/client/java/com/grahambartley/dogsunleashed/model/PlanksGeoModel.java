package com.grahambartley.dogsunleashed.model;

import com.grahambartley.dogsunleashed.render.PlanksBones;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;

public abstract class PlanksGeoModel<T extends GeoAnimatable> extends GeoModel<T> {

  private BakedGeoModel prepared;

  @Override
  public BakedGeoModel getBakedModel(final Identifier location) {
    final BakedGeoModel model = super.getBakedModel(location);
    if (model != this.prepared) {
      PlanksBones.hideIn(model);
      this.prepared = model;
    }
    return model;
  }
}
