package com.grahambartley.dogsunleashed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.entity.DogGrowthRules;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DireWolfSettingsTest {

  @Test
  @DisplayName("the defaults match the dire wolf's own spawn settings and evolution rule")
  void defaultsMatchTheBreed() {
    final UnleashedDogBreed.SpawnSettings spawn = UnleashedDogBreed.DIRE_WOLF.spawnSettings();
    assertEquals(
        new DireWolfSettings(
            100, DogGrowthRules.BATTLES_TO_EVOLVE, spawn.minGroupSize(), spawn.maxGroupSize()),
        DireWolfSettings.DEFAULTS);
  }

  static Stream<Arguments> clampCases() {
    return Stream.of(
        Arguments.of(new DireWolfSettings(-5, 0, 1, 99), new DireWolfSettings(0, 1, 4, 16)),
        Arguments.of(
            new DireWolfSettings(900, 999, 20, 20), new DireWolfSettings(500, 200, 16, 16)),
        Arguments.of(new DireWolfSettings(100, 20, 8, 5), new DireWolfSettings(100, 20, 8, 8)));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("clampCases")
  @DisplayName("out of range values are clamped and the largest pack is never below the smallest")
  void valuesAreClamped(final DireWolfSettings raw, final DireWolfSettings expected) {
    assertEquals(expected, raw);
  }

  @Test
  @DisplayName("raising the smallest pack past the largest raises the largest with it")
  void smallestPackRaisesLargest() {
    final DireWolfSettings settings = DireWolfSettings.DEFAULTS.withSmallestPack(12);
    assertEquals(12, settings.smallestPack());
    assertEquals(12, settings.largestPack());
  }

  @Test
  @DisplayName("lowering the largest pack below the smallest lowers the smallest with it")
  void largestPackLowersSmallest() {
    final DireWolfSettings settings = DireWolfSettings.DEFAULTS.withLargestPack(4);
    assertEquals(4, settings.smallestPack());
    assertEquals(4, settings.largestPack());
  }

  @Test
  @DisplayName("each with method changes only its own setting")
  void withMethodsChangeOneSetting() {
    final DireWolfSettings base = new DireWolfSettings(100, 20, 5, 10);
    assertEquals(new DireWolfSettings(250, 20, 5, 10), base.withGrowthRateMultiplierPercent(250));
    assertEquals(new DireWolfSettings(100, 7, 5, 10), base.withBattlesToEvolve(7));
  }

  @Test
  @DisplayName("the settings survive a JSON round trip")
  void jsonRoundTrips() {
    final DireWolfSettings settings = new DireWolfSettings(150, 9, 6, 12);
    assertEquals(settings, DireWolfSettings.fromJson(settings.toJson()));
  }

  @Test
  @DisplayName("missing or invalid JSON values fall back to the defaults")
  void invalidJsonFallsBack() {
    final JsonObject json =
        JsonParser.parseString("{\"battlesToEvolve\": \"lots\", \"smallestPack\": 7}")
            .getAsJsonObject();
    final DireWolfSettings settings = DireWolfSettings.fromJson(json);
    assertEquals(DireWolfSettings.DEFAULTS.battlesToEvolve(), settings.battlesToEvolve());
    assertEquals(7, settings.smallestPack());
    assertEquals(
        DireWolfSettings.DEFAULTS.growthRateMultiplierPercent(),
        settings.growthRateMultiplierPercent());
  }
}
