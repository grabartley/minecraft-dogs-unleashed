package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SliderScaleTest {

  private static final SliderScale PERCENT = new SliderScale(0, 500);

  @ParameterizedTest(name = "{0} sits at {1} of the track")
  @CsvSource({"0, 0", "250, 0.5", "500, 1", "-50, 0", "900, 1"})
  @DisplayName("a value maps to its place on the track, clamped to the ends")
  void fractionOf(final double value, final double fraction) {
    assertEquals(fraction, PERCENT.fractionOf(value), 1e-9);
  }

  @ParameterizedTest(name = "{0} of the track is {1}")
  @CsvSource({"0, 0", "0.5, 250", "1, 500"})
  @DisplayName("a place on the track maps back to its value")
  void valueAt(final double fraction, final double value) {
    assertEquals(value, PERCENT.valueAt(fraction), 1e-9);
  }

  @ParameterizedTest(name = "{0} round trips")
  @CsvSource({"4", "32", "77", "128"})
  @DisplayName("a value survives the trip to the track and back")
  void roundTrips(final double value) {
    final SliderScale range = new SliderScale(4, 128);
    assertEquals(value, range.valueAt(range.fractionOf(value)), 1e-9);
  }
}
