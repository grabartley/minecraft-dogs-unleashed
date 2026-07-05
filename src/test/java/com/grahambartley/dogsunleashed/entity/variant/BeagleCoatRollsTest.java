package com.grahambartley.dogsunleashed.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import net.minecraft.entity.SpawnReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BeagleCoatRollsTest {

  static Stream<Arguments> naturalRollBoundaries() {
    return Stream.of(
        Arguments.of(0, BeagleCoat.TRI_1),
        Arguments.of(BeagleCoatRolls.NATURAL_TRI_1_THRESHOLD - 1, BeagleCoat.TRI_1),
        Arguments.of(BeagleCoatRolls.NATURAL_TRI_1_THRESHOLD, BeagleCoat.RED_1),
        Arguments.of(BeagleCoatRolls.NATURAL_RED_1_THRESHOLD - 1, BeagleCoat.RED_1),
        Arguments.of(BeagleCoatRolls.NATURAL_RED_1_THRESHOLD, BeagleCoat.TRI_2),
        Arguments.of(BeagleCoatRolls.NATURAL_TRI_2_THRESHOLD - 1, BeagleCoat.TRI_2),
        Arguments.of(BeagleCoatRolls.NATURAL_TRI_2_THRESHOLD, BeagleCoat.TRI_3),
        Arguments.of(BeagleCoatRolls.NATURAL_TRI_3_THRESHOLD - 1, BeagleCoat.TRI_3),
        Arguments.of(BeagleCoatRolls.NATURAL_TRI_3_THRESHOLD, BeagleCoat.RED_2),
        Arguments.of(BeagleCoatRolls.NATURAL_RED_2_THRESHOLD - 1, BeagleCoat.RED_2),
        Arguments.of(BeagleCoatRolls.NATURAL_RED_2_THRESHOLD, BeagleCoat.LEMON_1),
        Arguments.of(BeagleCoatRolls.NATURAL_LEMON_1_THRESHOLD - 1, BeagleCoat.LEMON_1),
        Arguments.of(BeagleCoatRolls.NATURAL_LEMON_1_THRESHOLD, BeagleCoat.BROWN_WHITE),
        Arguments.of(BeagleCoatRolls.NATURAL_BROWN_WHITE_THRESHOLD - 1, BeagleCoat.BROWN_WHITE),
        Arguments.of(BeagleCoatRolls.NATURAL_BROWN_WHITE_THRESHOLD, BeagleCoat.BROWN_WHITE_TAN),
        Arguments.of(
            BeagleCoatRolls.NATURAL_BROWN_WHITE_TAN_THRESHOLD - 1, BeagleCoat.BROWN_WHITE_TAN),
        Arguments.of(BeagleCoatRolls.NATURAL_BROWN_WHITE_TAN_THRESHOLD, BeagleCoat.LEMON_2),
        Arguments.of(BeagleCoatRolls.ROLL_BOUND - 1, BeagleCoat.LEMON_2));
  }

  @ParameterizedTest(name = "roll {0} -> {1}")
  @MethodSource("naturalRollBoundaries")
  @DisplayName("natural spawns map every roll boundary to the expected beagle coat")
  void naturalRollMapsToExpectedCoat(final int roll, final BeagleCoat expected) {
    assertEquals(expected, BeagleCoatRolls.resolveCoatFromRoll(SpawnReason.NATURAL, roll));
  }

  static Stream<Arguments> breedingRollBoundaries() {
    return Stream.of(
        Arguments.of(0, BeagleCoat.TRI_1),
        Arguments.of(BeagleCoatRolls.BREEDING_TRI_1_THRESHOLD, BeagleCoat.RED_1),
        Arguments.of(BeagleCoatRolls.BREEDING_RED_1_THRESHOLD, BeagleCoat.TRI_2),
        Arguments.of(BeagleCoatRolls.BREEDING_TRI_2_THRESHOLD, BeagleCoat.BLACK_WHITE),
        Arguments.of(BeagleCoatRolls.BREEDING_BLACK_WHITE_THRESHOLD, BeagleCoat.TRI_3),
        Arguments.of(BeagleCoatRolls.BREEDING_TRI_3_THRESHOLD, BeagleCoat.RED_2),
        Arguments.of(BeagleCoatRolls.BREEDING_RED_2_THRESHOLD, BeagleCoat.LEMON_1),
        Arguments.of(BeagleCoatRolls.BREEDING_LEMON_1_THRESHOLD, BeagleCoat.LILAC_1),
        Arguments.of(BeagleCoatRolls.BREEDING_LILAC_1_THRESHOLD, BeagleCoat.LEMON_2),
        Arguments.of(BeagleCoatRolls.BREEDING_LEMON_2_THRESHOLD, BeagleCoat.BROWN_WHITE),
        Arguments.of(BeagleCoatRolls.BREEDING_BROWN_WHITE_THRESHOLD, BeagleCoat.BROWN_WHITE_TAN),
        Arguments.of(BeagleCoatRolls.BREEDING_BROWN_WHITE_TAN_THRESHOLD, BeagleCoat.LILAC_2),
        Arguments.of(BeagleCoatRolls.ROLL_BOUND - 1, BeagleCoat.LILAC_2));
  }

  @ParameterizedTest(name = "roll {0} -> {1}")
  @MethodSource("breedingRollBoundaries")
  @DisplayName("breeding spawns map every roll boundary to the expected beagle coat")
  void breedingRollMapsToExpectedCoat(final int roll, final BeagleCoat expected) {
    assertEquals(expected, BeagleCoatRolls.resolveCoatFromRoll(SpawnReason.BREEDING, roll));
  }
}
