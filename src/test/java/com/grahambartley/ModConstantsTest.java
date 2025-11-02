package com.grahambartley;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModConstantsTest {

  @Test
  @DisplayName("Spawn egg colors should be valid hex values")
  void testSpawnEggColors() {
    int primary = ModConstants.HUSKY_SPAWN_EGG_PRIMARY_COLOR;
    int secondary = ModConstants.HUSKY_SPAWN_EGG_SECONDARY_COLOR;

    assertTrue(primary >= 0x000000 && primary <= 0xFFFFFF, "Primary color must be valid hex");
    assertTrue(secondary >= 0x000000 && secondary <= 0xFFFFFF, "Secondary color must be valid hex");
  }

  @Test
  @DisplayName("Entity dimensions should be positive")
  void testEntityDimensions() {
    assertTrue(ModConstants.HUSKY_WIDTH > 0, "Width must be positive");
    assertTrue(ModConstants.HUSKY_HEIGHT > 0, "Height must be positive");
  }

  @Test
  @DisplayName("Spawn configuration should be valid")
  void testSpawnConfiguration() {
    assertTrue(
        ModConstants.HUSKY_SPAWN_WEIGHT >= 1 && ModConstants.HUSKY_SPAWN_WEIGHT <= 100,
        "Spawn weight should be 1-100");
    assertTrue(ModConstants.HUSKY_SPAWN_MIN_GROUP >= 1, "Min group size must be at least 1");
    assertTrue(
        ModConstants.HUSKY_SPAWN_MAX_GROUP >= ModConstants.HUSKY_SPAWN_MIN_GROUP,
        "Max group size must be >= min");
  }
}
