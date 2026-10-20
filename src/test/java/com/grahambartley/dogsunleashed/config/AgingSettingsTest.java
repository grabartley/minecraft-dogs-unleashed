package com.grahambartley.dogsunleashed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AgingSettingsTest {

  @Test
  @DisplayName("aging is off by default and dogs turn senior after 100 days")
  void defaults() {
    assertEquals(new AgingSettings(false, 100), AgingSettings.DEFAULTS);
  }

  @Test
  @DisplayName("each with method changes only its own setting")
  void withMethodsChangeOneSetting() {
    final AgingSettings base = AgingSettings.DEFAULTS;
    assertEquals(new AgingSettings(true, 100), base.withDogAgingEnabled(true));
    assertEquals(new AgingSettings(false, 7), base.withSeniorAfterDays(7));
  }

  @Test
  @DisplayName("the settings survive a JSON round trip under their flat keys")
  void jsonRoundTrips() {
    final AgingSettings settings = new AgingSettings(true, 42);
    final JsonObject root = new JsonObject();
    settings.writeJson(root);
    assertEquals(42, root.get("seniorAfterDays").getAsInt());
    assertEquals(true, root.get("dogAgingEnabled").getAsBoolean());
    assertEquals(settings, AgingSettings.fromJson(new ConfigJsonFields(root)));
  }

  @Test
  @DisplayName("missing keys fall back to the defaults")
  void missingKeysUseDefaults() {
    assertEquals(
        AgingSettings.DEFAULTS, AgingSettings.fromJson(new ConfigJsonFields(new JsonObject())));
  }

  static Stream<Arguments> dayClampCases() {
    return Stream.of(
        Arguments.of(-5, 1),
        Arguments.of(0, 1),
        Arguments.of(1, 1),
        Arguments.of(100, 100),
        Arguments.of(1000, 1000),
        Arguments.of(1001, 1000));
  }

  @ParameterizedTest(name = "{0} days clamps to {1}")
  @MethodSource("dayClampCases")
  @DisplayName("constructor clamps the senior age to [1, 1000] days")
  void constructorClampsDays(final int input, final int expected) {
    assertEquals(expected, new AgingSettings(true, input).seniorAfterDays());
  }
}
