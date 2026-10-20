package com.grahambartley.dogsunleashed.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PetCuredCriterionTest {

  @Test
  @DisplayName("the criterion is registered under dogs-unleashed:pet_cured")
  void exposesItsIdentifier() {
    assertEquals("dogs-unleashed:pet_cured", PetCuredCriterion.ID.toString());
  }
}
