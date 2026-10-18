package com.grahambartley.dogsunleashed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import org.junit.jupiter.params.provider.ValueSource;

class DogsUnleashedConfigTest {

  @TempDir Path tempDir;

  private static DogsUnleashedConfig configWithSpawnRates(
      final int globalPercent, final Map<String, Integer> breedPercents) {
    return new DogsUnleashedConfig(
        new SpawningSettings(true, globalPercent, breedPercents, false),
        new GameplaySettings(true, true, true),
        new SleepSettings(true, 32),
        new FurSettings(true, 100, 100),
        new SoundSettings(1.0f, 1.5f),
        DireWolfSettings.DEFAULTS);
  }

  @Test
  @DisplayName("AUTO_SLEEP_RANGE_MIN is below AUTO_SLEEP_RANGE_MAX")
  void autoSleepRangeMinIsBelowMax() {
    assertTrue(SleepSettings.RANGE_MIN < SleepSettings.RANGE_MAX);
  }

  @Test
  @DisplayName("AUTO_SLEEP_RANGE_MIN is at least 1 block")
  void autoSleepRangeMinIsAtLeastOne() {
    assertTrue(SleepSettings.RANGE_MIN >= 1);
  }

  @Test
  @DisplayName("VOLUME_MIN is non-negative and below VOLUME_MAX")
  void volumeMinIsNonNegativeAndBelowMax() {
    assertTrue(SoundSettings.VOLUME_MIN >= 0.0f);
    assertTrue(SoundSettings.VOLUME_MIN < SoundSettings.VOLUME_MAX);
  }

  @Test
  @DisplayName("SPAWN_RATE_MULTIPLIER bounds allow disabling and boosting spawns")
  void spawnRateMultiplierBoundsAllowDisablingAndBoosting() {
    assertEquals(0, SpawningSettings.RATE_MIN);
    assertTrue(SpawningSettings.RATE_MAX > SpawningSettings.DEFAULT_RATE_PERCENT);
  }

  @Test
  @DisplayName("defaults() returns the documented default values")
  void defaultsReturnsDocumentedValues() {
    final DogsUnleashedConfig defaults = DogsUnleashedConfig.defaults();
    assertTrue(defaults.spawning().enableNaturalSpawning());
    assertEquals(100, defaults.spawning().spawnRateMultiplierPercent());
    assertTrue(defaults.spawning().capIndependentSpawningEnabled());
    assertTrue(defaults.gameplay().gravesEnabled());
    assertTrue(defaults.gameplay().dropLeashOnPlayMode());
    assertTrue(defaults.sleep().autoSleepEnabled());
    assertEquals(32, defaults.sleep().autoSleepRangeBlocks());
    assertEquals(1.0f, defaults.sound().barkVolume());
    assertEquals(1.5f, defaults.sound().howlVolume());
    assertTrue(defaults.gameplay().showDogNames());
    assertTrue(defaults.fur().furGrowthEnabled());
    assertEquals(100, defaults.fur().furGrowthRateMultiplierPercent());
    assertEquals(100, defaults.fur().badHairDayChanceMultiplierPercent());
  }

  static Stream<Arguments> furMultiplierClampCases() {
    return Stream.of(
        Arguments.of(-5, 0),
        Arguments.of(0, 0),
        Arguments.of(250, 250),
        Arguments.of(500, 500),
        Arguments.of(501, 500));
  }

  @ParameterizedTest(name = "{0} clamps to {1}")
  @MethodSource("furMultiplierClampCases")
  @DisplayName("constructor clamps both fur multipliers to [0, 500]")
  void constructorClampsFurMultipliers(final int input, final int expected) {
    final DogsUnleashedConfig config =
        new DogsUnleashedConfig(
            new SpawningSettings(true, 100, Map.of(), false),
            new GameplaySettings(true, true, true),
            new SleepSettings(true, 32),
            new FurSettings(true, input, input),
            new SoundSettings(1.0f, 1.5f),
            DireWolfSettings.DEFAULTS);
    assertEquals(expected, config.fur().furGrowthRateMultiplierPercent());
    assertEquals(expected, config.fur().badHairDayChanceMultiplierPercent());
  }

  @ParameterizedTest(name = "{0} defaults to 100")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("defaults() carries an entry of 100 for every naturally spawning breed")
  void defaultsCarryEveryNaturallySpawningBreedAtOneHundred(final UnleashedDogBreed breed) {
    final Integer multiplier =
        DogsUnleashedConfig.defaults()
            .spawning()
            .breedSpawnRateMultipliersPercent()
            .get(breed.serializedId());
    if (breed.isNaturallySpawning()) {
      assertEquals(SpawningSettings.DEFAULT_RATE_PERCENT, multiplier);
    } else {
      assertNull(multiplier);
    }
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
            new SpawningSettings(false, 250, Map.of("husky", 0, "beagle", 40), false),
            new GameplaySettings(false, false, false),
            new SleepSettings(false, 64),
            new FurSettings(false, 250, 0),
            new SoundSettings(0.5f, 0.25f),
            new DireWolfSettings(
                175,
                8,
                6,
                9,
                new DireWolfPackSettings(false, true),
                new DireWolfStaminaSettings(false, 150, 60)));
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
    assertEquals(0.5f, loaded.sound().barkVolume());
    assertFalse(loaded.gameplay().gravesEnabled());
    assertTrue(loaded.gameplay().dropLeashOnPlayMode());
    assertTrue(loaded.spawning().enableNaturalSpawning());
    assertEquals(100, loaded.spawning().spawnRateMultiplierPercent());
    assertTrue(loaded.spawning().capIndependentSpawningEnabled());
    assertEquals(
        DogsUnleashedConfig.defaults().spawning().breedSpawnRateMultipliersPercent(),
        loaded.spawning().breedSpawnRateMultipliersPercent());
    assertTrue(loaded.sleep().autoSleepEnabled());
    assertEquals(32, loaded.sleep().autoSleepRangeBlocks());
    assertEquals(1.5f, loaded.sound().howlVolume());
    assertTrue(loaded.gameplay().showDogNames());
  }

  static Stream<Arguments> invalidFieldCases() {
    return Stream.of(
        Arguments.of("spawnRateMultiplierPercent", "\"abc\""),
        Arguments.of("autoSleepRangeBlocks", "\"abc\""),
        Arguments.of("barkVolume", "\"abc\""),
        Arguments.of("howlVolume", "\"abc\""),
        Arguments.of("autoSleepRangeBlocks", "{\"blocks\": 16}"),
        Arguments.of("spawnRateMultiplierPercent", "[50]"),
        Arguments.of("gravesEnabled", "\"yes\""),
        Arguments.of("showDogNames", "1"),
        Arguments.of("enableNaturalSpawning", "{}"),
        Arguments.of("breedSpawnRateMultipliersPercent", "{\"beagle\": \"high\"}"),
        Arguments.of("breedSpawnRateMultipliersPercent", "[]"));
  }

  @ParameterizedTest(name = "{0}={1} falls back to the default")
  @MethodSource("invalidFieldCases")
  @DisplayName("load() falls back per field on a wrong JSON type and keeps the valid fields")
  void loadFallsBackPerFieldOnWrongType(final String key, final String jsonValue)
      throws IOException {
    final Path path = tempDir.resolve("invalid.json");
    Files.writeString(
        path,
        "{\"howlVolume\": 0.75, \"autoSleepEnabled\": false, \"" + key + "\": " + jsonValue + "}",
        StandardCharsets.UTF_8);

    final DogsUnleashedConfig loaded = DogsUnleashedConfig.load(path);
    final DogsUnleashedConfig expected =
        DogsUnleashedConfig.defaults()
            .withSound(
                DogsUnleashedConfig.defaults()
                    .sound()
                    .withHowlVolume(
                        key.equals("howlVolume") ? SoundSettings.DEFAULTS.howlVolume() : 0.75f))
            .withSleep(SleepSettings.DEFAULTS.withAutoSleepEnabled(false));
    assertEquals(expected, loaded);
    assertTrue(Files.exists(path), "a file with a bad field is not malformed and stays in place");
  }

  @ParameterizedTest(name = "root [{0}]")
  @ValueSource(strings = {"[1, 2]", "\"text\"", "42", "", "null"})
  @DisplayName("load() returns defaults for a top-level value that is not an object")
  void loadReturnsDefaultsForNonObjectRoot(final String json) throws IOException {
    final Path path = tempDir.resolve("root.json");
    Files.writeString(path, json, StandardCharsets.UTF_8);
    assertEquals(DogsUnleashedConfig.defaults(), DogsUnleashedConfig.load(path));
  }

  @Test
  @DisplayName("loadOrCreate() writes the defaults when the file is missing")
  void loadOrCreateWritesDefaultsWhenMissing() {
    final Path path = tempDir.resolve("world").resolve("dogs-unleashed.json");

    assertEquals(DogsUnleashedConfig.defaults(), DogsUnleashedConfig.loadOrCreate(path));
    assertTrue(Files.exists(path), "missing config should be created with defaults");
    assertEquals(DogsUnleashedConfig.defaults(), DogsUnleashedConfig.load(path));
  }

  @Test
  @DisplayName("loadOrCreate() reads an existing file without overwriting it")
  void loadOrCreateReadsExistingFile() throws IOException {
    final Path path = tempDir.resolve("existing.json");
    Files.writeString(path, "{\"gravesEnabled\": false}", StandardCharsets.UTF_8);

    assertFalse(DogsUnleashedConfig.loadOrCreate(path).gameplay().gravesEnabled());
    assertEquals("{\"gravesEnabled\": false}", Files.readString(path, StandardCharsets.UTF_8));
  }

  @Test
  @DisplayName("loadOrCreate() returns defaults for a null path")
  void loadOrCreateNullPathReturnsDefaults() {
    assertEquals(DogsUnleashedConfig.defaults(), DogsUnleashedConfig.loadOrCreate(null));
  }

  static Stream<Arguments> autoSleepRangeClampCases() {
    return Stream.of(
        Arguments.of(0, SleepSettings.RANGE_MIN),
        Arguments.of(3, SleepSettings.RANGE_MIN),
        Arguments.of(SleepSettings.RANGE_MIN, SleepSettings.RANGE_MIN),
        Arguments.of(64, 64),
        Arguments.of(SleepSettings.RANGE_MAX, SleepSettings.RANGE_MAX),
        Arguments.of(500, SleepSettings.RANGE_MAX),
        Arguments.of(-50, SleepSettings.RANGE_MIN));
  }

  @ParameterizedTest(name = "{0} clamps to {1}")
  @MethodSource("autoSleepRangeClampCases")
  @DisplayName(
      "constructor clamps autoSleepRangeBlocks to [AUTO_SLEEP_RANGE_MIN, AUTO_SLEEP_RANGE_MAX]")
  void constructorClampsAutoSleepRange(final int input, final int expected) {
    final DogsUnleashedConfig config =
        new DogsUnleashedConfig(
            new SpawningSettings(true, 100, Map.of(), false),
            new GameplaySettings(true, true, true),
            new SleepSettings(true, input),
            new FurSettings(true, 100, 100),
            new SoundSettings(1.0f, 1.5f),
            DireWolfSettings.DEFAULTS);
    assertEquals(expected, config.sleep().autoSleepRangeBlocks());
  }

  static Stream<Arguments> volumeClampCases() {
    return Stream.of(
        Arguments.of(-1.0f, SoundSettings.VOLUME_MIN),
        Arguments.of(-0.0001f, SoundSettings.VOLUME_MIN),
        Arguments.of(SoundSettings.VOLUME_MIN, SoundSettings.VOLUME_MIN),
        Arguments.of(0.5f, 0.5f),
        Arguments.of(SoundSettings.VOLUME_MAX, SoundSettings.VOLUME_MAX),
        Arguments.of(2.5f, SoundSettings.VOLUME_MAX),
        Arguments.of(100.0f, SoundSettings.VOLUME_MAX));
  }

  @ParameterizedTest(name = "{0} clamps to {1}")
  @MethodSource("volumeClampCases")
  @DisplayName("constructor clamps barkVolume and howlVolume to [VOLUME_MIN, VOLUME_MAX]")
  void constructorClampsVolumes(final float input, final float expected) {
    final DogsUnleashedConfig config =
        new DogsUnleashedConfig(
            new SpawningSettings(true, 100, Map.of(), false),
            new GameplaySettings(true, true, true),
            new SleepSettings(true, 32),
            new FurSettings(true, 100, 100),
            new SoundSettings(input, input),
            DireWolfSettings.DEFAULTS);
    assertEquals(expected, config.sound().barkVolume());
    assertEquals(expected, config.sound().howlVolume());
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
  @DisplayName(
      "constructor clamps global and per-breed spawn rate multipliers to"
          + " [SPAWN_RATE_MULTIPLIER_MIN, SPAWN_RATE_MULTIPLIER_MAX]")
  void constructorClampsSpawnRateMultipliers(final int input, final int expected) {
    final DogsUnleashedConfig config = configWithSpawnRates(input, Map.of("husky", input));
    assertEquals(expected, config.spawning().spawnRateMultiplierPercent());
    assertEquals(expected, config.spawning().breedSpawnRateMultipliersPercent().get("husky"));
  }

  @Test
  @DisplayName("constructor fills breeds missing from the map with the default multiplier")
  void constructorFillsMissingBreedsWithDefault() {
    final DogsUnleashedConfig config = configWithSpawnRates(100, Map.of("beagle", 200));
    assertEquals(200, config.spawning().breedSpawnRateMultipliersPercent().get("beagle"));
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      if (breed != UnleashedDogBreed.BEAGLE && breed.isNaturallySpawning()) {
        assertEquals(
            SpawningSettings.DEFAULT_RATE_PERCENT,
            config.spawning().breedSpawnRateMultipliersPercent().get(breed.serializedId()),
            breed.serializedId());
      }
    }
  }

  @Test
  @DisplayName("constructor drops unknown breed keys from the map")
  void constructorDropsUnknownBreedKeys() {
    final DogsUnleashedConfig config =
        configWithSpawnRates(100, Map.of("chihuahua", 300, "husky", 50));
    assertFalse(config.spawning().breedSpawnRateMultipliersPercent().containsKey("chihuahua"));
    assertEquals(50, config.spawning().breedSpawnRateMultipliersPercent().get("husky"));
  }

  @Test
  @DisplayName("load() honors an opt-out capIndependentSpawningEnabled=false from JSON")
  void loadHonorsCapIndependentSpawningOptOut() throws IOException {
    final Path path = tempDir.resolve("capspawner.json");
    Files.writeString(path, "{\"capIndependentSpawningEnabled\": false}", StandardCharsets.UTF_8);
    assertFalse(DogsUnleashedConfig.load(path).spawning().capIndependentSpawningEnabled());
  }

  @Test
  @DisplayName("load() honors an opt-out dropLeashOnPlayMode=false from JSON")
  void loadHonorsDropLeashOnPlayModeOptOut() throws IOException {
    final Path path = tempDir.resolve("dropleash.json");
    Files.writeString(path, "{\"dropLeashOnPlayMode\": false}", StandardCharsets.UTF_8);
    assertFalse(DogsUnleashedConfig.load(path).gameplay().dropLeashOnPlayMode());
  }

  @Test
  @DisplayName("load() honors an opt-out showDogNames=false from JSON")
  void loadHonorsShowDogNamesOptOut() throws IOException {
    final Path path = tempDir.resolve("shownames.json");
    Files.writeString(path, "{\"showDogNames\": false}", StandardCharsets.UTF_8);
    assertFalse(DogsUnleashedConfig.load(path).gameplay().showDogNames());
  }

  @Test
  @DisplayName(
      "load() defaults showDogNames to true for a config written before the option existed")
  void loadPreExistingConfigWithoutShowDogNamesDefaultsToTrue() throws IOException {
    final Path path = tempDir.resolve("legacy.json");
    Files.writeString(
        path,
        "{\"enableNaturalSpawning\": true, \"spawnRateMultiplierPercent\": 100,"
            + " \"capIndependentSpawningEnabled\": true, \"gravesEnabled\": true,"
            + " \"dropLeashOnPlayMode\": true, \"autoSleepEnabled\": true,"
            + " \"autoSleepRangeBlocks\": 32, \"barkVolume\": 1.0, \"howlVolume\": 1.5}",
        StandardCharsets.UTF_8);

    assertTrue(DogsUnleashedConfig.load(path).gameplay().showDogNames());
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
    assertFalse(loaded.spawning().breedSpawnRateMultipliersPercent().containsKey("poodle"));
    assertEquals(25, loaded.spawning().breedSpawnRateMultipliersPercent().get("beagle"));
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
    assertEquals(SleepSettings.RANGE_MAX, loaded.sleep().autoSleepRangeBlocks());
    assertEquals(SoundSettings.VOLUME_MIN, loaded.sound().barkVolume());
    assertEquals(SoundSettings.VOLUME_MAX, loaded.sound().howlVolume());
    assertEquals(SpawningSettings.RATE_MAX, loaded.spawning().spawnRateMultiplierPercent());
    assertEquals(
        SpawningSettings.RATE_MIN,
        loaded.spawning().breedSpawnRateMultipliersPercent().get("husky"));
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
    assertEquals(expected, config.spawning().effectiveSpawnWeight(baseWeight, "husky"), label);
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
    final DogsUnleashedConfig config =
        configWithSpawnRates(globalPercent, Map.of("husky", breedPercent));
    assertEquals(expected, config.spawning().spawnRate("husky"), 1e-9);
  }

  @Test
  @DisplayName("spawnRate treats an unknown breed id as the default rate")
  void spawnRateUnknownBreedUsesDefault() {
    assertEquals(
        2.0, configWithSpawnRates(200, Map.of()).spawning().spawnRate("not-a-breed"), 1e-9);
  }

  @Test
  @DisplayName("effectiveSpawnWeight treats an unknown breed id as the default multiplier")
  void effectiveSpawnWeightUnknownBreedUsesDefault() {
    final DogsUnleashedConfig config = configWithSpawnRates(200, Map.of());
    assertEquals(20, config.spawning().effectiveSpawnWeight(10, "not-a-breed"));
  }

  @Test
  @DisplayName("withXxx() returns updated config without mutating the original instance")
  void witherReturnsUpdatedConfigWithoutMutatingOriginal() {
    final DogsUnleashedConfig original = DogsUnleashedConfig.defaults();
    final DogsUnleashedConfig updated =
        original
            .withSpawning(
                original
                    .spawning()
                    .withEnableNaturalSpawning(false)
                    .withSpawnRateMultiplierPercent(250)
                    .withBreedSpawnRateMultiplierPercent("beagle", 0)
                    .withCapIndependentSpawningEnabled(false))
            .withGameplay(
                original
                    .gameplay()
                    .withGravesEnabled(false)
                    .withDropLeashOnPlayMode(false)
                    .withShowDogNames(false))
            .withSleep(original.sleep().withAutoSleepEnabled(false).withAutoSleepRangeBlocks(64))
            .withSound(original.sound().withBarkVolume(0.25f).withHowlVolume(0.75f));

    assertEquals(DogsUnleashedConfig.defaults(), original);
    assertFalse(updated.spawning().enableNaturalSpawning());
    assertEquals(250, updated.spawning().spawnRateMultiplierPercent());
    assertEquals(0, updated.spawning().breedSpawnRateMultipliersPercent().get("beagle"));
    assertEquals(100, updated.spawning().breedSpawnRateMultipliersPercent().get("husky"));
    assertFalse(updated.spawning().capIndependentSpawningEnabled());
    assertFalse(updated.gameplay().gravesEnabled());
    assertFalse(updated.gameplay().dropLeashOnPlayMode());
    assertFalse(updated.sleep().autoSleepEnabled());
    assertEquals(64, updated.sleep().autoSleepRangeBlocks());
    assertEquals(0.25f, updated.sound().barkVolume());
    assertEquals(0.75f, updated.sound().howlVolume());
    assertFalse(updated.gameplay().showDogNames());
  }

  @Test
  @DisplayName("a config file saved on main loads with every value kept and saves back the same")
  void aConfigSavedOnMainLoadsAndSavesUnchanged() throws IOException {
    final Path path = tempDir.resolve("server-config.json");
    try (var in = getClass().getResourceAsStream("/config/server-config-saved-on-main.json")) {
      Files.write(path, in.readAllBytes());
    }
    final JsonObject saved = JsonParser.parseString(Files.readString(path)).getAsJsonObject();

    final DogsUnleashedConfig loaded = DogsUnleashedConfig.load(path);

    assertEquals(
        new DogsUnleashedConfig(
            new SpawningSettings(
                false,
                150,
                Map.of(
                    "husky", 90,
                    "dachshund", 80,
                    "beagle", 0,
                    "goldenretriever", 120,
                    "shibainu", 60,
                    "direwolf", 321),
                false),
            new GameplaySettings(false, false, false),
            new SleepSettings(false, 48),
            new FurSettings(false, 250, 40),
            new SoundSettings(0.75f, 1.25f),
            new DireWolfSettings(
                500,
                20,
                5,
                10,
                new DireWolfPackSettings(false, true),
                new DireWolfStaminaSettings(false, 150, 75))),
        loaded);
    assertTrue(DogsUnleashedConfig.save(path, loaded));
    assertEquals(saved, JsonParser.parseString(Files.readString(path)).getAsJsonObject());
  }
}
