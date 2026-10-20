package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DogBondStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CommandFollowOwnerGoalTest {

  @ParameterizedTest(name = "closer with bond {0}, {1}, {2} blocks away -> {3}")
  @CsvSource({
    "true, STRANGER, 9.9, false",
    "true, STRANGER, 10.0, true",
    "true, FRIEND, 8.0, true",
    "true, FRIEND, 7.9, false",
    "true, DEVOTED, 6.0, true",
    "true, DEVOTED, 5.9, false",
    "false, DEVOTED, 0.5, true"
  })
  @DisplayName("a closer dog starts following sooner, and heel distance ignores bond")
  void farEnough(
      final boolean closerWithBond,
      final DogBondStep step,
      final double distance,
      final boolean expected) {
    assertEquals(
        expected,
        CommandFollowOwnerGoal.farEnough(closerWithBond, step, 10.0f, distance * distance));
  }
}
