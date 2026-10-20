package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DogBondStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BondFollowOwnerGoalTest {

  @ParameterizedTest(name = "{0}, {1} blocks away -> {2}")
  @CsvSource({
    "STRANGER, 9.9, false",
    "STRANGER, 10.0, true",
    "FRIEND, 8.0, true",
    "FRIEND, 7.9, false",
    "DEVOTED, 6.0, true",
    "DEVOTED, 5.9, false"
  })
  @DisplayName("a closer dog starts following its owner from nearer")
  void farEnough(final DogBondStep step, final double distance, final boolean expected) {
    assertEquals(expected, BondFollowOwnerGoal.farEnough(step, 10.0f, distance * distance));
  }
}
