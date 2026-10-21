package com.grahambartley.dogsunleashed.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import net.minecraft.entity.SpawnReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PinscherCoatRollsTest {

  static Stream<Arguments> naturalRollBoundaries() {
    return Stream.of(
        Arguments.of(0, PinscherCoat.BLACK_TAN),
        Arguments.of(PinscherCoatRolls.BLACK_TAN_THRESHOLD - 1, PinscherCoat.BLACK_TAN),
        Arguments.of(PinscherCoatRolls.BLACK_TAN_THRESHOLD, PinscherCoat.RED),
        Arguments.of(PinscherCoatRolls.RED_THRESHOLD, PinscherCoat.RED),
        Arguments.of(PinscherCoatRolls.ROLL_BOUND - 1, PinscherCoat.RED));
  }

  @ParameterizedTest(name = "roll {0} -> {1}")
  @MethodSource("naturalRollBoundaries")
  @DisplayName("natural spawns never roll chocolate and tan")
  void naturalRollMapsToExpectedCoat(final int roll, final PinscherCoat expected) {
    assertEquals(expected, PinscherCoatRolls.resolveCoatFromRoll(SpawnReason.NATURAL, roll));
  }

  static Stream<Arguments> breedingRollBoundaries() {
    return Stream.of(
        Arguments.of(0, PinscherCoat.BLACK_TAN),
        Arguments.of(PinscherCoatRolls.BLACK_TAN_THRESHOLD - 1, PinscherCoat.BLACK_TAN),
        Arguments.of(PinscherCoatRolls.BLACK_TAN_THRESHOLD, PinscherCoat.RED),
        Arguments.of(PinscherCoatRolls.RED_THRESHOLD - 1, PinscherCoat.RED),
        Arguments.of(PinscherCoatRolls.RED_THRESHOLD, PinscherCoat.CHOCOLATE_TAN),
        Arguments.of(PinscherCoatRolls.ROLL_BOUND - 1, PinscherCoat.CHOCOLATE_TAN));
  }

  @ParameterizedTest(name = "roll {0} -> {1}")
  @MethodSource("breedingRollBoundaries")
  @DisplayName("breeding maps every roll boundary to the expected pinscher coat")
  void breedingRollMapsToExpectedCoat(final int roll, final PinscherCoat expected) {
    assertEquals(expected, PinscherCoatRolls.resolveCoatFromRoll(SpawnReason.BREEDING, roll));
  }
}
