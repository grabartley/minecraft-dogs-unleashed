package com.grahambartley;

import static org.junit.jupiter.api.Assertions.*;

import com.grahambartley.entity.UnleashedDogBreed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModConstantsTest {

  @Test
  @DisplayName("Spawn egg colors should be valid hex values for every breed")
  void testSpawnEggColors() {
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      final int primary = breed.spawnEggColors().primary();
      final int secondary = breed.spawnEggColors().secondary();

      assertTrue(primary >= 0x000000 && primary <= 0xFFFFFF, "Primary color must be valid hex");
      assertTrue(
          secondary >= 0x000000 && secondary <= 0xFFFFFF, "Secondary color must be valid hex");
    }
  }

  @Test
  @DisplayName("Entity dimensions should be positive for every breed")
  void testEntityDimensions() {
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      assertTrue(breed.dimensions().width() > 0, "Width must be positive");
      assertTrue(breed.dimensions().height() > 0, "Height must be positive");
    }
  }

  @Test
  @DisplayName("Spawn configuration should be valid for every breed")
  void testSpawnConfiguration() {
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      final UnleashedDogBreed.SpawnSettings spawnSettings = breed.spawnSettings();
      assertTrue(spawnSettings.weight() >= 1 && spawnSettings.weight() <= 100);
      assertTrue(spawnSettings.minGroupSize() >= 1, "Min group size must be at least 1");
      assertTrue(
          spawnSettings.maxGroupSize() >= spawnSettings.minGroupSize(),
          "Max group size must be >= min");
      assertTrue(spawnSettings.biomes().length > 0, "Every breed should have at least one biome");
    }
  }
}
