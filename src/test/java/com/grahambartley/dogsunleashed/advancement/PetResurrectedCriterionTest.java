package com.grahambartley.dogsunleashed.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PetResurrectedCriterionTest {

  @Test
  @DisplayName("the criterion id is dogs-unleashed:pet_resurrected")
  void exposesItsIdentifier() {
    assertEquals("dogs-unleashed:pet_resurrected", PetResurrectedCriterion.ID.toString());
  }
}
