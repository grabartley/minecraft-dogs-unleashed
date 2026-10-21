package com.grahambartley.dogsunleashed.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import net.minecraft.entity.SpawnReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BorderCollieCoatRollsTest {

  static Stream<Arguments> naturalRollBoundaries() {
    return Stream.of(
        Arguments.of(0, BorderCollieCoat.BLACK_WHITE),
        Arguments.of(BorderCollieCoatRolls.BLACK_WHITE_THRESHOLD - 1, BorderCollieCoat.BLACK_WHITE),
        Arguments.of(BorderCollieCoatRolls.BLACK_WHITE_THRESHOLD, BorderCollieCoat.RED_MERLE),
        Arguments.of(BorderCollieCoatRolls.RED_MERLE_THRESHOLD, BorderCollieCoat.RED_MERLE),
        Arguments.of(BorderCollieCoatRolls.ROLL_BOUND - 1, BorderCollieCoat.RED_MERLE));
  }

  @ParameterizedTest(name = "roll {0} -> {1}")
  @MethodSource("naturalRollBoundaries")
  @DisplayName("natural spawns map every roll boundary to the expected border collie coat")
  void naturalRollMapsToExpectedCoat(final int roll, final BorderCollieCoat expected) {
    assertEquals(expected, BorderCollieCoatRolls.resolveCoatFromRoll(SpawnReason.NATURAL, roll));
  }

  static Stream<Arguments> breedingRollBoundaries() {
    return Stream.of(
        Arguments.of(0, BorderCollieCoat.BLACK_WHITE),
        Arguments.of(BorderCollieCoatRolls.BLACK_WHITE_THRESHOLD - 1, BorderCollieCoat.BLACK_WHITE),
        Arguments.of(BorderCollieCoatRolls.BLACK_WHITE_THRESHOLD, BorderCollieCoat.RED_MERLE),
        Arguments.of(BorderCollieCoatRolls.RED_MERLE_THRESHOLD - 1, BorderCollieCoat.RED_MERLE),
        Arguments.of(BorderCollieCoatRolls.RED_MERLE_THRESHOLD, BorderCollieCoat.BLUE_MERLE),
        Arguments.of(BorderCollieCoatRolls.ROLL_BOUND - 1, BorderCollieCoat.BLUE_MERLE));
  }

  @ParameterizedTest(name = "roll {0} -> {1}")
  @MethodSource("breedingRollBoundaries")
  @DisplayName("breeding spawns map every roll boundary to the expected border collie coat")
  void breedingRollMapsToExpectedCoat(final int roll, final BorderCollieCoat expected) {
    assertEquals(expected, BorderCollieCoatRolls.resolveCoatFromRoll(SpawnReason.BREEDING, roll));
  }
}
