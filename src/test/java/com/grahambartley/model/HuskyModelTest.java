package com.grahambartley.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HuskyModelTest {
  @Test
  @DisplayName("Model class should exist")
  void testModelClassExists() {
    assertDoesNotThrow(
        () -> {
          Class<?> modelClass = Class.forName("com.grahambartley.model.HuskyModel");
          assertNotNull(modelClass);
        });
  }
}
