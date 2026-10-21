package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RaidDutyRulesTest {

  @ParameterizedTest
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  void onlyAnOwnedDogFightingAnActiveRaidCounts(
      final boolean tamed, final boolean opponentInActiveRaid, final boolean counts) {
    assertEquals(counts, RaidDutyRules.counts(tamed, opponentInActiveRaid));
  }

  @ParameterizedTest
  @CsvSource({
    "false, false, PENDING",
    "true, false, REWARD",
    "true, true, REWARD",
    "false, true, FORGET"
  })
  void aWonRaidRewardsAndAnyOtherEndForgets(
      final boolean won, final boolean ended, final RaidDutyRules.Outcome outcome) {
    assertEquals(outcome, RaidDutyRules.outcome(won, ended));
  }

  @ParameterizedTest
  @CsvSource({"true, true", "false, false"})
  void theRosetteIsWornOnlyWhenTheCosmeticSlotIsFree(final boolean slotEmpty, final boolean wears) {
    assertEquals(wears, RaidDutyRules.wearsRosette(slotEmpty));
  }
}
