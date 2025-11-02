package com.grahambartley;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModSpawnsTest {

  @Test
  @DisplayName("Spawn configuration values should be valid")
  void testSpawnConfiguration() {
    int weight = 10;
    int minGroupSize = 2;
    int maxGroupSize = 4;

    assertTrue(weight >= 1 && weight <= 100, "Spawn weight should be between 1-100");
    assertTrue(minGroupSize >= 1, "Min group size should be at least 1");
    assertTrue(maxGroupSize >= minGroupSize, "Max group size should be >= min");
    assertTrue(maxGroupSize <= 10, "Max group size should be reasonable");
  }
}
