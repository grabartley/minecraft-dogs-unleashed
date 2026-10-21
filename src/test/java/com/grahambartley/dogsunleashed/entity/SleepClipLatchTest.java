package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SleepClipLatchTest {

  private record Frame(long tick, boolean asleep, String chooserWouldPick) {}

  static Stream<Arguments> frames() {
    return Stream.of(
        Arguments.of(
            "keeps the first pick while asleep, even if the pick would change",
            List.of(
                new Frame(100, true, "sleep_2"),
                new Frame(101, true, "sleep"),
                new Frame(102, true, "sleep_3")),
            List.of("sleep_2", "sleep_2", "sleep_2")),
        Arguments.of(
            "picks again after waking",
            List.of(
                new Frame(100, true, "sleep_2"),
                new Frame(101, false, "sleep"),
                new Frame(102, true, "sleep_3")),
            List.of("sleep_2", "", "sleep_3")),
        Arguments.of(
            "keeps the pick across a short gap between rendered frames",
            List.of(
                new Frame(100, true, "sleep_2"),
                new Frame(100 + SleepClipLatch.STALE_TICKS, true, "sleep")),
            List.of("sleep_2", "sleep_2")),
        Arguments.of(
            "picks again after the dog was off screen for a while",
            List.of(
                new Frame(100, true, "sleep_2"),
                new Frame(101 + SleepClipLatch.STALE_TICKS, true, "sleep")),
            List.of("sleep_2", "sleep")),
        Arguments.of(
            "picks again when the clock jumps back",
            List.of(new Frame(5000, true, "sleep_2"), new Frame(100, true, "sleep")),
            List.of("sleep_2", "sleep")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("frames")
  @DisplayName("a sleeping dog holds one sleep clip until it wakes")
  void holds(final String scenario, final List<Frame> frames, final List<String> expected) {
    final SleepClipLatch latch = new SleepClipLatch();
    final Deque<String> played = new ArrayDeque<>();
    for (final Frame frame : frames) {
      if (frame.asleep()) {
        played.add(latch.hold(frame.tick(), frame::chooserWouldPick));
      } else {
        latch.release();
        played.add("");
      }
    }
    assertEquals(expected, List.copyOf(played));
  }
}
