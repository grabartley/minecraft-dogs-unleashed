package com.grahambartley.dogsunleashed.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FetchReturnedCriterionTest {

  @Test
  @DisplayName("the criterion is registered under dogs-unleashed:fetch_returned")
  void exposesItsIdentifier() {
    assertEquals("dogs-unleashed:fetch_returned", FetchReturnedCriterion.ID.toString());
  }
}
