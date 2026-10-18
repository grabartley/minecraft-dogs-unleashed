package com.grahambartley.dogsunleashed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FurSettingsTest {

  @Test
  @DisplayName("fur grows at the normal rate with the normal bad hair day chance by default")
  void defaults() {
    assertEquals(new FurSettings(true, 100, 100), FurSettings.DEFAULTS);
  }

  static Stream<Arguments> percentCases() {
    return Stream.of(
        Arguments.of(-10, FurSettings.PERCENT_MIN),
        Arguments.of(250, 250),
        Arguments.of(9999, FurSettings.PERCENT_MAX));
  }

  @ParameterizedTest(name = "{0}% clamps to {1}%")
  @MethodSource("percentCases")
  @DisplayName("both multipliers stay between their bounds")
  void multipliersAreClamped(final int input, final int expected) {
    final FurSettings settings = new FurSettings(true, input, input);
    assertEquals(expected, settings.furGrowthRateMultiplierPercent());
    assertEquals(expected, settings.badHairDayChanceMultiplierPercent());
  }

  @Test
  @DisplayName("each with method changes only its own setting")
  void withMethodsChangeOneSetting() {
    final FurSettings base = FurSettings.DEFAULTS;
    assertEquals(new FurSettings(false, 100, 100), base.withFurGrowthEnabled(false));
    assertEquals(new FurSettings(true, 300, 100), base.withFurGrowthRateMultiplierPercent(300));
    assertEquals(new FurSettings(true, 100, 0), base.withBadHairDayChanceMultiplierPercent(0));
  }

  @Test
  @DisplayName("the settings survive a JSON round trip under their flat keys")
  void jsonRoundTrips() {
    final FurSettings settings = new FurSettings(false, 300, 40);
    final JsonObject root = new JsonObject();
    settings.writeJson(root);
    assertEquals(300, root.get("furGrowthRateMultiplierPercent").getAsInt());
    assertEquals(settings, FurSettings.fromJson(new ConfigJsonFields(root)));
  }
}
