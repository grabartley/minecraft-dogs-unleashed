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
}
