package com.grahambartley.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HuskySoundDefinitionTest {

  private static final Path SOUNDS_JSON =
      Path.of("src/main/resources/assets/dogs-unleashed/sounds.json");
  private static final Path HUSKY_SOUND_DIR =
      Path.of("src/main/resources/assets/dogs-unleashed/sounds/entity/husky");

  @Test
  @DisplayName("Husky bark event should reference howl1 only")
  void huskyBarkEventReferencesHowlOneOnly() throws IOException {
    String soundsJson = Files.readString(SOUNDS_JSON);

    List<Integer> howlIds = extractHuskyHowlIdsFromBarkEvent(soundsJson);

    assertEquals(1, howlIds.size());
    assertTrue(howlIds.contains(1), "Expected howl1 to be present in husky bark event");
  }

  @Test
  @DisplayName("Husky bark event backing file should exist")
  void huskyBarkEventBackingFileExists() {
    assertTrue(
        Files.exists(HUSKY_SOUND_DIR.resolve("howl1.ogg")),
        "Expected howl1.ogg to exist for husky bark event");
  }

  @Test
  @DisplayName("Husky bark event should not include weighted entries")
  void huskyBarkEventUsesEqualChanceEntries() throws IOException {
    String soundsJson = Files.readString(SOUNDS_JSON);
    String huskyBlock = extractHuskyBarkEventBlock(soundsJson);

    assertFalse(huskyBlock.contains("\"weight\""));
    assertFalse(huskyBlock.contains("\"type\""));
  }

  private static List<Integer> extractHuskyHowlIdsFromBarkEvent(String soundsJson) {
    String huskyBlock = extractHuskyBarkEventBlock(soundsJson);
    Matcher matcher = Pattern.compile("entity/husky/howl(\\d+)").matcher(huskyBlock);
    List<Integer> howlIds = new ArrayList<>();
    while (matcher.find()) {
      howlIds.add(Integer.parseInt(matcher.group(1)));
    }
    return howlIds;
  }

  private static String extractHuskyBarkEventBlock(String soundsJson) {
    Matcher matcher =
        Pattern.compile("\\\"entity\\.husky\\.bark\\\"\\s*:\\s*\\{([\\s\\S]*?)\\n\\s*}\\s*,")
            .matcher(soundsJson);
    assertTrue(matcher.find(), "Could not find entity.husky.bark in sounds.json");
    return matcher.group(1);
  }
}
