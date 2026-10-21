package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogErrandRulesTest {

  @ParameterizedTest(name = "{0}, sitting {1}, carrying {2} -> {3}")
  @CsvSource({
    "FOLLOW, false, false, true",
    "FREE_ROAM, false, false, true",
    "FOLLOW, true, false, false",
    "FREE_ROAM, false, true, false",
    "HEEL, false, false, false",
    "STAY, false, false, false",
    "SIT, false, false, false",
    "GUARD, false, false, false",
    "HUNT, false, false, false"
  })
  @DisplayName("a dog runs errands of its own only when no command holds it")
  void isFree(
      final DogCommand command,
      final boolean sitting,
      final boolean carrying,
      final boolean expected) {
    assertEquals(expected, DogErrandRules.isFree(command, sitting, carrying));
  }

  @ParameterizedTest(name = "{0} squared -> {1}")
  @CsvSource({"0.0, true", "255.9, true", "256.0, true", "256.1, false"})
  @DisplayName("errands stay within sixteen blocks of the owner")
  void ownerNearby(final double squaredDistance, final boolean expected) {
    assertEquals(expected, DogErrandRules.ownerNearby(squaredDistance));
  }
}
