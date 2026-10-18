package com.grahambartley.dogsunleashed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SpawningSettingsTest {

  @Test
  @DisplayName("every naturally spawning breed starts at the default rate")
  void defaultsCoverEverySpawningBreed() {
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      assertEquals(
          breed.isNaturallySpawning(),
          SpawningSettings.DEFAULTS
              .breedSpawnRateMultipliersPercent()
              .containsKey(breed.serializedId()));
    }
  }

  @Test
  @DisplayName("unknown breeds are dropped and out of range rates are clamped")
  void breedMapIsNormalised() {
    final SpawningSettings settings =
        new SpawningSettings(true, 9999, Map.of("husky", 9999, "unicorn", 50), true);
    assertEquals(SpawningSettings.RATE_MAX, settings.spawnRateMultiplierPercent());
    assertEquals(
        SpawningSettings.RATE_MAX, settings.breedSpawnRateMultipliersPercent().get("husky"));
    assertFalse(settings.breedSpawnRateMultipliersPercent().containsKey("unicorn"));
  }

  static Stream<Arguments> weightCases() {
    return Stream.of(
        Arguments.of(100, 100, 10, 10),
        Arguments.of(200, 100, 10, 20),
        Arguments.of(100, 50, 10, 5),
        Arguments.of(0, 100, 10, 0),
        Arguments.of(100, 0, 10, 0),
        Arguments.of(1, 1, 10, 1));
  }

  @ParameterizedTest(name = "global {0}% x breed {1}% of {2} = {3}")
  @MethodSource("weightCases")
  @DisplayName("the spawn weight scales by both rates and never rounds a live spawn to zero")
  void effectiveSpawnWeight(final int global, final int breed, final int base, final int expected) {
    final SpawningSettings settings =
        new SpawningSettings(true, global, Map.of("husky", breed), true);
    assertEquals(expected, settings.effectiveSpawnWeight(base, "husky"));
  }

  @Test
  @DisplayName("each with method changes only its own setting")
  void withMethodsChangeOneSetting() {
    final SpawningSettings base = SpawningSettings.DEFAULTS;
    assertFalse(base.withEnableNaturalSpawning(false).enableNaturalSpawning());
    assertEquals(250, base.withSpawnRateMultiplierPercent(250).spawnRateMultiplierPercent());
    assertEquals(
        0,
        base.withBreedSpawnRateMultiplierPercent("beagle", 0)
            .breedSpawnRateMultipliersPercent()
            .get("beagle"));
    assertFalse(base.withCapIndependentSpawningEnabled(false).capIndependentSpawningEnabled());
    assertTrue(base.withCapIndependentSpawningEnabled(false).enableNaturalSpawning());
  }

  @Test
  @DisplayName("the settings survive a JSON round trip under their flat keys")
  void jsonRoundTrips() {
    final SpawningSettings settings =
        new SpawningSettings(false, 150, Map.of("beagle", 0, "direwolf", 321), false);
    final JsonObject root = new JsonObject();
    settings.writeJson(root);
    assertEquals(
        321, root.getAsJsonObject("breedSpawnRateMultipliersPercent").get("direwolf").getAsInt());
    assertEquals(settings, SpawningSettings.fromJson(new ConfigJsonFields(root)));
  }
}
