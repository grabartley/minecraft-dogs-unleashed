package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DogBondStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BondFollowOwnerGoalTest {

  @ParameterizedTest(name = "{0}, scale {1}, {2} blocks away -> {3}")
  @CsvSource({
    "STRANGER, 1.0, 9.9, false",
    "STRANGER, 1.0, 10.0, true",
    "FRIEND, 1.0, 8.0, true",
    "FRIEND, 1.0, 7.9, false",
    "DEVOTED, 1.0, 6.0, true",
    "DEVOTED, 1.0, 5.9, false",
    "STRANGER, 0.6, 6.0, true",
    "STRANGER, 0.6, 5.9, false",
    "DEVOTED, 0.6, 3.6, true",
    "DEVOTED, 0.6, 3.5, false"
  })
  @DisplayName("a closer or clingier dog starts following its owner from nearer")
  void farEnough(
      final DogBondStep step, final double scale, final double distance, final boolean expected) {
    assertEquals(expected, BondFollowOwnerGoal.farEnough(step, 10.0f, scale, distance * distance));
  }
}
