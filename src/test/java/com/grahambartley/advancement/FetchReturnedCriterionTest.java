package com.grahambartley.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FetchReturnedCriterionTest {

  @Test
  void exposesExpectedIdentifier() {
    assertEquals("dogs-unleashed:fetch_returned", FetchReturnedCriterion.ID.toString());
    assertEquals("dogs-unleashed", FetchReturnedCriterion.ID.getNamespace());
    assertEquals("fetch_returned", FetchReturnedCriterion.ID.getPath());
  }

  @Test
  void keepsASingleSharedCriterionInstance() {
    assertTrue(FetchReturnedCriterion.INSTANCE instanceof FetchReturnedCriterion);
    assertTrue(FetchReturnedCriterion.INSTANCE == FetchReturnedCriterion.INSTANCE);
  }
}
