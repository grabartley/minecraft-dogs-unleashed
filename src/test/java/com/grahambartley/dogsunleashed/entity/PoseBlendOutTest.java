package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.PoseBlendOut.Step;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PoseBlendOutTest {

  static Stream<Arguments> steps() {
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
  @MethodSource("steps")
  @DisplayName("the movement clip blends back only when a pose has finished and is holding")
  void steps(
      final String scenario,
      final boolean poseHeld,
      final boolean movementRunning,
      final Step expected) {
    assertEquals(expected, PoseBlendOut.step(poseHeld, movementRunning));
  }

  static Stream<Arguments> resets() {
    return Stream.of(
        Arguments.of("never handed back", null, 0L, (double) PoseBlendOut.RESET_TICKS),
        Arguments.of(
            "never handed back, late in the world",
            null,
            90_000L,
            (double) PoseBlendOut.RESET_TICKS),
        Arguments.of("the tick of the hand-back", 100L, 100L, (double) PoseBlendOut.TICKS),
        Arguments.of(
            "the last tick of the blend",
            100L,
            100L + PoseBlendOut.TICKS,
            (double) PoseBlendOut.TICKS),
        Arguments.of(
            "after the blend", 100L, 101L + PoseBlendOut.TICKS, (double) PoseBlendOut.RESET_TICKS),
        Arguments.of("the clock jumped back", 5000L, 100L, (double) PoseBlendOut.RESET_TICKS));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("resets")
  @DisplayName("bones ease back slowly only during a hand-back")
  void resets(
      final String scenario, final Long handedBackAt, final long now, final double expected) {
    final PoseBlendOut blend = new PoseBlendOut();
    if (handedBackAt != null) {
      blend.handBack(handedBackAt);
    }
    assertEquals(expected, blend.boneResetTicks(now));
  }
}
