package com.grahambartley.dogsunleashed.gesture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PlayerGestureTest {

  @ParameterizedTest(name = "{0} lasts {1} ticks")
  @CsvSource({"WHISTLE, 14", "PAT, 30"})
  @DisplayName("each gesture has its own length")
  void eachGestureHasItsOwnLength(final PlayerGesture gesture, final int ticks) {
    assertEquals(ticks, gesture.ticks());
  }

  @ParameterizedTest(name = "{0} at {1} ticks -> playing {2}")
  @CsvSource({
    "WHISTLE, -0.5, false",
    "WHISTLE, 0, true",
    "WHISTLE, 13.9, true",
    "WHISTLE, 14, false",
    "PAT, -1, false",
    "PAT, 0, true",
    "PAT, 15, true",
    "PAT, 29.99, true",
    "PAT, 30, false",
    "PAT, 300, false"
  })
  @DisplayName("a gesture plays from its first tick up to its length")
  void aGesturePlaysForItsLength(
      final PlayerGesture gesture, final double elapsed, final boolean expected) {
    assertEquals(expected, gesture.isPlaying(elapsed));
  }

  @ParameterizedTest(name = "{0} at {1} ticks with a {2} tick ramp -> {3}")
  @CsvSource({
    "WHISTLE, -1, 3, 0",
    "WHISTLE, 0, 3, 0",
    "WHISTLE, 1.5, 3, 0.5",
    "WHISTLE, 3, 3, 1",
    "WHISTLE, 12.5, 3, 0.5",
    "WHISTLE, 14, 3, 0",
    "PAT, 2.5, 5, 0.5",
    "PAT, 15, 5, 1",
    "PAT, 27.5, 5, 0.5",
    "PAT, 30, 5, 0"
  })
  @DisplayName("a gesture ramps in at its start, holds, and ramps out at its end")
  void aGestureRampsInHoldsAndRampsOut(
      final PlayerGesture gesture, final float elapsed, final float ramp, final float expected) {
    assertEquals(expected, gesture.ramp(elapsed, ramp), 1.0e-5f);
  }
}
