package com.grahambartley.dogsunleashed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DireWolfPackSettingsTest {

  @Test
  @DisplayName("wild packs hunt and defend each other by default")
  void defaultsAreOn() {
    assertTrue(DireWolfPackSettings.DEFAULTS.hunt());
    assertTrue(DireWolfPackSettings.DEFAULTS.defend());
  }

  @ParameterizedTest
  @CsvSource({"true,true", "true,false", "false,true", "false,false"})
  @DisplayName("the settings survive a JSON round trip")
  void jsonRoundTrips(final boolean hunt, final boolean defend) {
    final DireWolfPackSettings settings = new DireWolfPackSettings(hunt, defend);
    assertEquals(settings, DireWolfPackSettings.fromJson(settings.toJson()));
  }

  @Test
  @DisplayName("each with method changes only its own setting")
  void withMethodsChangeOneSetting() {
    assertEquals(
        new DireWolfPackSettings(false, true), DireWolfPackSettings.DEFAULTS.withHunt(false));
    assertEquals(
        new DireWolfPackSettings(true, false), DireWolfPackSettings.DEFAULTS.withDefend(false));
  }

  @Test
  @DisplayName("missing or invalid values fall back to the defaults")
  void invalidJsonFallsBack() {
    final DireWolfPackSettings settings =
        DireWolfPackSettings.fromJson(
            JsonParser.parseString("{\"hunt\": \"maybe\"}").getAsJsonObject());
    assertEquals(DireWolfPackSettings.DEFAULTS, settings);
  }
}
