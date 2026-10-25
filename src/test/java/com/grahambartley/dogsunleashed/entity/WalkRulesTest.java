package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class WalkRulesTest {

  static Stream<Arguments> cases() {
    return Stream.of(
        Arguments.of(0.0f, false, false),
        Arguments.of(WalkRules.START_LIMB_SPEED, false, false),
        Arguments.of(Math.nextUp(WalkRules.START_LIMB_SPEED), false, true),
        Arguments.of(0.4f, false, true),
        Arguments.of(0.0f, true, false),
        Arguments.of(WalkRules.STOP_LIMB_SPEED, true, false),
        Arguments.of(Math.nextUp(WalkRules.STOP_LIMB_SPEED), true, true),
        Arguments.of(WalkRules.START_LIMB_SPEED, true, true),
        Arguments.of(1.0f, true, true));
  }

  @ParameterizedTest(name = "limb speed {0}, walking now {1} -> walks {2}")
  @MethodSource("cases")
  @DisplayName(
      "dogs start walking above the start speed and keep walking until below the stop speed")
  void walks(final float limbSpeed, final boolean walkingNow, final boolean expected) {
    assertEquals(expected, WalkRules.walks(limbSpeed, walkingNow));
  }
}
