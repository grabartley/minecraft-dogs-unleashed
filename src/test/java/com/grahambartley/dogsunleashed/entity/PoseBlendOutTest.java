package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.PoseBlendOut.Step;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PoseBlendOutTest {

  static Stream<Arguments> cases() {
    return Stream.of(
        Arguments.of(
            "a held pose hands back to a running movement clip", true, true, Step.BLEND_BACK),
        Arguments.of(
            "a held pose hands back while movement is still blending",
            true,
            false,
            Step.BLEND_BACK),
        Arguments.of(
            "a running movement clip goes back to switching instantly", false, true, Step.SNAP),
        Arguments.of("a movement clip mid blend keeps its blend", false, false, Step.KEEP));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("cases")
  @DisplayName("the movement clip blends back only when a pose has finished and is holding")
  void steps(
      final String scenario,
      final boolean poseHeld,
      final boolean movementRunning,
      final Step expected) {
    assertEquals(expected, PoseBlendOut.step(poseHeld, movementRunning));
  }
}
