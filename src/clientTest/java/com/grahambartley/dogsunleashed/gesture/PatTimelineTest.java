package com.grahambartley.dogsunleashed.gesture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PatTimelineTest {

  private static final float LENGTH = 2.5f;
  private static final long START = 1000L;

  @ParameterizedTest(name = "{0} ticks after the click -> clip at {1}s")
  @CsvSource({"0, 0", "6, 0.3", "12, 0.6", "30, 0.6", "60, 0.6"})
  @DisplayName("while the dog walks over the player kneels, then holds the kneel")
  void theInviteKneelsThenHolds(final float ticks, final float clipTime) {
    final PatTimeline.Moment moment =
        PatTimeline.at(PatTimeline.invited(START), START + ticks, LENGTH).orElseThrow();
    assertEquals(clipTime, moment.clipTime(), 1.0e-4f);
  }

  @ParameterizedTest(name = "pat {0} ticks in -> clip at {1}s")
  @CsvSource({"0, 0.6", "10, 1.1", "30, 2.1", "36, 2.4"})
  @DisplayName("the strokes start when the dog arrives and run into the rise")
  void theStrokesRunFromArrival(final float ticks, final float clipTime) {
    final PatTimeline.Session session = PatTimeline.invited(START).patted(START + 20);
    assertEquals(
        clipTime,
        PatTimeline.at(session, START + 20 + ticks, LENGTH).orElseThrow().clipTime(),
        1.0e-4f);
  }

  @ParameterizedTest(name = "{0} ticks after the pat")
  @CsvSource({"38", "60"})
  @DisplayName("the pose ends once the player has stood up")
  void thePoseEndsAfterTheRise(final float ticks) {
    final PatTimeline.Session session = PatTimeline.invited(START).patted(START + 12);
    assertEquals(Optional.empty(), PatTimeline.at(session, START + 12 + ticks, LENGTH));
  }

  @ParameterizedTest(name = "released {0} ticks after the click")
  @CsvSource({"3", "8", "20"})
  @DisplayName("a call-off rises from wherever the player is, without a jump")
  void aCallOffRisesSmoothly(final long releasedAfter) {
    final PatTimeline.Session session = PatTimeline.invited(START).released(START + releasedAfter);
    final PatTimeline.Moment atRelease =
        PatTimeline.at(session, START + releasedAfter, LENGTH).orElseThrow();
    assertEquals(0.0f, atRelease.fade(), 1.0e-4f);
    assertEquals(
        Math.min(releasedAfter / 20.0f, PatTimeline.KNEEL_END), atRelease.fromClipTime(), 1.0e-4f);
    final PatTimeline.Moment later =
        PatTimeline.at(session, START + releasedAfter + 4, LENGTH).orElseThrow();
    assertEquals(1.0f, later.fade(), 1.0e-4f);
    assertTrue(later.clipTime() >= PatTimeline.STROKES_END, "the rise plays");
  }

  @ParameterizedTest(name = "invite left for {0} ticks")
  @CsvSource({"68", "75"})
  @DisplayName("a walk over that never ends in a pat rises on its own")
  void anUnansweredInviteRises(final float ticks) {
    final PatTimeline.Moment moment =
        PatTimeline.at(PatTimeline.invited(START), START + ticks, LENGTH).orElseThrow();
    assertTrue(moment.clipTime() >= PatTimeline.STROKES_END, "the player gets up");
  }

  @ParameterizedTest(name = "clip at {0}s -> aim {1}")
  @CsvSource({"0, 0", "0.3, 0.5", "0.6, 1", "1.0, 1", "2.1, 1", "2.25, 0.5", "2.4, 0"})
  @DisplayName("the hand eases onto the dog and off it again")
  void theAimEasesInAndOut(final float time, final float aim) {
    assertEquals(aim, PatTimeline.aimAt(time), 1.0e-4f);
  }

  @ParameterizedTest(name = "clip at {0}s -> body {1}")
  @CsvSource({"0, 0", "0.05, 0.5", "0.1, 1", "1.0, 1", "2.45, 0.5", "2.5, 0"})
  @DisplayName("the body takes over from walking and hands back to it smoothly")
  void theBodyBlendsInAndOut(final float time, final float body) {
    assertEquals(body, PatTimeline.bodyAt(time, LENGTH), 1.0e-4f);
  }
}
