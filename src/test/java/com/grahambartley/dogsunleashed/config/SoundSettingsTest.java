package com.grahambartley.dogsunleashed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SoundSettingsTest {

  @Test
  @DisplayName("barks play at full volume and howls at 1.5 by default")
  void defaults() {
    assertEquals(new SoundSettings(1.0f, 1.5f), SoundSettings.DEFAULTS);
  }

  static Stream<Arguments> volumeCases() {
    return Stream.of(
        Arguments.of(-1.0f, SoundSettings.VOLUME_MIN),
        Arguments.of(0.5f, 0.5f),
        Arguments.of(5.0f, SoundSettings.VOLUME_MAX));
  }

  @ParameterizedTest(name = "{0} clamps to {1}")
  @MethodSource("volumeCases")
  @DisplayName("both volumes stay between their bounds")
  void volumesAreClamped(final float input, final float expected) {
    final SoundSettings settings = new SoundSettings(input, input);
    assertEquals(expected, settings.barkVolume());
    assertEquals(expected, settings.howlVolume());
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
}
