package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.bernie.geckolib.cache.object.BakedGeoModel;

class SlotBoundsTest {

  private static final float EPSILON = 1.0e-5f;

  @Test
  @DisplayName("a slot's bounds take in every cube of every named bone")
  void unionOfNamedBones() {
    final BakedGeoModel beagle = ShippedRigs.model("beagle");
    final CubeGeometry.Bounds head = SlotBounds.of(beagle, Set.of("head"));
    final CubeGeometry.Bounds both = SlotBounds.of(beagle, Set.of("head", "snout"));

    assertTrue(both.min().z <= head.min().z);
    assertEquals(
        head.max().y,
        CubeGeometry.posedBoundsOf(beagle.getBone("head").orElseThrow()).max().y,
        EPSILON);
  }

  @Test
  @DisplayName("names the rig does not have are skipped")
  void missingBonesAreSkipped() {
    assertTrue(SlotBounds.of(ShippedRigs.model("beagle"), Set.of("no_such_bone")).isEmpty());
  }
}
