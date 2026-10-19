package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class DeathWatchRulesTest {

  @ParameterizedTest
  @CsvSource({
    "true, true, true, false, false, 0, true",
    "true, true, true, false, false, 1024, true",
    "true, true, true, false, false, 1024.01, false",
    "true, true, false, true, false, 100, true",
    "true, true, false, false, false, 100, false",
    "false, true, true, false, false, 100, false",
    "true, false, true, false, false, 100, false",
    "true, false, false, true, false, 100, false",
    "true, true, true, false, true, 100, false"
  })
  void answersCallOnlyForTheOwnersFreeFollowingDogsWithinGatherRange(
      final boolean tamed,
      final boolean ownedByDeceased,
      final boolean followingOwner,
      final boolean alreadyWatching,
      final boolean leashed,
      final double squaredDistanceToSpot,
      final boolean answers) {
    assertEquals(
        answers,
        DeathWatchRules.answersCall(
            new DeathWatchRules.Candidate(
                tamed,
                ownedByDeceased,
                followingOwner,
                alreadyWatching,
                leashed,
                squaredDistanceToSpot)));
  }

  @ParameterizedTest
  @CsvSource({
    "false, 0, false, KEEP_WATCH",
    "false, 0, true, KEEP_WATCH",
    "false, 10000, false, KEEP_WATCH",
    "true, 10000, false, CALL_OUT",
    "true, 10000, true, KEEP_WATCH",
    "true, 64.01, false, CALL_OUT",
    "true, 64, false, STAND_DOWN",
    "true, 0, true, STAND_DOWN"
  })
  void nextCallsOutOnceTheOwnerLivesAgainAndStandsDownWhenTheyComeClose(
      final boolean ownerAliveHere,
      final double ownerSquaredDistanceToSpot,
      final boolean calledOut,
      final DeathWatchRules.Step step) {
    assertEquals(step, DeathWatchRules.next(ownerAliveHere, ownerSquaredDistanceToSpot, calledOut));
  }

  @ParameterizedTest
  @CsvSource({"0, true", "2.25, true", "2.26, false", "100, false"})
  void hasArrivedWithinArrivalDistanceOfTheSpot(
      final double squaredDistanceToSpot, final boolean arrived) {
    assertEquals(arrived, DeathWatchRules.hasArrived(squaredDistanceToSpot));
  }

  @ParameterizedTest
  @EnumSource(DogCommand.class)
  void commandAfterWatchReturnsToTheFollowingCommandTheDogHadOrFollow(final DogCommand before) {
    final DogCommand expected = before == DogCommand.HEEL ? DogCommand.HEEL : DogCommand.FOLLOW;
    assertEquals(expected, DeathWatchRules.commandAfterWatch(before));
  }
}
