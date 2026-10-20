package com.grahambartley.dogsunleashed.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PetAgeTest {

  @ParameterizedTest(name = "baby {0}, senior {1} -> {2}")
  @CsvSource({
    "true, false, PUPPY",
    "true, true, PUPPY",
    "false, false, ADULT",
    "false, true, SENIOR"
  })
  @DisplayName("a pet's age comes from its baby and senior flags, with babies never senior")
  void of(final boolean baby, final boolean senior, final PetAge expected) {
    assertEquals(expected, PetAge.of(baby, senior));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "PUPPY, screen.dogs-unleashed.pet_details.age_baby",
    "ADULT, screen.dogs-unleashed.pet_details.age_adult",
    "SENIOR, screen.dogs-unleashed.pet_details.age_senior"
  })
  @DisplayName("each age names its label")
  void translationKey(final PetAge age, final String key) {
    assertEquals(key, age.translationKey());
  }
}
