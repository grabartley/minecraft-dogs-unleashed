package com.grahambartley.dogsunleashed.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import net.minecraft.entity.SpawnReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DireWolfCoatRollsTest {

  static Stream<Arguments> rollShares() {
    return Stream.of(
        Arguments.of(SpawnReason.NATURAL, DireWolfCoat.SMOKEGREY, 30),
        Arguments.of(SpawnReason.NATURAL, DireWolfCoat.ASHGREY, 27),
        Arguments.of(SpawnReason.NATURAL, DireWolfCoat.FROSTGREY, 20),
        Arguments.of(SpawnReason.NATURAL, DireWolfCoat.SILVER, 14),
        Arguments.of(SpawnReason.NATURAL, DireWolfCoat.BLACK, 7),
        Arguments.of(SpawnReason.NATURAL, DireWolfCoat.WHITE, 2),
        Arguments.of(SpawnReason.BREEDING, DireWolfCoat.SMOKEGREY, 26),
        Arguments.of(SpawnReason.BREEDING, DireWolfCoat.ASHGREY, 24),
        Arguments.of(SpawnReason.BREEDING, DireWolfCoat.FROSTGREY, 22),
        Arguments.of(SpawnReason.BREEDING, DireWolfCoat.SILVER, 17),
        Arguments.of(SpawnReason.BREEDING, DireWolfCoat.BLACK, 9),
        Arguments.of(SpawnReason.BREEDING, DireWolfCoat.WHITE, 2));
  }

  @ParameterizedTest(name = "{0} {1} = {2} of 100")
  @MethodSource("rollShares")
  @DisplayName("each coat takes its documented share of the 100 rolls")
  void coatTakesItsDocumentedShare(
      final SpawnReason reason, final DireWolfCoat coat, final int expectedRolls) {
    int rolls = 0;
    for (int roll = 0; roll < DogCoats.ROLL_BOUND; roll++) {
      if (DireWolfCoatRolls.resolveCoatFromRoll(reason, roll) == coat) {
        rolls++;
      }
    }
    assertEquals(expectedRolls, rolls);
  }

  static Stream<Arguments> boundaries() {
    return Stream.of(
        Arguments.of(SpawnReason.NATURAL, 0, DireWolfCoat.SMOKEGREY),
        Arguments.of(SpawnReason.NATURAL, 29, DireWolfCoat.SMOKEGREY),
        Arguments.of(SpawnReason.NATURAL, 30, DireWolfCoat.ASHGREY),
        Arguments.of(SpawnReason.NATURAL, 97, DireWolfCoat.BLACK),
        Arguments.of(SpawnReason.NATURAL, 98, DireWolfCoat.WHITE),
        Arguments.of(SpawnReason.NATURAL, 99, DireWolfCoat.WHITE),
        Arguments.of(SpawnReason.BREEDING, 25, DireWolfCoat.SMOKEGREY),
        Arguments.of(SpawnReason.BREEDING, 26, DireWolfCoat.ASHGREY),
        Arguments.of(SpawnReason.BREEDING, 99, DireWolfCoat.WHITE));
  }

  @ParameterizedTest(name = "{0} roll {1} -> {2}")
  @MethodSource("boundaries")
  @DisplayName("rolls either side of each threshold land on the right coat")
  void boundaryRollsLandOnTheRightCoat(
      final SpawnReason reason, final int roll, final DireWolfCoat expected) {
    assertEquals(expected, DireWolfCoatRolls.resolveCoatFromRoll(reason, roll));
  }
}
