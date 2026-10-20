package com.grahambartley.dogsunleashed.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DogSleptInBedCriterionTest {

  @Test
  @DisplayName("the criterion is registered under dogs-unleashed:dog_slept_in_bed")
  void exposesItsIdentifier() {
    assertEquals("dogs-unleashed:dog_slept_in_bed", DogSleptInBedCriterion.ID.toString());
  }
}
