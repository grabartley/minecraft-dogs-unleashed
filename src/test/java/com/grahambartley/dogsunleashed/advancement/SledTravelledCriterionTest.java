package com.grahambartley.dogsunleashed.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SledTravelledCriterionTest {

  @Test
  @DisplayName("the criterion id is dogs-unleashed:sled_travelled")
  void exposesItsIdentifier() {
    assertEquals("dogs-unleashed:sled_travelled", SledTravelledCriterion.ID.toString());
  }

  @ParameterizedTest(name = "{0} blocks against a minimum of {1} -> {2}")
  @CsvSource({
    "1000.0, 1000.0, true",
    "999.9, 1000.0, false",
    "1200.5, 1000.0, true",
    "1.0, , true"
  })
  @DisplayName("distance matches once it reaches the minimum")
  void matches(final double blocks, final Double minimum, final boolean expected) {
    assertEquals(
        expected,
        new SledTravelledCriterion.Conditions(Optional.empty(), Optional.ofNullable(minimum))
            .matches(blocks));
  }
}
