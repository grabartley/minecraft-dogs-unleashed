package com.grahambartley.dogsunleashed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class DogsUnleashedConfigTest {

  @TempDir Path tempDir;

  private static DogsUnleashedConfig configWithSpawnRates(
      final int globalPercent, final Map<String, Integer> breedPercents) {
    return new DogsUnleashedConfig(true, globalPercent, breedPercents, true, true, 32, 1.0f, 1.5f);
  }

  @Test
  @DisplayName("AUTO_SLEEP_RANGE_MIN is below AUTO_SLEEP_RANGE_MAX")
  void autoSleepRangeMinIsBelowMax() {
    assertTrue(DogsUnleashedConfig.AUTO_SLEEP_RANGE_MIN < DogsUnleashedConfig.AUTO_SLEEP_RANGE_MAX);
  }

  @Test
  @DisplayName("AUTO_SLEEP_RANGE_MIN is at least 1 block")
  void autoSleepRangeMinIsAtLeastOne() {
    assertTrue(DogsUnleashedConfig.AUTO_SLEEP_RANGE_MIN >= 1);
  }

  @Test
  @DisplayName("VOLUME_MIN is non-negative and below VOLUME_MAX")
  void volumeMinIsNonNegativeAndBelowMax() {
    assertTrue(DogsUnleashedConfig.VOLUME_MIN >= 0.0f);
    assertTrue(DogsUnleashedConfig.VOLUME_MIN < DogsUnleashedConfig.VOLUME_MAX);
  }

  @Test
  @DisplayName("SPAWN_RATE_MULTIPLIER bounds allow disabling and boosting spawns")
  void spawnRateMultiplierBoundsAllowDisablingAndBoosting() {
    assertEquals(0, DogsUnleashedConfig.SPAWN_RATE_MULTIPLIER_MIN);
    assertTrue(
        DogsUnleashedConfig.SPAWN_RATE_MULTIPLIER_MAX
            > DogsUnleashedConfig.DEFAULT_SPAWN_RATE_MULTIPLIER_PERCENT);
  }

  @Test
  @DisplayName("defaults() returns the documented default values")
  void defaultsReturnsDocumentedValues() {
    final DogsUnleashedConfig defaults = DogsUnleashedConfig.defaults();
    assertTrue(defaults.enableNaturalSpawning());
    assertEquals(100, defaults.spawnRateMultiplierPercent());
    assertTrue(defaults.gravesEnabled());
    assertTrue(defaults.autoSleepEnabled());
    assertEquals(32, defaults.autoSleepRangeBlocks());
    assertEquals(1.0f, defaults.barkVolume());
    assertEquals(1.5f, defaults.howlVolume());
  }

  @ParameterizedTest(name = "{0} defaults to 100")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("defaults() carries an entry of 100 for every breed")
  void defaultsCarryEveryBreedAtOneHundred(final UnleashedDogBreed breed) {
    assertEquals(
        DogsUnleashedConfig.DEFAULT_SPAWN_RATE_MULTIPLIER_PERCENT,
        DogsUnleashedConfig.defaults()
            .breedSpawnRateMultipliersPercent()
            .get(breed.serializedId()));
  }

  @Test
  @DisplayName("load() falls back to defaults when the file is missing")
  void loadMissingFileReturnsDefaults() {
    assertEquals(
        DogsUnleashedConfig.defaults(), DogsUnleashedConfig.load(tempDir.resolve("missing.json")));
  }

  @Test
  @DisplayName("load() falls back to defaults when path is null")
  void loadNullPathReturnsDefaults() {
    assertEquals(DogsUnleashedConfig.defaults(), DogsUnleashedConfig.load(null));
  }

  @Test
  @DisplayName("save() then load() round-trips every field")
  void saveAndLoadRoundTripsEveryField() throws IOException {
    final Path path = tempDir.resolve("config.json");
    final DogsUnleashedConfig original =
        new DogsUnleashedConfig(
            false, 250, Map.of("husky", 0, "beagle", 40), false, false, 64, 0.5f, 0.25f);
    assertTrue(DogsUnleashedConfig.save(path, original));
    assertTrue(Files.exists(path));
    assertEquals(original, DogsUnleashedConfig.load(path));
  }

  @Test
  @DisplayName("load() with malformed JSON returns defaults and renames the broken file")
  void loadMalformedJsonReturnsDefaultsAndBacksUp() throws IOException {
    final Path path = tempDir.resolve("malformed.json");
    Files.writeString(path, "this is not valid json {{{", StandardCharsets.UTF_8);

    assertEquals(DogsUnleashedConfig.defaults(), DogsUnleashedConfig.load(path));
    assertFalse(Files.exists(path), "broken file should have been renamed away");
    final boolean backupExists =
        Files.list(tempDir)
            .anyMatch(p -> p.getFileName().toString().startsWith("malformed.broken."));
    assertTrue(backupExists, "a .broken.<timestamp>.json backup should be present");
  }

  @Test
  @DisplayName("load() fills missing keys with defaults while honoring present keys")
  void loadMissingKeysFillsInDefaults() throws IOException {
    final Path path = tempDir.resolve("partial.json");
    Files.writeString(
        path, "{\"barkVolume\": 0.5, \"gravesEnabled\": false}", StandardCharsets.UTF_8);

    final DogsUnleashedConfig loaded = DogsUnleashedConfig.load(path);
    assertEquals(0.5f, loaded.barkVolume());
    assertFalse(loaded.gravesEnabled());
    assertTrue(loaded.enableNaturalSpawning());
    assertEquals(100, loaded.spawnRateMultiplierPercent());
    assertEquals(
        DogsUnleashedConfig.defaults().breedSpawnRateMultipliersPercent(),
        loaded.breedSpawnRateMultipliersPercent());
    assertTrue(loaded.autoSleepEnabled());
    assertEquals(32, loaded.autoSleepRangeBlocks());
    assertEquals(1.5f, loaded.howlVolume());
  }

  static Stream<Arguments> autoSleepRangeClampCases() {
    return Stream.of(
        Arguments.of(0, DogsUnleashedConfig.AUTO_SLEEP_RANGE_MIN),
        Arguments.of(3, DogsUnleashedConfig.AUTO_SLEEP_RANGE_MIN),
        Arguments.of(
            DogsUnleashedConfig.AUTO_SLEEP_RANGE_MIN, DogsUnleashedConfig.AUTO_SLEEP_RANGE_MIN),
        Arguments.of(64, 64),
        Arguments.of(
            DogsUnleashedConfig.AUTO_SLEEP_RANGE_MAX, DogsUnleashedConfig.AUTO_SLEEP_RANGE_MAX),
        Arguments.of(500, DogsUnleashedConfig.AUTO_SLEEP_RANGE_MAX),
        Arguments.of(-50, DogsUnleashedConfig.AUTO_SLEEP_RANGE_MIN));
  }

  @ParameterizedTest(name = "{0} clamps to {1}")
  @MethodSource("autoSleepRangeClampCases")
  @DisplayName(
      "constructor clamps autoSleepRangeBlocks to [AUTO_SLEEP_RANGE_MIN, AUTO_SLEEP_RANGE_MAX]")
  void constructorClampsAutoSleepRange(final int input, final int expected) {
    final DogsUnleashedConfig config =
        new DogsUnleashedConfig(true, 100, Map.of(), true, true, input, 1.0f, 1.5f);
    assertEquals(expected, config.autoSleepRangeBlocks());
  }

  static Stream<Arguments> volumeClampCases() {
    return Stream.of(
        Arguments.of(-1.0f, DogsUnleashedConfig.VOLUME_MIN),
        Arguments.of(-0.0001f, DogsUnleashedConfig.VOLUME_MIN),
        Arguments.of(DogsUnleashedConfig.VOLUME_MIN, DogsUnleashedConfig.VOLUME_MIN),
        Arguments.of(0.5f, 0.5f),
        Arguments.of(DogsUnleashedConfig.VOLUME_MAX, DogsUnleashedConfig.VOLUME_MAX),
        Arguments.of(2.5f, DogsUnleashedConfig.VOLUME_MAX),
        Arguments.of(100.0f, DogsUnleashedConfig.VOLUME_MAX));
  }

  @ParameterizedTest(name = "{0} clamps to {1}")
  @MethodSource("volumeClampCases")
  @DisplayName("constructor clamps barkVolume and howlVolume to [VOLUME_MIN, VOLUME_MAX]")
  void constructorClampsVolumes(final float input, final float expected) {
    final DogsUnleashedConfig config =
        new DogsUnleashedConfig(true, 100, Map.of(), true, true, 32, input, input);
    assertEquals(expected, config.barkVolume());
    assertEquals(expected, config.howlVolume());
  }

  static Stream<Arguments> spawnRateClampCases() {
    return Stream.of(
        Arguments.of(-100, DogsUnleashedConfig.SPAWN_RATE_MULTIPLIER_MIN),
        Arguments.of(-1, DogsUnleashedConfig.SPAWN_RATE_MULTIPLIER_MIN),
        Arguments.of(
            DogsUnleashedConfig.SPAWN_RATE_MULTIPLIER_MIN,
            DogsUnleashedConfig.SPAWN_RATE_MULTIPLIER_MIN),
        Arguments.of(100, 100),
        Arguments.of(
            DogsUnleashedConfig.SPAWN_RATE_MULTIPLIER_MAX,
            DogsUnleashedConfig.SPAWN_RATE_MULTIPLIER_MAX),
        Arguments.of(501, DogsUnleashedConfig.SPAWN_RATE_MULTIPLIER_MAX),
        Arguments.of(99999, DogsUnleashedConfig.SPAWN_RATE_MULTIPLIER_MAX));
  }

  @ParameterizedTest(name = "{0} clamps to {1}")
  @MethodSource("spawnRateClampCases")
  @DisplayName(
      "constructor clamps global and per-breed spawn rate multipliers to"
          + " [SPAWN_RATE_MULTIPLIER_MIN, SPAWN_RATE_MULTIPLIER_MAX]")
  void constructorClampsSpawnRateMultipliers(final int input, final int expected) {
    final DogsUnleashedConfig config = configWithSpawnRates(input, Map.of("husky", input));
    assertEquals(expected, config.spawnRateMultiplierPercent());
    assertEquals(expected, config.breedSpawnRateMultipliersPercent().get("husky"));
  }

  @Test
  @DisplayName("constructor fills breeds missing from the map with the default multiplier")
  void constructorFillsMissingBreedsWithDefault() {
    final DogsUnleashedConfig config = configWithSpawnRates(100, Map.of("beagle", 200));
    assertEquals(200, config.breedSpawnRateMultipliersPercent().get("beagle"));
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      if (breed != UnleashedDogBreed.BEAGLE) {
        assertEquals(
            DogsUnleashedConfig.DEFAULT_SPAWN_RATE_MULTIPLIER_PERCENT,
            config.breedSpawnRateMultipliersPercent().get(breed.serializedId()),
            breed.serializedId());
      }
    }
  }

  @Test
  @DisplayName("constructor drops unknown breed keys from the map")
  void constructorDropsUnknownBreedKeys() {
    final DogsUnleashedConfig config =
        configWithSpawnRates(100, Map.of("chihuahua", 300, "husky", 50));
    assertFalse(config.breedSpawnRateMultipliersPercent().containsKey("chihuahua"));
    assertEquals(50, config.breedSpawnRateMultipliersPercent().get("husky"));
  }

  @Test
  @DisplayName("load() ignores unknown breed keys and keeps known ones")
  void loadDropsUnknownBreedKeys() throws IOException {
    final Path path = tempDir.resolve("unknown-breed.json");
    Files.writeString(
        path,
        "{\"breedSpawnRateMultipliersPercent\": {\"poodle\": 400, \"beagle\": 25}}",
        StandardCharsets.UTF_8);

    final DogsUnleashedConfig loaded = DogsUnleashedConfig.load(path);
    assertFalse(loaded.breedSpawnRateMultipliersPercent().containsKey("poodle"));
    assertEquals(25, loaded.breedSpawnRateMultipliersPercent().get("beagle"));
  }

  @Test
  @DisplayName("load() clamps out-of-range JSON values to their allowed bounds")
  void loadClampsOutOfRange() throws IOException {
    final Path path = tempDir.resolve("oor.json");
    Files.writeString(
        path,
        "{\"autoSleepRangeBlocks\": 999, \"barkVolume\": -5.0, \"howlVolume\": 10.0,"
            + " \"spawnRateMultiplierPercent\": 9000,"
            + " \"breedSpawnRateMultipliersPercent\": {\"husky\": -20}}",
        StandardCharsets.UTF_8);

    final DogsUnleashedConfig loaded = DogsUnleashedConfig.load(path);
    assertEquals(DogsUnleashedConfig.AUTO_SLEEP_RANGE_MAX, loaded.autoSleepRangeBlocks());
    assertEquals(DogsUnleashedConfig.VOLUME_MIN, loaded.barkVolume());
    assertEquals(DogsUnleashedConfig.VOLUME_MAX, loaded.howlVolume());
    assertEquals(
        DogsUnleashedConfig.SPAWN_RATE_MULTIPLIER_MAX, loaded.spawnRateMultiplierPercent());
    assertEquals(
        DogsUnleashedConfig.SPAWN_RATE_MULTIPLIER_MIN,
        loaded.breedSpawnRateMultipliersPercent().get("husky"));
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
    final DogsUnleashedConfig config =
        configWithSpawnRates(globalPercent, Map.of("husky", breedPercent));
    assertEquals(expected, config.effectiveSpawnWeight(baseWeight, "husky"), label);
  }

  @Test
  @DisplayName("effectiveSpawnWeight treats an unknown breed id as the default multiplier")
  void effectiveSpawnWeightUnknownBreedUsesDefault() {
    final DogsUnleashedConfig config = configWithSpawnRates(200, Map.of());
    assertEquals(20, config.effectiveSpawnWeight(10, "not-a-breed"));
  }

  @Test
  @DisplayName("withXxx() returns updated config without mutating the original instance")
  void witherReturnsUpdatedConfigWithoutMutatingOriginal() {
    final DogsUnleashedConfig original = DogsUnleashedConfig.defaults();
    final DogsUnleashedConfig updated =
        original
            .withEnableNaturalSpawning(false)
            .withSpawnRateMultiplierPercent(250)
            .withBreedSpawnRateMultiplierPercent("beagle", 0)
            .withGravesEnabled(false)
            .withAutoSleepEnabled(false)
            .withAutoSleepRangeBlocks(64)
            .withBarkVolume(0.25f)
            .withHowlVolume(0.75f);

    assertEquals(DogsUnleashedConfig.defaults(), original);
    assertFalse(updated.enableNaturalSpawning());
    assertEquals(250, updated.spawnRateMultiplierPercent());
    assertEquals(0, updated.breedSpawnRateMultipliersPercent().get("beagle"));
    assertEquals(100, updated.breedSpawnRateMultipliersPercent().get("husky"));
    assertFalse(updated.gravesEnabled());
    assertFalse(updated.autoSleepEnabled());
    assertEquals(64, updated.autoSleepRangeBlocks());
    assertEquals(0.25f, updated.barkVolume());
    assertEquals(0.75f, updated.howlVolume());
  }
}
