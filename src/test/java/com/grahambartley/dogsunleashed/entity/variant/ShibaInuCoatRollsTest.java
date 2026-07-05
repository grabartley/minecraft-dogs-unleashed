package com.grahambartley.dogsunleashed.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import net.minecraft.entity.SpawnReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ShibaInuCoatRollsTest {

  static Stream<Arguments> naturalRollBoundaries() {
    return Stream.of(
        Arguments.of(0, ShibaInuCoat.RED),
        Arguments.of(ShibaInuCoatRolls.RED_THRESHOLD - 1, ShibaInuCoat.RED),
        Arguments.of(ShibaInuCoatRolls.RED_THRESHOLD, ShibaInuCoat.BLACK),
        Arguments.of(ShibaInuCoatRolls.ROLL_BOUND - 1, ShibaInuCoat.BLACK));
  }

  @ParameterizedTest(name = "roll {0} -> {1}")
  @MethodSource("naturalRollBoundaries")
  @DisplayName("natural spawns map every roll boundary to the expected shiba coat")
  void naturalRollMapsToExpectedCoat(final int roll, final ShibaInuCoat expected) {
    assertEquals(expected, ShibaInuCoatRolls.resolveCoatFromRoll(SpawnReason.NATURAL, roll));
  }

  static Stream<Arguments> breedingRollBoundaries() {
    return Stream.of(
        Arguments.of(0, ShibaInuCoat.RED),
        Arguments.of(ShibaInuCoatRolls.RED_THRESHOLD, ShibaInuCoat.BLACK),
        Arguments.of(ShibaInuCoatRolls.BLACK_THRESHOLD, ShibaInuCoat.SESAME),
        Arguments.of(ShibaInuCoatRolls.ROLL_BOUND - 1, ShibaInuCoat.SESAME));
  }

  @ParameterizedTest(name = "roll {0} -> {1}")
  @MethodSource("breedingRollBoundaries")
  @DisplayName("breeding spawns map every roll boundary to the expected shiba coat")
  void breedingRollMapsToExpectedCoat(final int roll, final ShibaInuCoat expected) {
    assertEquals(expected, ShibaInuCoatRolls.resolveCoatFromRoll(SpawnReason.BREEDING, roll));
  }
}
