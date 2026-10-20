package com.grahambartley.dogsunleashed.entity.burial;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FoxRaidRulesTest {

  @ParameterizedTest(name = "night {0}, wild {1}, mouth empty {2}, busy {3}: raids {4}")
  @CsvSource({
    "true, true, true, false, true",
    "false, true, true, false, false",
    "true, false, true, false, false",
    "true, true, false, false, false",
    "true, true, true, true, false"
  })
  @DisplayName("only a wild, idle fox with an empty mouth raids, and only at night")
  void raidsOnlyWhenWildIdleAndNight(
      final boolean night,
      final boolean wild,
      final boolean mouthEmpty,
      final boolean busy,
      final boolean raids) {
    assertEquals(raids, FoxRaidRules.mayRaid(night, wild, mouthEmpty, busy));
  }

  @ParameterizedTest(name = "roll {0} starts a raid: {1}")
  @CsvSource({"0, true", "1, false", "2, false", "3, false"})
  @DisplayName("one search in four turns into a raid")
  void oneInFourSearchesRaids(final int roll, final boolean raids) {
    assertEquals(raids, FoxRaidRules.rollsRaid(roll));
  }

  @ParameterizedTest(name = "{0} ticks, {1} blocks from the start: over {2}")
  @CsvSource({
    "0, 0.0, false",
    "119, 11.9, false",
    "120, 0.0, true",
    "10, 12.0, true",
    "500, 40.0, true"
  })
  @DisplayName("a chase ends after six seconds or twelve blocks, whichever comes first")
  void chaseEndsShortly(final int ticks, final double distance, final boolean over) {
    assertEquals(over, FoxRaidRules.chaseIsOver(ticks, distance));
  }
}
