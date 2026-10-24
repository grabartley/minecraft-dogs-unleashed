package com.grahambartley.dogsunleashed.entity.trick;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrickReadinessTest {

  @ParameterizedTest(name = "{0}: interrupts {1}, may start {2}")
  @CsvSource({
    "free, false, true",
    "asleep, true, false",
    "hitched, true, false",
    "mounted, true, false",
    "fighting, true, false",
    "hurt, true, false",
    "inWater, true, false",
    "onErrand, false, false",
    "performing, false, false"
  })
  @DisplayName("each busy state blocks a new trick, and only the urgent ones stop a running one")
  void readiness(final String state, final boolean interrupts, final boolean mayStart) {
    final TrickReadiness readiness = with(state);
    assertEquals(interrupts, readiness.interruptsTrick());
    assertEquals(mayStart, readiness.mayStart());
  }

  static TrickReadiness with(final String state) {
    return new TrickReadiness(
        state.equals("asleep"),
        state.equals("hitched"),
        state.equals("mounted"),
        state.equals("fighting"),
        state.equals("hurt"),
        state.equals("inWater"),
        state.equals("onErrand"),
        state.equals("performing"));
  }
}
