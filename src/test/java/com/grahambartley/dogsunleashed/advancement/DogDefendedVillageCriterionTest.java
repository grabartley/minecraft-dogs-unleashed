package com.grahambartley.dogsunleashed.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DogDefendedVillageCriterionTest {

  @Test
  @DisplayName("the criterion id is dogs-unleashed:dog_defended_village")
  void exposesItsIdentifier() {
    assertEquals("dogs-unleashed:dog_defended_village", DogDefendedVillageCriterion.ID.toString());
  }
}
