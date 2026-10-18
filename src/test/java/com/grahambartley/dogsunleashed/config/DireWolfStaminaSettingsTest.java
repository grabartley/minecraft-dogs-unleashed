package com.grahambartley.dogsunleashed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DireWolfStaminaSettingsTest {

  @Test
  @DisplayName("stamina drains at full capacity and recovery by default")
  void defaults() {
    assertTrue(DireWolfStaminaSettings.DEFAULTS.drains());
    assertEquals(100, DireWolfStaminaSettings.DEFAULTS.capacityPercent());
    assertEquals(100, DireWolfStaminaSettings.DEFAULTS.recoveryPercent());
  }

  static Stream<Arguments> clampCases() {
    return Stream.of(
        Arguments.of(new DireWolfStaminaSettings(true, 0, 9999), 25, 400),
        Arguments.of(new DireWolfStaminaSettings(true, -50, 10), 25, 25),
        Arguments.of(new DireWolfStaminaSettings(false, 150, 75), 150, 75));
  }

  @ParameterizedTest
  @MethodSource("clampCases")
  @DisplayName("capacity and recovery stay between 25% and 400%")
  void percentagesAreClamped(
      final DireWolfStaminaSettings settings, final int capacity, final int recovery) {
    assertEquals(capacity, settings.capacityPercent());
    assertEquals(recovery, settings.recoveryPercent());
  }

  @Test
  @DisplayName("each with method changes only its own setting")
  void withMethodsChangeOneSetting() {
    final DireWolfStaminaSettings base = DireWolfStaminaSettings.DEFAULTS;
    assertEquals(new DireWolfStaminaSettings(false, 100, 100), base.withDrains(false));
    assertEquals(new DireWolfStaminaSettings(true, 250, 100), base.withCapacityPercent(250));
    assertEquals(new DireWolfStaminaSettings(true, 100, 50), base.withRecoveryPercent(50));
  }

  @Test
  @DisplayName("the settings survive a JSON round trip")
  void jsonRoundTrips() {
    final DireWolfStaminaSettings settings = new DireWolfStaminaSettings(false, 175, 60);
    assertEquals(settings, DireWolfStaminaSettings.fromJson(settings.toJson()));
  }

  @Test
  @DisplayName("missing or invalid values fall back to the defaults")
  void invalidJsonFallsBack() {
    final DireWolfStaminaSettings settings =
        DireWolfStaminaSettings.fromJson(
            JsonParser.parseString("{\"drains\": 3, \"capacityPercent\": \"lots\"}")
                .getAsJsonObject());
    assertEquals(DireWolfStaminaSettings.DEFAULTS, settings);
  }
}
