package com.grahambartley.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HuskyHowledCriterionTest {

  @Test
  void exposesExpectedIdentifier() {
    assertEquals("dogs-unleashed:husky_howled", HuskyHowledCriterion.ID.toString());
    assertEquals("dogs-unleashed", HuskyHowledCriterion.ID.getNamespace());
    assertEquals("husky_howled", HuskyHowledCriterion.ID.getPath());
  }

  @Test
  void keepsASingleSharedCriterionInstance() {
    assertTrue(HuskyHowledCriterion.INSTANCE instanceof HuskyHowledCriterion);
    assertTrue(HuskyHowledCriterion.INSTANCE == HuskyHowledCriterion.INSTANCE);
  }
}
