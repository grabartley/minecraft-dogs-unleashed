package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class DogGrowthRulesTest {

  static Stream<Arguments> packStages() {
    return Stream.of(
        Arguments.of(0, 0, DireWolfGrowthStage.YEARLING),
        Arguments.of(0, 49, DireWolfGrowthStage.YEARLING),
        Arguments.of(0, 50, DireWolfGrowthStage.ADULT),
        Arguments.of(0, 99, DireWolfGrowthStage.ADULT),
        Arguments.of(1, 0, DireWolfGrowthStage.PUP),
        Arguments.of(1, 99, DireWolfGrowthStage.PUP),
        Arguments.of(2, 0, DireWolfGrowthStage.YEARLING),
        Arguments.of(3, 99, DireWolfGrowthStage.ADULT),
        Arguments.of(4, 39, DireWolfGrowthStage.PUP),
        Arguments.of(4, 40, DireWolfGrowthStage.YEARLING),
        Arguments.of(5, 79, DireWolfGrowthStage.YEARLING),
        Arguments.of(9, 80, DireWolfGrowthStage.ADULT),
        Arguments.of(9, 99, DireWolfGrowthStage.ADULT));
  }

  @ParameterizedTest(name = "member {0}, roll {1} -> {2}")
  @MethodSource("packStages")
  @DisplayName("pack members are an adult, a pup, two more adults, then a 40/40/20 mix")
  void packMemberStage(final int member, final int roll, final DireWolfGrowthStage expected) {
    assertEquals(expected, DogGrowthRules.packStage(member, roll));
  }

  @Test
  @DisplayName("no wild pack member is ever above an adult")
  void noWildMemberAboveAdult() {
    for (int member = 0; member < 10; member++) {
      for (int roll = 0; roll < DogGrowthRules.PERCENT; roll++) {
        assertTrue(
            DogGrowthRules.packStage(member, roll).number() <= DireWolfGrowthStage.ADULT.number());
      }
    }
  }

  @Test
  @DisplayName("every smallest pack has at least three adults and a pup, whatever it rolls")
  void smallestPackHasThreeAdultsAndAPup() {
    for (int roll = 0; roll < DogGrowthRules.PERCENT; roll++) {
      int adults = 0;
      int pups = 0;
      for (int member = 0; member < 5; member++) {
        final DireWolfGrowthStage stage = DogGrowthRules.packStage(member, roll);
        if (stage == DireWolfGrowthStage.PUP) {
          pups++;
        } else {
          adults++;
        }
      }
      assertTrue(adults >= 3, "roll " + roll + " gave " + adults + " adults");
      assertTrue(pups >= 1, "roll " + roll + " gave no pup");
    }
  }

  @Test
  @DisplayName("the later-member mix is 40 percent pups, 40 yearlings and 20 adults")
  void laterMemberMixMatchesTheSpec() {
    final int[] counts = new int[6];
    for (int roll = 0; roll < DogGrowthRules.PERCENT; roll++) {
      counts[DogGrowthRules.packStage(4, roll).number()]++;
    }
    assertEquals(40, counts[1]);
    assertEquals(40, counts[2]);
    assertEquals(20, counts[3]);
  }

  @ParameterizedTest(name = "health {0} of {1} -> {3} when max becomes {2}")
  @CsvSource({
    "30, 30, 40, 40",
    "12, 30, 40, 22",
    "1, 50, 60, 11",
    "30, 30, 20, 20",
    "15, 30, 20, 15",
  })
  @DisplayName("growing adds the max health gained rather than healing to full")
  void growingAddsOnlyTheGain(
      final float current, final float previousMax, final float newMax, final float expected) {
    assertEquals(
        expected, DogGrowthRules.healthAfterStageChange(current, previousMax, newMax), 1e-6f);
  }

  @Test
  @DisplayName("a stage advances once its twenty minutes are up")
  void stageAdvancesAfterTwentyMinutes() {
    assertFalse(DogGrowthRules.advancesStage(23999));
    assertTrue(DogGrowthRules.advancesStage(24000));
  }
}
