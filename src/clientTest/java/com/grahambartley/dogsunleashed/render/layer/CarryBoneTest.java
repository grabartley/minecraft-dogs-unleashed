package com.grahambartley.dogsunleashed.render.layer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CarryBoneTest {

  @ParameterizedTest(name = "frisbee {0}, fetched item {1} -> {2}")
  @CsvSource({"true, false, jaw", "true, true, snout", "false, false, snout", "false, true, snout"})
  @DisplayName("a carried frisbee hangs off the grip bone, everything else off the mouth bone")
  void picksTheBone(final boolean frisbee, final boolean fetchedItem, final String expected) {
    assertEquals(
        Optional.of(expected),
        CarryBone.of(frisbee, fetchedItem, Optional.of("jaw"), Optional.of("snout")));
  }

  @ParameterizedTest(name = "frisbee {0}")
  @CsvSource({"true", "false"})
  @DisplayName("a rig with no grip or mouth bone carries nothing there")
  void missingBoneCarriesNothing(final boolean frisbee) {
    assertEquals(
        Optional.empty(), CarryBone.of(frisbee, false, Optional.empty(), Optional.empty()));
  }
}
