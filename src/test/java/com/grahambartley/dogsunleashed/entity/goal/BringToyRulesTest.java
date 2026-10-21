package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BringToyRulesTest {

  @ParameterizedTest(name = "{0}, sitting {1}, carrying {2}, fetching {3} -> {4}")
  @CsvSource({
    "FOLLOW, false, false, false, true",
    "FREE_ROAM, false, false, false, true",
    "FOLLOW, true, false, false, false",
    "FOLLOW, false, true, false, false",
    "FOLLOW, false, false, true, false",
    "HEEL, false, false, false, false",
    "STAY, false, false, false, false",
    "GUARD, false, false, false, false"
  })
  @DisplayName("a playful dog only goes for a toy when it is free")
  void mayLook(
      final DogCommand command,
      final boolean sitting,
      final boolean carrying,
      final boolean fetching,
      final boolean expected) {
    assertEquals(expected, BringToyRules.mayLook(command, sitting, carrying, fetching));
  }

  @ParameterizedTest(name = "{0} squared from owner -> {1}")
  @CsvSource({"4.0, false", "9.0, false", "9.1, true", "60.0, true"})
  @DisplayName("a toy already at the owner's feet is left alone")
  void worthBringing(final double squaredDistance, final boolean expected) {
    assertEquals(expected, BringToyRules.worthBringing(squaredDistance));
  }
}
