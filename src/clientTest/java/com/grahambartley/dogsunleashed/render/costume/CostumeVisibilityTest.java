package com.grahambartley.dogsunleashed.render.costume;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CostumeVisibilityTest {

  @ParameterizedTest(name = "costume {0}, armour {1}, invisible {2} -> shows {3}")
  @CsvSource({
    "true, false, false, true",
    "true, true, false, false",
    "true, false, true, false",
    "true, true, true, false",
    "false, false, false, false"
  })
  @DisplayName("a worn costume shows unless armour covers it or the dog is invisible")
  void showsOnlyWhenUncovered(
      final boolean wearsCostume,
      final boolean armoured,
      final boolean invisible,
      final boolean expected) {
    assertEquals(expected, CostumeVisibility.shows(wearsCostume, armoured, invisible));
  }
}
