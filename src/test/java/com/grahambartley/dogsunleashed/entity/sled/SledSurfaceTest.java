package com.grahambartley.dogsunleashed.entity.sled;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class SledSurfaceTest {

  @ParameterizedTest(name = "snow {0}, slipperiness {1}, sand {2} -> {3}")
  @CsvSource({
    "false, 0.98, false, ICE",
    "false, 0.989, false, ICE",
    "true, 0.98, false, ICE",
    "true, 0.6, false, SNOW",
    "false, 0.6, true, SAND",
    "false, 0.6, false, GROUND",
    "false, 0.8, false, GROUND",
    "true, 0.6, true, SNOW"
  })
  @DisplayName("ice is told by slipperiness, then snow, then sand, and everything else is ground")
  void classify(
      final boolean snow,
      final float slipperiness,
      final boolean sand,
      final SledSurface expected) {
    assertEquals(expected, SledSurface.classify(snow, slipperiness, sand));
  }

  @Test
  @DisplayName("the surfaces rank ice, snow, ground, sand from fastest to slowest")
  void ranking() {
    assertTrue(SledSurface.ICE.speedFactor() > SledSurface.SNOW.speedFactor());
    assertTrue(SledSurface.SNOW.speedFactor() > SledSurface.GROUND.speedFactor());
    assertTrue(SledSurface.GROUND.speedFactor() > SledSurface.SAND.speedFactor());
  }

  static Stream<Arguments> teams() {
    return Stream.of(
        Arguments.of(List.of(), 0.0),
        Arguments.of(List.of(SledSurface.ICE), 1.0),
        Arguments.of(List.of(SledSurface.ICE, SledSurface.GROUND), 0.7),
        Arguments.of(List.of(SledSurface.SNOW, SledSurface.SNOW, SledSurface.SAND), 0.5833333));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("teams")
  @DisplayName("a team's surface is the average under its dogs")
  void average(final List<SledSurface> surfaces, final double expected) {
    assertEquals(expected, SledSurface.averageFactor(surfaces), 1.0e-6);
  }
}
