package com.grahambartley.dogsunleashed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class SpawningSettingsTest {

  private static SpawningSettings settings(
      final int globalPercent, final Map<String, Integer> breedPercents) {
    return new SpawningSettings(true, globalPercent, breedPercents, false, true, true);
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
    assertFalse(base.withVillageStrays(false).villageStrays());
    assertTrue(base.withVillageStrays(false).traderPuppies());
    assertFalse(base.withTraderPuppies(false).traderPuppies());
    assertTrue(base.withTraderPuppies(false).villageStrays());
  }

  @Test
  @DisplayName("strays and trader puppies are on by default")
  void straysAndTraderPuppiesDefaultOn() {
    assertTrue(SpawningSettings.DEFAULTS.villageStrays());
    assertTrue(SpawningSettings.DEFAULTS.traderPuppies());
  }

  @Test
  @DisplayName("the settings survive a JSON round trip under their flat keys")
  void jsonRoundTrips() {
    final SpawningSettings settings =
        new SpawningSettings(false, 150, Map.of("beagle", 0, "direwolf", 321), false, false, false);
    final JsonObject root = new JsonObject();
    settings.writeJson(root);
    assertEquals(
        321, root.getAsJsonObject("breedSpawnRateMultipliersPercent").get("direwolf").getAsInt());
    assertEquals(settings, SpawningSettings.fromJson(new ConfigJsonFields(root)));
  }

  @Test
  @DisplayName("the spawn rate bounds allow turning spawns off and boosting them")
  void spawnRateMultiplierBoundsAllowDisablingAndBoosting() {
    assertEquals(0, SpawningSettings.RATE_MIN);
    assertTrue(SpawningSettings.RATE_MAX > SpawningSettings.DEFAULT_RATE_PERCENT);
  }

  @ParameterizedTest(name = "{0} defaults to 100")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("defaults() carries an entry of 100 for every naturally spawning breed")
  void defaultsCarryEveryNaturallySpawningBreedAtOneHundred(final UnleashedDogBreed breed) {
    final Integer multiplier =
        SpawningSettings.DEFAULTS.breedSpawnRateMultipliersPercent().get(breed.serializedId());
    if (breed.isNaturallySpawning()) {
      assertEquals(SpawningSettings.DEFAULT_RATE_PERCENT, multiplier);
    } else {
      assertNull(multiplier);
    }
  }

  static Stream<Arguments> spawnRateClampCases() {
    return Stream.of(
        Arguments.of(-100, SpawningSettings.RATE_MIN),
        Arguments.of(-1, SpawningSettings.RATE_MIN),
        Arguments.of(SpawningSettings.RATE_MIN, SpawningSettings.RATE_MIN),
        Arguments.of(100, 100),
        Arguments.of(SpawningSettings.RATE_MAX, SpawningSettings.RATE_MAX),
        Arguments.of(501, SpawningSettings.RATE_MAX),
        Arguments.of(99999, SpawningSettings.RATE_MAX));
  }

  @ParameterizedTest(name = "{0} clamps to {1}")
  @MethodSource("spawnRateClampCases")
  @DisplayName("the global and per-breed spawn rates are clamped to their bounds")
  void constructorClampsSpawnRateMultipliers(final int input, final int expected) {
    final SpawningSettings config = settings(input, Map.of("husky", input));
    assertEquals(expected, config.spawnRateMultiplierPercent());
    assertEquals(expected, config.breedSpawnRateMultipliersPercent().get("husky"));
  }

  @Test
  @DisplayName("constructor fills breeds missing from the map with the default multiplier")
  void constructorFillsMissingBreedsWithDefault() {
    final SpawningSettings config = settings(100, Map.of("beagle", 200));
    assertEquals(200, config.breedSpawnRateMultipliersPercent().get("beagle"));
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      if (breed != UnleashedDogBreed.BEAGLE && breed.isNaturallySpawning()) {
        assertEquals(
            SpawningSettings.DEFAULT_RATE_PERCENT,
            config.breedSpawnRateMultipliersPercent().get(breed.serializedId()),
            breed.serializedId());
      }
    }
  }

  @Test
  @DisplayName("constructor drops unknown breed keys from the map")
  void constructorDropsUnknownBreedKeys() {
    final SpawningSettings config = settings(100, Map.of("chihuahua", 300, "husky", 50));
    assertFalse(config.breedSpawnRateMultipliersPercent().containsKey("chihuahua"));
    assertEquals(50, config.breedSpawnRateMultipliersPercent().get("husky"));
  }

  static Stream<Arguments> effectiveSpawnWeightCases() {
    return Stream.of(
        Arguments.of("all defaults keep base weight", 10, 100, 100, 10),
        Arguments.of("double global doubles weight", 10, 200, 100, 20),
        Arguments.of("double breed doubles weight", 10, 100, 200, 20),
        Arguments.of("global and breed multiply together", 10, 200, 200, 40),
        Arguments.of("max both quintuples twice", 10, 500, 500, 250),
        Arguments.of("half and half rounds to nearest", 10, 50, 50, 3),
        Arguments.of("tiny product floors at 1", 10, 1, 1, 1),
        Arguments.of("small fraction below half floors at 1", 10, 5, 5, 1),
        Arguments.of("zero global disables", 10, 0, 100, 0),
        Arguments.of("zero breed disables", 10, 100, 0, 0),
        Arguments.of("zero both disables", 10, 0, 0, 0));
  }

  @ParameterizedTest(name = "{0}: base {1} x {2}% x {3}% -> {4}")
  @MethodSource("effectiveSpawnWeightCases")
  @DisplayName(
      "effectiveSpawnWeight applies max(1, round(base * global * breed / 10000)) and 0 disables")
  void effectiveSpawnWeightAppliesMultiplierFormula(
      final String label,
      final int baseWeight,
      final int globalPercent,
      final int breedPercent,
      final int expected) {
    final SpawningSettings config = settings(globalPercent, Map.of("husky", breedPercent));
    assertEquals(expected, config.effectiveSpawnWeight(baseWeight, "husky"), label);
  }

  static Stream<Arguments> spawnRateCases() {
    return Stream.of(
        Arguments.of(100, 100, 1.0),
        Arguments.of(300, 100, 3.0),
        Arguments.of(100, 300, 3.0),
        Arguments.of(200, 150, 3.0),
        Arguments.of(50, 50, 0.25),
        Arguments.of(500, 500, 25.0),
        Arguments.of(0, 100, 0.0),
        Arguments.of(100, 0, 0.0));
  }

  @ParameterizedTest(name = "global {0}% x breed {1}% -> {2}")
  @MethodSource("spawnRateCases")
  @DisplayName("spawnRate is the global rate times the breed rate, as a multiple of default")
  void spawnRateMultipliesGlobalAndBreedRates(
      final int globalPercent, final int breedPercent, final double expected) {
    final SpawningSettings config = settings(globalPercent, Map.of("husky", breedPercent));
    assertEquals(expected, config.spawnRate("husky"), 1e-9);
  }

  @Test
  @DisplayName("spawnRate treats an unknown breed id as the default rate")
  void spawnRateUnknownBreedUsesDefault() {
    assertEquals(2.0, settings(200, Map.of()).spawnRate("not-a-breed"), 1e-9);
  }

  @Test
  @DisplayName("effectiveSpawnWeight treats an unknown breed id as the default multiplier")
  void effectiveSpawnWeightUnknownBreedUsesDefault() {
    final SpawningSettings config = settings(200, Map.of());
    assertEquals(20, config.effectiveSpawnWeight(10, "not-a-breed"));
  }

  static Stream<Arguments> biomeSpawnChanges() {
    final SpawningSettings base = SpawningSettings.DEFAULTS;
    return Stream.of(
        Arguments.of("natural spawning toggled", base.withEnableNaturalSpawning(false), true),
        Arguments.of("global rate changed", base.withSpawnRateMultiplierPercent(150), true),
        Arguments.of(
            "one breed rate changed", base.withBreedSpawnRateMultiplierPercent("beagle", 0), true),
        Arguments.of(
            "cap independent spawning toggled",
            base.withCapIndependentSpawningEnabled(false),
            false),
        Arguments.of("same global rate set again", base.withSpawnRateMultiplierPercent(100), false),
        Arguments.of(
            "same breed rate set again",
            base.withBreedSpawnRateMultiplierPercent("beagle", 100),
            false),
        Arguments.of("same toggle set again", base.withEnableNaturalSpawning(true), false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("biomeSpawnChanges")
  @DisplayName("only settings that biome spawns read count as a biome spawn change")
  void biomeSpawnsDifferFrom(
      final String change, final SpawningSettings updated, final boolean expected) {
    assertEquals(expected, SpawningSettings.DEFAULTS.biomeSpawnsDifferFrom(updated));
    assertEquals(expected, updated.biomeSpawnsDifferFrom(SpawningSettings.DEFAULTS));
  }
}
