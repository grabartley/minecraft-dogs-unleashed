package com.grahambartley.dogsunleashed.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.bernie.geckolib.cache.object.BakedGeoModel;

class TennisBallModelTest {

  @Test
  @DisplayName("every bone the fly animation turns exists in the geometry")
  void flyAnimationDrivesRealBones() {
    final BakedGeoModel model = ShippedRigs.model("tennis_ball");
    final List<String> bones = ShippedRigs.animatedBones("tennis_ball", "fly");

    assertFalse(bones.isEmpty());
    for (final String bone : bones) {
      assertTrue(model.getBone(bone).isPresent(), "missing bone " + bone);
    }
  }
}
