package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RaidAlertRulesTest {

  @ParameterizedTest
  @CsvSource({
    "true, false, FOLLOW, true",
    "true, false, HEEL, true",
    "true, false, HUNT, true",
    "true, false, GUARD, true",
    "true, false, FREE_ROAM, true",
    "true, false, SIT, false",
    "true, false, STAY, false",
    "true, true, FOLLOW, false",
    "false, false, FOLLOW, false"
  })
  void onlyAnAwakeOwnedDogNotToldToSitOrStayWatchesForIntruders(
      final boolean tamed, final boolean asleep, final DogCommand command, final boolean listens) {
    assertEquals(listens, RaidAlertRules.listens(tamed, asleep, command));
  }

  @ParameterizedTest
  @CsvSource({
    "true, false, false, true",
    "false, true, false, true",
    "false, false, true, true",
    "false, false, false, false"
  })
  void illagersRaidersInARaidAndPatrolsAreIntruders(
      final boolean illager,
      final boolean inRaid,
      final boolean patrolling,
      final boolean intruder) {
    assertEquals(intruder, RaidAlertRules.isIntruder(illager, inRaid, patrolling));
  }

  @ParameterizedTest
  @CsvSource({"0, true", "100, true", "2304, true", "2305, false", "10000, false"})
  void dogsHearIntrudersUpToFortyEightBlocksAway(
      final double squaredDistance, final boolean inRange) {
    assertEquals(inRange, RaidAlertRules.inRange(squaredDistance));
  }
}
