package com.grahambartley.dogsunleashed.gesture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PatSessionsTest {

  private static final float LENGTH = 2.5f;
  private static final long START = 500L;

  @ParameterizedTest(name = "dog {0}")
  @CsvSource({"7", "1234"})
  @DisplayName("an invite aims the player at that dog and marks it as patted")
  void anInviteAimsAtTheDog(final int dog) {
    final PatSessions sessions = new PatSessions();
    sessions.show(1, PlayerGesture.INVITE, dog, START);
    assertEquals(dog, sessions.of(1, START + 2, 0.0f, LENGTH).orElseThrow().targetEntityId());
    assertTrue(sessions.targets(dog));
    assertEquals(false, sessions.targets(dog + 1));
  }

  @ParameterizedTest(name = "pat with no invite seen, {0} ticks in")
  @CsvSource({"0, 0.6", "10, 1.1"})
  @DisplayName("a player who only sees the pat starts it already knelt")
  void aPatWithoutAnInviteStartsKnelt(final long ticks, final float clipTime) {
    final PatSessions sessions = new PatSessions();
    sessions.show(2, PlayerGesture.PAT, 9, START);
    assertEquals(
        clipTime,
        sessions.of(2, START + ticks, 0.0f, LENGTH).orElseThrow().moment().clipTime(),
        1.0e-4f);
  }

  @ParameterizedTest(name = "release for dog {0} while patting dog 9 -> rising {1}")
  @CsvSource({"9, true", "3, false"})
  @DisplayName("only a release for the dog being patted ends the pat")
  void onlyTheRightReleaseEndsIt(final int releasedDog, final boolean rising) {
    final PatSessions sessions = new PatSessions();
    sessions.show(4, PlayerGesture.INVITE, 9, START);
    sessions.show(4, PlayerGesture.RELEASE, releasedDog, START + 4);
    final float time = sessions.of(4, START + 10, 0.0f, LENGTH).orElseThrow().moment().clipTime();
    assertEquals(rising, time >= PatTimeline.STROKES_END);
  }

  @ParameterizedTest(name = "{0} ticks after the pat")
  @CsvSource({"40", "100"})
  @DisplayName("a finished pat is forgotten, and a reset forgets them all")
  void finishedPatsAreForgotten(final long ticks) {
    final PatSessions sessions = new PatSessions();
    sessions.show(5, PlayerGesture.PAT, 9, START);
    sessions.show(6, PlayerGesture.INVITE, 8, START);
    assertTrue(sessions.of(5, START + ticks, 0.0f, LENGTH).isEmpty());
    sessions.clear();
    assertTrue(sessions.of(6, START + 1, 0.0f, LENGTH).isEmpty());
  }

  @ParameterizedTest(name = "dog lost {0} ticks into the pat")
  @CsvSource({"5", "20"})
  @DisplayName("a dog that vanishes mid pat leaves the player rising toward where it was")
  void aLostDogRisesTowardItsLastSpot(final long ticks) {
    final PatSessions sessions = new PatSessions();
    final PatTarget.Aim aim =
        new PatTarget.Aim(new Vector3f(0, 12, -8), new Vector3f(-3, 14, -7), 1.0f, 1.0f);
    sessions.show(7, PlayerGesture.PAT, 9, START);
    sessions.remember(7, aim);
    sessions.lose(7, START + ticks);
    final PatSessions.Active active = sessions.of(7, START + ticks + 1, 0.0f, LENGTH).orElseThrow();
    assertEquals(aim, active.lastAim().orElseThrow());
    assertTrue(active.moment().clipTime() >= PatTimeline.STROKES_END, "the player gets up");
  }

  @ParameterizedTest(name = "{0} ticks later")
  @CsvSource({"201", "5000"})
  @DisplayName("sessions for players no longer drawn are dropped when the next gesture arrives")
  void staleSessionsAreDropped(final long later) {
    final PatSessions sessions = new PatSessions();
    sessions.show(8, PlayerGesture.INVITE, 3, START);
    sessions.show(9, PlayerGesture.INVITE, 4, START + later);
    assertEquals(false, sessions.targets(3));
    assertTrue(sessions.targets(4));
  }
}
