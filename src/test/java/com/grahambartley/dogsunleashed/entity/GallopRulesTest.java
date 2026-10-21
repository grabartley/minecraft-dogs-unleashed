package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GallopRulesTest {

  @ParameterizedTest(name = "ridden {0}, hitched {1}, limb speed {2}, run clip {3} -> {4}")
  @CsvSource({
    "true, false, 0.2, true, true",
    "true, false, 0.05, true, false",
    "false, true, 0.6, true, true",
    "false, true, 0.3, true, false",
    "false, true, 0.9, false, false",
    "false, false, 0.9, true, false",
    "true, true, 0.2, true, true"
  })
  @DisplayName(
      "ridden dogs gallop when moving, hitched dogs run at speed only if the rig has a run")
  void gallops(
      final boolean ridden,
      final boolean hitched,
      final float limbSpeed,
      final boolean hasRunClip,
      final boolean expected) {
    assertEquals(expected, GallopRules.gallops(ridden, hitched, limbSpeed, hasRunClip));
  }
}
