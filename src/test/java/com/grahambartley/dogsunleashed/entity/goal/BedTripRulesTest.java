package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BedTripRulesTest {

  private static final double VANILLA_FOLLOW_RANGE = 16.0;

  @ParameterizedTest
  @CsvSource({"0, true", "1, true", "4, true", "4.01, false", "9, false", "400, false"})
  void theDogLiesDownOnceItIsWithinTwoBlocksOfTheBed(
      final double squaredDistance, final boolean arrived) {
    assertEquals(arrived, BedTripRules.hasArrived(squaredDistance));
  }

  @ParameterizedTest
  @CsvSource({"0, 48", "5, 53", "32, 80", "80, 128", "200, 128", "-3, 48"})
  void theSearchCoversTheStraightLineDistancePlusRoomForADetourUpToACap(
      final double distance, final double range) {
    assertEquals(range, BedTripRules.searchRange(distance));
  }

  @ParameterizedTest
  @CsvSource({"0, 16, 32", "5, 16, 37", "200, 16, 112", "0, 48, 0", "0, 64, 0"})
  void theFollowRangeOnlyGrowsByWhatTheSearchNeedsAboveTheDogsOwnRange(
      final double distance, final double baseRange, final double extra) {
    assertEquals(extra, BedTripRules.extraFollowRange(distance, baseRange));
  }

  @ParameterizedTest
  @CsvSource({"0, 16, 3", "16, 16, 4", "200, 16, 8", "0, 48, 1", "0, 96, 1"})
  void theNodeBudgetGrowsInStepWithTheSearchRangeAndNeverShrinks(
      final double distance, final double baseRange, final float scale) {
    assertEquals(scale, BedTripRules.nodeBudgetScale(distance, baseRange));
  }

  @ParameterizedTest
  @CsvSource({"0", "5", "40", "200"})
  void everyBedTripSearchesFurtherThanVanillaSixteenBlocks(final double distance) {
    assertEquals(
        true,
        BedTripRules.searchRange(distance) > VANILLA_FOLLOW_RANGE,
        "A detour longer than sixteen blocks must stay findable");
  }
}
