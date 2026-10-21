package com.grahambartley.dogsunleashed.render.sled;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HarnessEyesTest {

  private static final List<Vector3f> EYES =
      List.of(
          new Vector3f(5, 0, 1),
          new Vector3f(3, 0, 1),
          new Vector3f(1, 0, 1),
          new Vector3f(-1, 0, 1),
          new Vector3f(-3, 0, 1),
          new Vector3f(-5, 0, 1));

  static Stream<Arguments> teams() {
    return Stream.of(
        Arguments.of(List.of(0.45, -0.45), List.of(5f, -5f)),
        Arguments.of(
            List.of(0.45, -0.45, 0.45, -0.45, 0.45, -0.45), List.of(5f, -5f, 3f, -3f, 1f, -1f)),
        Arguments.of(List.of(0.0), List.of(1f)),
        Arguments.of(List.of(0.45, -0.45, 0.0), List.of(5f, -5f, 1f)),
        Arguments.of(List.of(), List.of()));
  }

  @ParameterizedTest(name = "sides {0} -> eyes {1}")
  @MethodSource("teams")
  @DisplayName("the nearest pair takes the outer eyes, so lines to farther dogs run between them")
  void assign(final List<Double> sides, final List<Float> expectedX) {
    assertEquals(expectedX, HarnessEyes.assign(EYES, sides).stream().map(eye -> eye.x).toList());
  }

  @ParameterizedTest(name = "no eyes, sides {0}")
  @MethodSource("teams")
  @DisplayName("a sled with no eyes ties every line to its centre")
  void noEyes(final List<Double> sides, final List<Float> ignored) {
    HarnessEyes.assign(List.of(), sides).forEach(eye -> assertEquals(new Vector3f(), eye));
  }
}
