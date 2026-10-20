package com.grahambartley.dogsunleashed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GameplaySettingsTest {

  @Test
  @DisplayName("graves, dropping the leash, name tags and muddy dogs are all on by default")
  void defaults() {
    assertEquals(new GameplaySettings(true, true, true, true), GameplaySettings.DEFAULTS);
  }

  static Stream<Arguments> withMethods() {
    return Stream.of(
        Arguments.of(
            "graves",
            (UnaryOperator<GameplaySettings>) s -> s.withGravesEnabled(false),
            new GameplaySettings(false, true, true, true)),
        Arguments.of(
            "drop leash",
            (UnaryOperator<GameplaySettings>) s -> s.withDropLeashOnPlayMode(false),
            new GameplaySettings(true, false, true, true)),
        Arguments.of(
            "show names",
            (UnaryOperator<GameplaySettings>) s -> s.withShowDogNames(false),
            new GameplaySettings(true, true, false, true)),
        Arguments.of(
            "muddy dogs",
            (UnaryOperator<GameplaySettings>) s -> s.withMuddyDogs(false),
            new GameplaySettings(true, true, true, false)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("withMethods")
  @DisplayName("each with method changes only its own setting")
  void withMethodsChangeOneSetting(
      final String name,
      final UnaryOperator<GameplaySettings> change,
      final GameplaySettings expected) {
    assertEquals(expected, change.apply(GameplaySettings.DEFAULTS));
  }

  @Test
  @DisplayName("the settings survive a JSON round trip under their flat keys")
  void jsonRoundTrips() {
    final GameplaySettings settings = new GameplaySettings(false, true, false, false);
    final JsonObject root = new JsonObject();
    settings.writeJson(root);
    assertFalse(root.get("gravesEnabled").getAsBoolean());
    assertTrue(root.get("dropLeashOnPlayMode").getAsBoolean());
    assertFalse(root.get("showDogNames").getAsBoolean());
    assertFalse(root.get("muddyDogs").getAsBoolean());
    assertEquals(settings, GameplaySettings.fromJson(new ConfigJsonFields(root)));
  }

  @Test
  @DisplayName("missing or invalid values fall back to the defaults")
  void invalidJsonFallsBack() {
    final JsonObject root =
        JsonParser.parseString(
                "{\"gravesEnabled\": \"no\", \"showDogNames\": false, \"muddyDogs\": 3}")
            .getAsJsonObject();
    assertEquals(
        new GameplaySettings(true, true, false, true),
        GameplaySettings.fromJson(new ConfigJsonFields(root)));
  }
}
