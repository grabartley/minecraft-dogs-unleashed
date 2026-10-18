package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogTreatBuffStateTest {

  @ParameterizedTest(name = "{0} ticks saved reads back as {1}")
  @CsvSource({"0, 0", "1, 1", "600, 600", "-1, 0", "-600, 0"})
  @DisplayName("a negative treat buff on disk is clamped away")
  void aNegativeTreatBuffOnDiskIsClampedAway(final int saved, final int expected) {
    assertEquals(expected, DogTreatBuffState.sanitizedTicks(saved));
  }
}
