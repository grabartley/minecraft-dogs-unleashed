package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

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
    assertEquals(searchRange, DogNavigationRules.searchRange(distance, followRange, false));
  }

  @ParameterizedTest
  @CsvSource({"0, 16", "5, 16", "128, 16", "40, 200"})
  void aDogWhoseWideSearchJustMissedSearchesOnlyAsFarAsVanilla(
      final double distance, final double followRange) {
    assertEquals(followRange, DogNavigationRules.searchRange(distance, followRange, true));
  }

  @ParameterizedTest
  @CsvSource({"48, 16, 3", "53, 16, 3.3125", "176, 16, 11", "16, 16, 1", "8, 16, 1"})
  void theNodeBudgetGrowsInStepWithTheSearchAndNeverShrinksBelowVanilla(
      final double searchRange, final double nodeBudgetRange, final float scale) {
    assertEquals(scale, DogNavigationRules.nodeBudgetScale(searchRange, nodeBudgetRange));
  }

  @ParameterizedTest
  @CsvSource({
    "100, 100, true",
    "100, 119, true",
    "100, 120, false",
    "100, 500, false",
    "-9223372036854775808, 0, false"
  })
  void aMissedWideSearchHoldsBackTheNextOneForASecond(
      final long missedAt, final long now, final boolean recentlyMissed) {
    assertEquals(recentlyMissed, DogNavigationRules.recentlyMissed(missedAt, now));
  }

  static Stream<Arguments> targetSets() {
    return Stream.of(
        Arguments.of(Set.of(), 0.0),
        Arguments.of(Set.of(new BlockPos(3, 0, 4)), 5.0),
        Arguments.of(Set.of(new BlockPos(30, 0, 40), new BlockPos(0, 0, 2)), 2.0),
        Arguments.of(Set.of(new BlockPos(0, 0, 0)), 0.0));
  }

  @ParameterizedTest
  @MethodSource("targetSets")
  void theSearchIsSizedByTheNearestTarget(final Set<BlockPos> targets, final double nearest) {
    assertEquals(nearest, DogNavigationRules.nearestDistance(BlockPos.ORIGIN, targets));
  }
}
