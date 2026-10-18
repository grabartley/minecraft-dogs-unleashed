package com.grahambartley.dogsunleashed.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameplaySettingsTest {

  @Test
  @DisplayName("graves, dropping the leash and name tags are all on by default")
  void defaults() {
    assertEquals(new GameplaySettings(true, true, true), GameplaySettings.DEFAULTS);
  }

  @Test
  @DisplayName("each with method changes only its own setting")
  void withMethodsChangeOneSetting() {
    final GameplaySettings base = GameplaySettings.DEFAULTS;
    assertEquals(new GameplaySettings(false, true, true), base.withGravesEnabled(false));
    assertEquals(new GameplaySettings(true, false, true), base.withDropLeashOnPlayMode(false));
    assertEquals(new GameplaySettings(true, true, false), base.withShowDogNames(false));
  }

  @Test
  @DisplayName("the settings survive a JSON round trip under their flat keys")
  void jsonRoundTrips() {
    final GameplaySettings settings = new GameplaySettings(false, true, false);
    final JsonObject root = new JsonObject();
    settings.writeJson(root);
    assertFalse(root.get("gravesEnabled").getAsBoolean());
    assertTrue(root.get("dropLeashOnPlayMode").getAsBoolean());
    assertFalse(root.get("showDogNames").getAsBoolean());
    assertEquals(settings, GameplaySettings.fromJson(new ConfigJsonFields(root)));
  }

  @Test
  @DisplayName("missing or invalid values fall back to the defaults")
  void invalidJsonFallsBack() {
    final JsonObject root =
        JsonParser.parseString("{\"gravesEnabled\": \"no\", \"showDogNames\": false}")
            .getAsJsonObject();
    assertEquals(
        new GameplaySettings(true, true, false),
        GameplaySettings.fromJson(new ConfigJsonFields(root)));
  }
}
