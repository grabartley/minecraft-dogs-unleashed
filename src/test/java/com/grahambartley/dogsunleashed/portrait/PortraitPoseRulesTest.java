package com.grahambartley.dogsunleashed.portrait;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PortraitPoseRulesTest {

  @ParameterizedTest(name = "portrait {0}, main hand {1}, empty hand {2} poses {3}")
  @CsvSource({
    "true, true, true, true",
    "false, true, true, false",
    "true, false, true, false",
    "true, true, false, false",
    "false, false, false, false"
  })
  @DisplayName("only an empty main hand on a painted portrait changes its pose")
  void posesOnlyWithAnEmptyMainHand(
      final boolean hasPortrait,
      final boolean mainHand,
      final boolean emptyHand,
      final boolean poses) {
    assertEquals(poses, PortraitPoseRules.poses(hasPortrait, mainHand, emptyHand));
  }
}
