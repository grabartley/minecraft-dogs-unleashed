package com.grahambartley.dogsunleashed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DogsUnleashedConfigTest {

  @TempDir Path tempDir;

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
  @DisplayName("defaults() returns the documented default values")
  void defaultsReturnsDocumentedValues() {
    final DogsUnleashedConfig defaults = DogsUnleashedConfig.defaults();
    assertTrue(defaults.enableNaturalSpawning());
    assertTrue(defaults.gravesEnabled());
    assertTrue(defaults.autoSleepEnabled());
    assertEquals(32, defaults.autoSleepRangeBlocks());
    assertEquals(1.0f, defaults.barkVolume());
    assertEquals(1.5f, defaults.howlVolume());
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
        new DogsUnleashedConfig(false, false, false, 64, 0.5f, 0.25f);
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
    final DogsUnleashedConfig config = new DogsUnleashedConfig(true, true, true, input, 1.0f, 1.5f);
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
    final DogsUnleashedConfig config = new DogsUnleashedConfig(true, true, true, 32, input, input);
    assertEquals(expected, config.barkVolume());
    assertEquals(expected, config.howlVolume());
  }

  @Test
  @DisplayName("load() clamps out-of-range JSON values to their allowed bounds")
  void loadClampsOutOfRange() throws IOException {
    final Path path = tempDir.resolve("oor.json");
    Files.writeString(
        path,
        "{\"autoSleepRangeBlocks\": 999, \"barkVolume\": -5.0, \"howlVolume\": 10.0}",
        StandardCharsets.UTF_8);

    final DogsUnleashedConfig loaded = DogsUnleashedConfig.load(path);
    assertEquals(DogsUnleashedConfig.AUTO_SLEEP_RANGE_MAX, loaded.autoSleepRangeBlocks());
    assertEquals(DogsUnleashedConfig.VOLUME_MIN, loaded.barkVolume());
    assertEquals(DogsUnleashedConfig.VOLUME_MAX, loaded.howlVolume());
  }

  @Test
  @DisplayName("withXxx() returns updated config without mutating the original instance")
  void witherReturnsUpdatedConfigWithoutMutatingOriginal() {
    final DogsUnleashedConfig original = DogsUnleashedConfig.defaults();
    final DogsUnleashedConfig updated =
        original
            .withEnableNaturalSpawning(false)
            .withGravesEnabled(false)
            .withAutoSleepEnabled(false)
            .withAutoSleepRangeBlocks(64)
            .withBarkVolume(0.25f)
            .withHowlVolume(0.75f);

    assertEquals(DogsUnleashedConfig.defaults(), original);
    assertFalse(updated.enableNaturalSpawning());
    assertFalse(updated.gravesEnabled());
    assertFalse(updated.autoSleepEnabled());
    assertEquals(64, updated.autoSleepRangeBlocks());
    assertEquals(0.25f, updated.barkVolume());
    assertEquals(0.75f, updated.howlVolume());
  }
}
