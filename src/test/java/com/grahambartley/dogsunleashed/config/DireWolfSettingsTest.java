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
            100,
            DogGrowthRules.BATTLES_TO_EVOLVE,
            spawn.minGroupSize(),
            spawn.maxGroupSize(),
            DireWolfPackSettings.DEFAULTS,
            DireWolfStaminaSettings.DEFAULTS),
        DireWolfSettings.DEFAULTS);
  }

  static Stream<Arguments> clampCases() {
    return Stream.of(
        Arguments.of(
            new DireWolfSettings(
                -5, 0, 1, 99, DireWolfPackSettings.DEFAULTS, DireWolfStaminaSettings.DEFAULTS),
            new DireWolfSettings(
                0, 1, 4, 16, DireWolfPackSettings.DEFAULTS, DireWolfStaminaSettings.DEFAULTS)),
        Arguments.of(
            new DireWolfSettings(
                900, 999, 20, 20, DireWolfPackSettings.DEFAULTS, DireWolfStaminaSettings.DEFAULTS),
            new DireWolfSettings(
                500, 200, 16, 16, DireWolfPackSettings.DEFAULTS, DireWolfStaminaSettings.DEFAULTS)),
        Arguments.of(
            new DireWolfSettings(
                100, 20, 8, 5, DireWolfPackSettings.DEFAULTS, DireWolfStaminaSettings.DEFAULTS),
            new DireWolfSettings(
                100, 20, 8, 8, DireWolfPackSettings.DEFAULTS, DireWolfStaminaSettings.DEFAULTS)));
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
    final DireWolfSettings base =
        new DireWolfSettings(
            100, 20, 5, 10, DireWolfPackSettings.DEFAULTS, DireWolfStaminaSettings.DEFAULTS);
    assertEquals(
        new DireWolfSettings(
            250, 20, 5, 10, DireWolfPackSettings.DEFAULTS, DireWolfStaminaSettings.DEFAULTS),
        base.withGrowthRateMultiplierPercent(250));
    assertEquals(
        new DireWolfSettings(
            100, 7, 5, 10, DireWolfPackSettings.DEFAULTS, DireWolfStaminaSettings.DEFAULTS),
        base.withBattlesToEvolve(7));
  }

  @Test
  @DisplayName("missing pack and stamina groups fall back to their defaults")
  void nullGroupsUseDefaults() {
    final DireWolfSettings settings = new DireWolfSettings(100, 20, 5, 10, null, null);
    assertEquals(DireWolfPackSettings.DEFAULTS, settings.packs());
    assertEquals(DireWolfStaminaSettings.DEFAULTS, settings.stamina());
  }

  @Test
  @DisplayName("the pack and stamina groups can be replaced on their own")
  void groupsAreReplacedAlone() {
    final DireWolfPackSettings packs = new DireWolfPackSettings(false, false);
    final DireWolfStaminaSettings stamina = new DireWolfStaminaSettings(false, 200, 50);
    assertEquals(packs, DireWolfSettings.DEFAULTS.withPacks(packs).packs());
    assertEquals(stamina, DireWolfSettings.DEFAULTS.withStamina(stamina).stamina());
    assertEquals(
        DireWolfSettings.DEFAULTS.stamina(), DireWolfSettings.DEFAULTS.withPacks(packs).stamina());
  }

  @Test
  @DisplayName("the settings survive a JSON round trip")
  void jsonRoundTrips() {
    final DireWolfSettings settings =
        new DireWolfSettings(
            150,
            9,
            6,
            12,
            new DireWolfPackSettings(false, true),
            new DireWolfStaminaSettings(false, 300, 40));
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
