package com.grahambartley.dogsunleashed.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import net.minecraft.entity.SpawnReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DachshundCoatRollsTest {

  static Stream<Arguments> naturalRollBoundaries() {
    return Stream.of(
        Arguments.of(0, DachshundCoat.BLACK_TAN),
        Arguments.of(DachshundCoatRolls.NATURAL_BLACK_TAN_THRESHOLD - 1, DachshundCoat.BLACK_TAN),
        Arguments.of(DachshundCoatRolls.NATURAL_BLACK_TAN_THRESHOLD, DachshundCoat.RED),
        Arguments.of(DachshundCoatRolls.NATURAL_RED_THRESHOLD - 1, DachshundCoat.RED),
        Arguments.of(DachshundCoatRolls.NATURAL_RED_THRESHOLD, DachshundCoat.CHOCOLATE_TAN),
        Arguments.of(
            DachshundCoatRolls.NATURAL_CHOCOLATE_TAN_THRESHOLD - 1, DachshundCoat.CHOCOLATE_TAN),
        Arguments.of(
            DachshundCoatRolls.NATURAL_CHOCOLATE_TAN_THRESHOLD, DachshundCoat.CHOCOLATE_CREAM),
        Arguments.of(
            DachshundCoatRolls.NATURAL_CHOCOLATE_CREAM_THRESHOLD - 1,
            DachshundCoat.CHOCOLATE_CREAM),
        Arguments.of(
            DachshundCoatRolls.NATURAL_CHOCOLATE_CREAM_THRESHOLD, DachshundCoat.BLACK_CREAM),
        Arguments.of(
            DachshundCoatRolls.NATURAL_BLACK_CREAM_THRESHOLD - 1, DachshundCoat.BLACK_CREAM),
        Arguments.of(
            DachshundCoatRolls.NATURAL_BLACK_CREAM_THRESHOLD, DachshundCoat.LIGHT_BLACK_CREAM),
        Arguments.of(
            DachshundCoatRolls.NATURAL_LIGHT_BLACK_CREAM_THRESHOLD - 1,
            DachshundCoat.LIGHT_BLACK_CREAM),
        Arguments.of(
            DachshundCoatRolls.NATURAL_LIGHT_BLACK_CREAM_THRESHOLD, DachshundCoat.RED_PIEBALD),
        Arguments.of(DachshundCoatRolls.ROLL_BOUND - 1, DachshundCoat.RED_PIEBALD));
  }

  @ParameterizedTest(name = "roll {0} -> {1}")
  @MethodSource("naturalRollBoundaries")
  @DisplayName("natural spawns map every roll boundary to the expected dachshund coat")
  void naturalRollMapsToExpectedCoat(final int roll, final DachshundCoat expected) {
    assertEquals(expected, DachshundCoatRolls.resolveCoatFromRoll(SpawnReason.NATURAL, roll));
  }

  static Stream<Arguments> breedingRollBoundaries() {
    return Stream.of(
        Arguments.of(0, DachshundCoat.BLACK_TAN),
        Arguments.of(DachshundCoatRolls.BREEDING_BLACK_TAN_THRESHOLD, DachshundCoat.RED),
        Arguments.of(DachshundCoatRolls.BREEDING_RED_THRESHOLD, DachshundCoat.CHOCOLATE_TAN),
        Arguments.of(
            DachshundCoatRolls.BREEDING_CHOCOLATE_TAN_THRESHOLD, DachshundCoat.CHOCOLATE_CREAM),
        Arguments.of(
            DachshundCoatRolls.BREEDING_CHOCOLATE_CREAM_THRESHOLD, DachshundCoat.BLACK_CREAM),
        Arguments.of(
            DachshundCoatRolls.BREEDING_BLACK_CREAM_THRESHOLD, DachshundCoat.LIGHT_BLACK_CREAM),
        Arguments.of(
            DachshundCoatRolls.BREEDING_LIGHT_BLACK_CREAM_THRESHOLD, DachshundCoat.RED_PIEBALD),
        Arguments.of(
            DachshundCoatRolls.BREEDING_RED_PIEBALD_THRESHOLD, DachshundCoat.BLACK_TAN_PIEBALD),
        Arguments.of(
            DachshundCoatRolls.BREEDING_BLACK_TAN_PIEBALD_THRESHOLD, DachshundCoat.BLUE_TAN),
        Arguments.of(DachshundCoatRolls.BREEDING_BLUE_TAN_THRESHOLD, DachshundCoat.ALBINO),
        Arguments.of(DachshundCoatRolls.ROLL_BOUND - 1, DachshundCoat.ALBINO));
  }

  @ParameterizedTest(name = "roll {0} -> {1}")
  @MethodSource("breedingRollBoundaries")
  @DisplayName("breeding spawns map every roll boundary to the expected dachshund coat")
  void breedingRollMapsToExpectedCoat(final int roll, final DachshundCoat expected) {
    assertEquals(expected, DachshundCoatRolls.resolveCoatFromRoll(SpawnReason.BREEDING, roll));
  }
}
