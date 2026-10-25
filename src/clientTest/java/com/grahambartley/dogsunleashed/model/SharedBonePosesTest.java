package com.grahambartley.dogsunleashed.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SharedBonePosesTest {

  private static final Object GOLDEN_BONES = new Object();
  private static final Object HUSKY_BONES = new Object();
  private static final Object PURE_MODEL = new Object();
  private static final Object CROSS_MODEL = new Object();
  private static final long LEXI = 7L;
  private static final long BRANDY = 9L;

  static Stream<Arguments> posers() {
    return Stream.of(
        Arguments.of("nobody has posed the bones yet", false, false, null, 0L, false),
        Arguments.of(
            "the same model posed the same dog last", true, false, CROSS_MODEL, LEXI, true),
        Arguments.of(
            "another model posed the shared bones", true, false, PURE_MODEL, BRANDY, false),
        Arguments.of("the same model posed another dog", true, false, CROSS_MODEL, BRANDY, false),
        Arguments.of(
            "another model posed a different bone set", true, true, PURE_MODEL, BRANDY, true));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("posers")
  @DisplayName("a dog's pose is only reusable while nobody else has posed its bones since")
  void lastPosedBy(
      final String scenario,
      final boolean lexiPosedFirst,
      final boolean otherUsesHuskyBones,
      final Object otherModel,
      final long otherInstance,
      final boolean reusable) {
    final SharedBonePoses<Object> poses = new SharedBonePoses<>();
    if (lexiPosedFirst) {
      poses.posedBy(GOLDEN_BONES, CROSS_MODEL, LEXI);
    }
    if (otherModel != null) {
      poses.posedBy(otherUsesHuskyBones ? HUSKY_BONES : GOLDEN_BONES, otherModel, otherInstance);
    }

    assertEquals(reusable, poses.lastPosedBy(GOLDEN_BONES, CROSS_MODEL, LEXI));
  }
}
