package com.grahambartley.dogsunleashed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SleepSettingsTest {

  @Test
  @DisplayName("auto sleep is on with a 32 block range by default")
  void defaults() {
    assertEquals(new SleepSettings(true, 32), SleepSettings.DEFAULTS);
  }

  @Test
  @DisplayName("each with method changes only its own setting")
  void withMethodsChangeOneSetting() {
    assertEquals(new SleepSettings(false, 32), SleepSettings.DEFAULTS.withAutoSleepEnabled(false));
    assertEquals(new SleepSettings(true, 64), SleepSettings.DEFAULTS.withAutoSleepRangeBlocks(64));
  }

  @Test
  @DisplayName("the settings survive a JSON round trip, and a file value out of range is clamped")
  void jsonRoundTripsAndClamps() {
    final SleepSettings settings = new SleepSettings(false, 100);
    final JsonObject root = new JsonObject();
    settings.writeJson(root);
    assertEquals(settings, SleepSettings.fromJson(new ConfigJsonFields(root)));
    root.addProperty("autoSleepRangeBlocks", 9000);
    assertEquals(
        SleepSettings.RANGE_MAX,
        SleepSettings.fromJson(new ConfigJsonFields(root)).autoSleepRangeBlocks());
  }

  @Test
  @DisplayName("the smallest range is below the largest")
  void autoSleepRangeMinIsBelowMax() {
    assertTrue(SleepSettings.RANGE_MIN < SleepSettings.RANGE_MAX);
  }

  @Test
  @DisplayName("the smallest range is at least one block")
  void autoSleepRangeMinIsAtLeastOne() {
    assertTrue(SleepSettings.RANGE_MIN >= 1);
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
  @DisplayName("the range is clamped to its bounds")
  void constructorClampsAutoSleepRange(final int input, final int expected) {
    final SleepSettings config = new SleepSettings(true, input);
    assertEquals(expected, config.autoSleepRangeBlocks());
  }
}
