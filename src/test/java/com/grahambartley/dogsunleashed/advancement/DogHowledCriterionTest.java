package com.grahambartley.dogsunleashed.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DogHowledCriterionTest {

  @Test
  @DisplayName("the criterion is registered under dogs-unleashed:dog_howled")
  void exposesItsIdentifier() {
    assertEquals("dogs-unleashed:dog_howled", DogHowledCriterion.ID.toString());
  }
}
