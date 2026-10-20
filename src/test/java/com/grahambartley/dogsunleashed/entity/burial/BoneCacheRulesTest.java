package com.grahambartley.dogsunleashed.entity.burial;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BoneCacheRulesTest {

  @Test
  @DisplayName("the dog buries within 16 blocks and digs for three seconds")
  void reachAndDigTime() {
    assertEquals(16, BoneCacheRules.BURY_RADIUS);
    assertEquals(60, BoneCacheRules.DIG_TICKS);
  }

  @ParameterizedTest(name = "buried at {0}, now {1}: due {2}")
  @CsvSource({
    "1000, 1000, false",
    "1000, 25000, false",
    "1000, 72999, false",
    "1000, 73000, true",
    "1000, 500000, true",
    "73000, 1000, true"
  })
  @DisplayName("a cache is due three in-game days after burial, or when time has run backwards")
  void dueAfterThreeDays(final long buriedAt, final long now, final boolean due) {
    assertEquals(due, BoneCacheRules.isDue(buriedAt, now));
  }

  @ParameterizedTest(name = "roll {0} gives a bonus: {1}")
  @CsvSource({"0, true", "1, false", "2, false", "3, false", "4, false"})
  @DisplayName("one roll in five finds a bonus item")
  void oneInFiveBonus(final int roll, final boolean bonus) {
    assertEquals(5, BoneCacheRules.BONUS_ODDS);
    assertEquals(bonus, BoneCacheRules.rollsBonus(roll));
  }
}
