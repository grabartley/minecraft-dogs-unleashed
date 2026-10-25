package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogNavigationRulesTest {

  @ParameterizedTest
  @CsvSource({
    "0, 16, 48",
    "5, 16, 53",
    "32, 16, 80",
    "128, 16, 176",
    "300, 16, 176",
    "0, 64, 64",
    "40, 200, 200"
  })
  void theSearchCoversTheDistancePlusRoomForADetourButNeverLessThanTheFollowRange(
      final double distance, final double followRange, final double searchRange) {
    assertEquals(searchRange, DogNavigationRules.searchRange(distance, followRange));
  }

  @ParameterizedTest
  @CsvSource({"48, 16, 3", "53, 16, 3.3125", "176, 16, 11", "16, 16, 1", "8, 16, 1"})
  void theNodeBudgetGrowsInStepWithTheSearchAndNeverShrinksBelowVanilla(
      final double searchRange, final double nodeBudgetRange, final float scale) {
    assertEquals(scale, DogNavigationRules.nodeBudgetScale(searchRange, nodeBudgetRange));
  }

  @ParameterizedTest
  @CsvSource({"100, 100, false", "100, 119, false", "100, 120, true", "100, 500, true"})
  void anUnreachableTargetIsOnlyRetriedOnceASecond(
      final long missedAt, final long now, final boolean retry) {
    assertEquals(retry, DogNavigationRules.mayRetryUnreachable(missedAt, now));
  }
}
