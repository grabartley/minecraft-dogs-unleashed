package com.grahambartley.dogsunleashed.entity.sled;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SledSpeedTest {

  private static final double EPSILON = 1.0e-6;

  @ParameterizedTest(name = "{0} dogs -> {1}")
  @CsvSource({"0, 0.0", "1, 0.4444444", "2, 0.6666667", "4, 0.8888889", "6, 1.0"})
  @DisplayName("a bigger team pulls faster, up to full speed with six")
  void teamFactor(final int size, final double expected) {
    assertEquals(expected, SledSpeed.teamFactor(size), 1.0e-6);
  }

  @ParameterizedTest(name = "{0} dogs")
  @ValueSource(ints = {1, 2, 3, 4, 5})
  @DisplayName("each extra dog adds speed")
  void moreDogsAreFaster(final int size) {
    assertTrue(SledSpeed.teamFactor(size + 1) > SledSpeed.teamFactor(size));
  }

  @ParameterizedTest(name = "{0} dogs on {1}, sprinting {2} -> {3} blocks a tick")
  @CsvSource({
    "6, ICE, false, 0.42",
    "6, ICE, true, 0.672",
    "6, SNOW, false, 0.336",
    "2, SNOW, false, 0.224",
    "2, GROUND, false, 0.112",
    "2, SAND, false, 0.042",
    "0, ICE, true, 0.0"
  })
  @DisplayName(
      "cruising speed is top speed scaled by team size and surface, and sprinting adds 60%")
  void cruise(
      final int size, final SledSurface surface, final boolean sprinting, final double expected) {
    assertEquals(expected, SledSpeed.cruise(size, surface.speedFactor(), sprinting), EPSILON);
  }

  @ParameterizedTest(name = "{0} toward {1}, braking {2} -> {3}")
  @CsvSource({
    "0.0, 0.3, false, 0.012",
    "0.295, 0.3, false, 0.3",
    "0.3, 0.0, false, 0.28",
    "0.3, 0.0, true, 0.25",
    "0.01, 0.0, true, 0.0",
    "0.2, 0.2, false, 0.2"
  })
  @DisplayName("the sled eases up to speed, coasts down and brakes harder")
  void approach(
      final double current, final double target, final boolean braking, final double expected) {
    assertEquals(expected, SledSpeed.approach(current, target, braking), EPSILON);
  }

  @ParameterizedTest(name = "{0} blocks a tick at attribute {1} -> x{2}")
  @CsvSource({
    "0.1943, 0.3, 1.0",
    "0.7772, 0.3, 2.0",
    "0.0, 0.3, 0.6",
    "5.0, 0.3, 3.0",
    "0.2, 0.0, 0.6"
  })
  @DisplayName("dogs walk at the multiplier vanilla walking needs to cover that distance")
  void dogSpeedMultiplier(
      final double blocksPerTick, final double attribute, final double expected) {
    assertEquals(expected, SledSpeed.dogSpeedMultiplier(blocksPerTick, attribute), 1.0e-3);
  }
}
