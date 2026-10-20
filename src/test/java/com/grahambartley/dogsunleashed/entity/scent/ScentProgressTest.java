package com.grahambartley.dogsunleashed.entity.scent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScentProgressTest {

  @Test
  @DisplayName("the dog gives up after two minutes of ticks without getting closer")
  void stallsAfterTwoMinutes() {
    assertEquals(2400, ScentProgress.GIVE_UP_TICKS);
    final ScentProgress progress = new ScentProgress();
    progress.record(20.0);
    for (int tick = 1; tick < 2400; tick++) {
      progress.record(20.0);
      assertFalse(progress.hasStalled(), "stalled early at tick " + tick);
    }
    progress.record(20.0);
    assertTrue(progress.hasStalled());
  }

  @Test
  @DisplayName("getting a real step closer starts the two minutes again")
  void gainingGroundResetsTheClock() {
    final ScentProgress progress = new ScentProgress();
    progress.record(20.0);
    for (int tick = 0; tick < 2399; tick++) {
      progress.record(20.0);
    }
    progress.record(19.0);
    for (int tick = 0; tick < 2399; tick++) {
      progress.record(19.0);
    }
    assertFalse(progress.hasStalled());
  }

  @Test
  @DisplayName("shuffling back and forth on the spot is not progress")
  void jitterIsNotProgress() {
    final ScentProgress progress = new ScentProgress();
    progress.record(20.0);
    for (int tick = 0; tick < 2400; tick++) {
      progress.record(tick % 2 == 0 ? 19.8 : 20.2);
    }
    assertTrue(progress.hasStalled());
  }

  @Test
  @DisplayName("a reset forgets the stall")
  void resetClearsTheStall() {
    final ScentProgress progress = new ScentProgress();
    for (int tick = 0; tick <= 2400; tick++) {
      progress.record(5.0);
    }
    progress.reset();
    progress.record(30.0);
    assertFalse(progress.hasStalled());
  }
}
