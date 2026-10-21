package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BringToyRulesTest {

  @ParameterizedTest(name = "{0}, sitting {1}, carrying {2}, fetching {3}, owner {4} -> {5}")
  @CsvSource({
    "FOLLOW, false, false, false, 25.0, true",
    "FREE_ROAM, false, false, false, 256.0, true",
    "FREE_ROAM, false, false, false, 400.0, false",
    "FOLLOW, true, false, false, 25.0, false",
    "FOLLOW, false, true, false, 25.0, false",
    "FOLLOW, false, false, true, 25.0, false",
    "HEEL, false, false, false, 25.0, false",
    "STAY, false, false, false, 25.0, false",
    "GUARD, false, false, false, 25.0, false"
  })
  @DisplayName("a playful dog only goes for a toy when it is free and near its owner")
  void mayLook(
      final DogCommand command,
      final boolean sitting,
      final boolean carrying,
      final boolean fetching,
      final double ownerSquaredDistance,
      final boolean expected) {
    assertEquals(
        expected,
        BringToyRules.mayLook(command, sitting, carrying, fetching, ownerSquaredDistance));
  }

  @ParameterizedTest(name = "{0} squared from owner -> {1}")
  @CsvSource({"4.0, false", "9.0, false", "9.1, true", "60.0, true"})
  @DisplayName("a toy already at the owner's feet is left alone")
  void worthBringing(final double squaredDistance, final boolean expected) {
    assertEquals(expected, BringToyRules.worthBringing(squaredDistance));
  }
}
