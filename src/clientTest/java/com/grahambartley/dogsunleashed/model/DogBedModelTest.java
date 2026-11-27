package com.grahambartley.dogsunleashed.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.grahambartley.dogsunleashed.render.PlanksBones;
import com.grahambartley.dogsunleashed.render.rig.ShippedRigs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

class DogBedModelTest {

  @ParameterizedTest(name = "{0} drawn with planks: {1}")
  @CsvSource({"bb_main, false", "planks, true"})
  @DisplayName("the wood parts are split into planks bones drawn with vanilla's planks")
  void woodIsSplitIntoPlanksBones(final String name, final boolean planks) {
    final BakedGeoModel geometry =
        ShippedRigs.modelAt(new DogBedModel().getModelResource(null).getPath());
    PlanksBones.hideIn(geometry);

    final GeoBone bone = geometry.getBone(name).orElseThrow();
    assertEquals(planks, bone.isHidden());
    assertFalse(planks && bone.getCubes().isEmpty(), name + " has no cubes");
  }
}
