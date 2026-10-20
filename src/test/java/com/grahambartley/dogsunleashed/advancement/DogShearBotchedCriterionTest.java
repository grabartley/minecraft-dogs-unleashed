package com.grahambartley.dogsunleashed.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DogShearBotchedCriterionTest {

  @Test
  @DisplayName("the criterion id is dogs-unleashed:dog_shear_botched")
  void exposesItsIdentifier() {
    assertEquals("dogs-unleashed:dog_shear_botched", DogShearBotchedCriterion.ID.toString());
  }
}
