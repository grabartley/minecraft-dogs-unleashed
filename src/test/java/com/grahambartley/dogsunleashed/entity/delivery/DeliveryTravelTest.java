package com.grahambartley.dogsunleashed.entity.delivery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DeliveryTravelTest {

  @ParameterizedTest(name = "{0} blocks, other dimension {1} -> {2} ticks")
  @CsvSource({
    "0, false, 200",
    "40, false, 200",
    "80, false, 200",
    "400, false, 1000",
    "2400, false, 6000",
    "100000, false, 6000",
    "0, true, 800",
    "400, true, 1600",
    "100000, true, 6600"
  })
  @DisplayName("a leg takes eight blocks a second, within ten seconds and five minutes")
  void legTicks(final double blocks, final boolean crossDimension, final long expected) {
    assertEquals(expected, DeliveryTravel.legTicks(blocks, crossDimension));
  }

  @ParameterizedTest(name = "({0}, {1}) x{2} to ({3}, {4}) x{5} is {6}")
  @CsvSource({
    "0, 0, 1, 300, 400, 1, 500",
    "0, 0, 1, -30, 40, 1, 50",
    "100, 0, 8, 800, 0, 1, 0",
    "0, 0, 8, 0, 100, 1, 100",
    "10, 10, 8, 0, 0, 1, 113.137"
  })
  @DisplayName("distance compares both ends in overworld blocks")
  void distanceUsesCoordinateScale(
      final double fromX,
      final double fromZ,
      final double fromScale,
      final double toX,
      final double toZ,
      final double toScale,
      final double expected) {
    assertEquals(
        expected, DeliveryTravel.distance(fromX, fromZ, fromScale, toX, toZ, toScale), 0.001);
  }
}
