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
import org.junit.jupiter.params.provider.ValueSource;

class SoundSettingsTest {

  @Test
  @DisplayName("barks play at full volume and howls at 1.5 by default")
  void defaults() {
    assertEquals(new SoundSettings(1.0f, 1.5f), SoundSettings.DEFAULTS);
  }

  @Test
  @DisplayName("each with method changes only its own setting")
  void withMethodsChangeOneSetting() {
    assertEquals(new SoundSettings(0.25f, 1.5f), SoundSettings.DEFAULTS.withBarkVolume(0.25f));
    assertEquals(new SoundSettings(1.0f, 0.75f), SoundSettings.DEFAULTS.withHowlVolume(0.75f));
  }

  @Test
  @DisplayName("the settings survive a JSON round trip under their flat keys")
  void jsonRoundTrips() {
    final SoundSettings settings = new SoundSettings(0.25f, 1.75f);
    final JsonObject root = new JsonObject();
    settings.writeJson(root);
    assertEquals(0.25f, root.get("barkVolume").getAsFloat());
    assertEquals(settings, SoundSettings.fromJson(new ConfigJsonFields(root)));
  }

  @Test
  @DisplayName("the lowest volume is not negative and is below the highest")
  void volumeMinIsNonNegativeAndBelowMax() {
    assertTrue(SoundSettings.VOLUME_MIN >= 0.0f);
    assertTrue(SoundSettings.VOLUME_MIN < SoundSettings.VOLUME_MAX);
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
  @DisplayName("both volumes are clamped to their bounds")
  void constructorClampsVolumes(final float input, final float expected) {
    final SoundSettings config = new SoundSettings(input, input);
    assertEquals(expected, config.barkVolume());
    assertEquals(expected, config.howlVolume());
  }

  @ParameterizedTest(name = "{0} falls back to the defaults")
  @ValueSource(floats = {Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY})
  @DisplayName("volumes that are not finite are refused and replaced by the defaults")
  void constructorRefusesNonFiniteVolumes(final float input) {
    assertEquals(SoundSettings.DEFAULTS, new SoundSettings(input, input));
  }
}
