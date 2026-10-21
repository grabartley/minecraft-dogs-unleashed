package com.grahambartley.dogsunleashed.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import net.minecraft.entity.SpawnReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GermanShepherdCoatRollsTest {

  static Stream<Arguments> naturalRollBoundaries() {
    return Stream.of(
        Arguments.of(0, GermanShepherdCoat.SADDLE),
        Arguments.of(GermanShepherdCoatRolls.SADDLE_THRESHOLD - 1, GermanShepherdCoat.SADDLE),
        Arguments.of(GermanShepherdCoatRolls.SADDLE_THRESHOLD, GermanShepherdCoat.SABLE),
        Arguments.of(GermanShepherdCoatRolls.SABLE_THRESHOLD, GermanShepherdCoat.SABLE),
        Arguments.of(GermanShepherdCoatRolls.ROLL_BOUND - 1, GermanShepherdCoat.SABLE));
  }

  @ParameterizedTest(name = "roll {0} -> {1}")
  @MethodSource("naturalRollBoundaries")
  @DisplayName("natural spawns map every roll boundary to the expected german shepherd coat")
  void naturalRollMapsToExpectedCoat(final int roll, final GermanShepherdCoat expected) {
    assertEquals(expected, GermanShepherdCoatRolls.resolveCoatFromRoll(SpawnReason.NATURAL, roll));
  }

  static Stream<Arguments> breedingRollBoundaries() {
    return Stream.of(
        Arguments.of(0, GermanShepherdCoat.SADDLE),
        Arguments.of(GermanShepherdCoatRolls.SADDLE_THRESHOLD - 1, GermanShepherdCoat.SADDLE),
        Arguments.of(GermanShepherdCoatRolls.SADDLE_THRESHOLD, GermanShepherdCoat.SABLE),
        Arguments.of(GermanShepherdCoatRolls.SABLE_THRESHOLD - 1, GermanShepherdCoat.SABLE),
        Arguments.of(GermanShepherdCoatRolls.SABLE_THRESHOLD, GermanShepherdCoat.BLACK),
        Arguments.of(GermanShepherdCoatRolls.ROLL_BOUND - 1, GermanShepherdCoat.BLACK));
  }

  @ParameterizedTest(name = "roll {0} -> {1}")
  @MethodSource("breedingRollBoundaries")
  @DisplayName("breeding spawns map every roll boundary to the expected german shepherd coat")
  void breedingRollMapsToExpectedCoat(final int roll, final GermanShepherdCoat expected) {
    assertEquals(expected, GermanShepherdCoatRolls.resolveCoatFromRoll(SpawnReason.BREEDING, roll));
  }
}
