package com.grahambartley;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModSoundsTest {

  private static final Path SOUNDS_JSON =
      Path.of("src/main/resources/assets/dogs-unleashed/sounds.json");
  private static final Path HUSKY_SOUND_DIR =
      Path.of("src/main/resources/assets/dogs-unleashed/sounds/entity/husky");

  @Test
  @DisplayName("Husky bark event should be absent from sounds.json")
  void huskyBarkEventIsAbsent() throws IOException {
    JsonObject root = readSoundsJson();
    assertFalse(root.has("entity.husky.bark"));
  }

  @Test
  @DisplayName("Husky howl event should reference howl1 only")
  void huskyHowlEventReferencesHowlOneOnly() throws IOException {
    List<String> huskyHowlSoundIds = extractHuskyHowlSoundIds(readSoundsJson());
    assertEquals(List.of("dogs-unleashed:entity/husky/howl1"), huskyHowlSoundIds);
  }

  @Test
  @DisplayName("Husky howl event backing file should exist")
  void huskyHowlEventBackingFileExists() {
    assertTrue(
        Files.exists(HUSKY_SOUND_DIR.resolve("howl1.ogg")),
        "Expected howl1.ogg to exist for husky howl event");
  }

  @Test
  @DisplayName("Husky howl event should not include weighted entries")
  void huskyHowlEventUsesEqualChanceEntries() throws IOException {
    JsonArray sounds =
        readSoundsJson().getAsJsonObject("entity.husky.howl").getAsJsonArray("sounds");
    for (int i = 0; i < sounds.size(); i++) {
      assertTrue(sounds.get(i).isJsonPrimitive(), "Expected plain string sound entries only");
    }
  }

  private static JsonObject readSoundsJson() throws IOException {
    return JsonParser.parseString(Files.readString(SOUNDS_JSON)).getAsJsonObject();
  }

  private static List<String> extractHuskyHowlSoundIds(JsonObject root) {
    JsonArray sounds = root.getAsJsonObject("entity.husky.howl").getAsJsonArray("sounds");
    java.util.ArrayList<String> howlIds = new java.util.ArrayList<>();
    for (int i = 0; i < sounds.size(); i++) {
      howlIds.add(sounds.get(i).getAsString());
    }
    return howlIds;
  }
}
