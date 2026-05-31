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

class DachshundSoundDefinitionTest {

  private static final Path SOUNDS_JSON =
      Path.of("src/main/resources/assets/dogs-unleashed/sounds.json");
  private static final Path DACHSHUND_SOUND_DIR =
      Path.of("src/main/resources/assets/dogs-unleashed/sounds/entity/dachshund");

  @Test
  @DisplayName("Dachshund bark event should reference all 14 bark files")
  void dachshundBarkEventReferencesAllFiles() throws IOException {
    String soundsJson = Files.readString(SOUNDS_JSON);

    List<Integer> barkIds = extractDachshundBarkIds(soundsJson);

    assertEquals(14, barkIds.size());
    for (int i = 1; i <= 14; i++) {
      assertTrue(barkIds.contains(i), "Expected bark" + i + " to be present in sounds.json");
    }
  }

  @Test
  @DisplayName("Dachshund bark files should exist as bark1.ogg through bark14.ogg")
  void dachshundBarkFilesExist() {
    for (int i = 1; i <= 14; i++) {
      assertTrue(
          Files.exists(DACHSHUND_SOUND_DIR.resolve("bark" + i + ".ogg")),
          "Expected bark" + i + ".ogg to exist");
    }
  }

  @Test
  @DisplayName("Dachshund bark event should not include weighted entries")
  void dachshundBarkEventUsesEqualChanceEntries() throws IOException {
    String soundsJson = Files.readString(SOUNDS_JSON);
    String dachshundBlock = extractDachshundEventBlock(soundsJson);

    assertFalse(dachshundBlock.contains("\"weight\""));
    assertFalse(dachshundBlock.contains("\"type\""));
  }

  private static List<Integer> extractDachshundBarkIds(String soundsJson) {
    String dachshundBlock = extractDachshundEventBlock(soundsJson);
    Matcher matcher = Pattern.compile("entity/dachshund/bark(\\d+)").matcher(dachshundBlock);
    List<Integer> barkIds = new ArrayList<>();
    while (matcher.find()) {
      barkIds.add(Integer.parseInt(matcher.group(1)));
    }
    return barkIds;
  }

  private static String extractDachshundEventBlock(String soundsJson) {
    Matcher matcher =
        Pattern.compile("\\\"entity\\.dachshund\\.bark\\\"\\s*:\\s*\\{([\\s\\S]*?)\\n\\s*}\\s*,")
            .matcher(soundsJson);
    assertTrue(matcher.find(), "Could not find entity.dachshund.bark in sounds.json");
    return matcher.group(1);
  }
}
