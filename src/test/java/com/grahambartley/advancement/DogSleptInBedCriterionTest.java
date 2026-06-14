package com.grahambartley.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DogSleptInBedCriterionTest {

  @Test
  void exposesExpectedIdentifier() {
    assertEquals("dogs-unleashed:dog_slept_in_bed", DogSleptInBedCriterion.ID.toString());
    assertEquals("dogs-unleashed", DogSleptInBedCriterion.ID.getNamespace());
    assertEquals("dog_slept_in_bed", DogSleptInBedCriterion.ID.getPath());
  }

  @Test
  void keepsASingleSharedCriterionInstance() {
    assertTrue(DogSleptInBedCriterion.INSTANCE instanceof DogSleptInBedCriterion);
    assertTrue(DogSleptInBedCriterion.INSTANCE == DogSleptInBedCriterion.INSTANCE);
  }
}
