package com.grahambartley.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import net.minecraft.entity.SpawnReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HuskyCoatRollsTest {

  static Stream<Arguments> naturalRollBoundaries() {
    return Stream.of(
        Arguments.of(0, HuskyCoat.BLACK_WHITE),
        Arguments.of(HuskyCoatRolls.NATURAL_BLACK_WHITE_THRESHOLD - 1, HuskyCoat.BLACK_WHITE),
        Arguments.of(HuskyCoatRolls.NATURAL_BLACK_WHITE_THRESHOLD, HuskyCoat.GREY_WHITE),
        Arguments.of(HuskyCoatRolls.NATURAL_GREY_WHITE_THRESHOLD - 1, HuskyCoat.GREY_WHITE),
        Arguments.of(HuskyCoatRolls.NATURAL_GREY_WHITE_THRESHOLD, HuskyCoat.RED_WHITE),
        Arguments.of(HuskyCoatRolls.NATURAL_RED_WHITE_THRESHOLD - 1, HuskyCoat.RED_WHITE),
        Arguments.of(HuskyCoatRolls.NATURAL_RED_WHITE_THRESHOLD, HuskyCoat.SABLE),
        Arguments.of(HuskyCoatRolls.ROLL_BOUND - 1, HuskyCoat.SABLE));
  }

  @ParameterizedTest(name = "roll {0} -> {1}")
  @MethodSource("naturalRollBoundaries")
  @DisplayName("natural spawns map every roll boundary to the expected husky coat")
  void naturalRollMapsToExpectedCoat(final int roll, final HuskyCoat expected) {
    assertEquals(expected, HuskyCoatRolls.resolveCoatFromRoll(SpawnReason.NATURAL, roll));
  }

  static Stream<Arguments> breedingRollBoundaries() {
    return Stream.of(
        Arguments.of(0, HuskyCoat.BLACK_WHITE),
        Arguments.of(HuskyCoatRolls.NATURAL_BLACK_WHITE_THRESHOLD, HuskyCoat.GREY_WHITE),
        Arguments.of(HuskyCoatRolls.NATURAL_GREY_WHITE_THRESHOLD, HuskyCoat.RED_WHITE),
        Arguments.of(HuskyCoatRolls.NATURAL_RED_WHITE_THRESHOLD, HuskyCoat.SABLE),
        Arguments.of(HuskyCoatRolls.BREEDING_SABLE_THRESHOLD, HuskyCoat.AGOUTI),
        Arguments.of(HuskyCoatRolls.BREEDING_AGOUTI_THRESHOLD, HuskyCoat.WHITE),
        Arguments.of(HuskyCoatRolls.ROLL_BOUND - 1, HuskyCoat.WHITE));
  }

  @ParameterizedTest(name = "roll {0} -> {1}")
  @MethodSource("breedingRollBoundaries")
  @DisplayName("breeding spawns map every roll boundary to the expected husky coat")
  void breedingRollMapsToExpectedCoat(final int roll, final HuskyCoat expected) {
    assertEquals(expected, HuskyCoatRolls.resolveCoatFromRoll(SpawnReason.BREEDING, roll));
  }
}
