package com.grahambartley.dogsunleashed.entity.expedition;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ExpeditionScheduleTest {

  @ParameterizedTest(name = "sent at {0} -> back at {1}")
  @CsvSource({
    "0, 24000",
    "6000, 24000",
    "13000, 24000",
    "22000, 24000",
    "23500, 24700",
    "48000, 72000",
    "60123, 72000"
  })
  @DisplayName("a dog comes back the next morning, never sooner than a minute after leaving")
  void returnsNextMorning(final long sentAt, final long expected) {
    assertEquals(expected, ExpeditionSchedule.returnTimeOfDay(sentAt));
  }

  @ParameterizedTest(name = "return {0}, left {1}, time of day {2}, game time {3} -> {4}")
  @CsvSource({
    "24000, 100, 23999, 5000, false",
    "24000, 100, 24000, 5000, true",
    "24000, 100, 30000, 5000, true",
    "24000, 100, 1000, 24099, false",
    "24000, 100, 1000, 24100, true"
  })
  @DisplayName(
      "a trip is due at the return time, or after a full day when the clock was turned back")
  void isDue(
      final long returnTime,
      final long departedAt,
      final long timeOfDay,
      final long gameTime,
      final boolean expected) {
    assertEquals(expected, ExpeditionSchedule.isDue(returnTime, departedAt, timeOfDay, gameTime));
  }
}
