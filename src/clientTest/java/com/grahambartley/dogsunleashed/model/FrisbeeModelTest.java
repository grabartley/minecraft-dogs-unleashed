package com.grahambartley.dogsunleashed.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.bernie.geckolib.cache.object.BakedGeoModel;

class FrisbeeModelTest {

  private final FrisbeeModel model = new FrisbeeModel();

  @Test
  @DisplayName("every bone the fly animation turns exists in the model's geometry")
  void flyAnimationDrivesRealBones() {
    final BakedGeoModel geometry = ShippedRigs.modelAt(model.getModelResource(null).getPath());
    final List<String> bones =
        ShippedRigs.animatedBones(model.getAnimationResource(null).getPath(), "fly");

    assertFalse(bones.isEmpty());
    for (final String bone : bones) {
      assertTrue(geometry.getBone(bone).isPresent(), "missing bone " + bone);
    }
  }
}
