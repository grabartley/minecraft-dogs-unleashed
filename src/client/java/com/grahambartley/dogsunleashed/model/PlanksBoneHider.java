package com.grahambartley.dogsunleashed.model;

import com.grahambartley.dogsunleashed.render.PlanksBones;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public final class PlanksBoneHider {

  private BakedGeoModel prepared;

  public BakedGeoModel prepare(final BakedGeoModel model) {
    if (model != this.prepared) {
      PlanksBones.hideIn(model);
      this.prepared = model;
    }
    return model;
  }
}
