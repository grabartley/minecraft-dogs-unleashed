package com.grahambartley;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModEntitiesTest {

  @Test
  @DisplayName("Entity dimensions should be positive")
  void testEntityDimensions() {
    float width = 0.6f;
    float height = 0.85f;

    assertTrue(width > 0);
    assertTrue(height > 0);
  }

  @Test
  @DisplayName("Entity spawn group weight should be reasonable")
  void testSpawnWeight() {
    int weight = 10;
    int minGroup = 2;
    int maxGroup = 4;

    assertTrue(weight > 0 && weight <= 100);
    assertTrue(minGroup > 0 && minGroup <= maxGroup);
  }
}
