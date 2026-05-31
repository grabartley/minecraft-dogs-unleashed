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

class GoldenRetrieverSoundDefinitionTest {

  private static final Path SOUNDS_JSON =
      Path.of("src/main/resources/assets/dogs-unleashed/sounds.json");
  private static final Path GOLDEN_RETRIEVER_SOUND_DIR =
      Path.of("src/main/resources/assets/dogs-unleashed/sounds/entity/goldenretriever");

  @Test
  @DisplayName("Golden retriever bark event should reference all 5 bark files")
  void goldenRetrieverBarkEventReferencesAllFiles() throws IOException {
    String soundsJson = Files.readString(SOUNDS_JSON);

    List<Integer> barkIds = extractGoldenRetrieverBarkIds(soundsJson);

    assertEquals(5, barkIds.size());
    for (int i = 1; i <= 5; i++) {
      assertTrue(barkIds.contains(i), "Expected bark" + i + " to be present in sounds.json");
    }
  }

  @Test
  @DisplayName("Golden retriever bark files should exist as bark1.ogg through bark5.ogg")
  void goldenRetrieverBarkFilesExist() {
    for (int i = 1; i <= 5; i++) {
      assertTrue(
          Files.exists(GOLDEN_RETRIEVER_SOUND_DIR.resolve("bark" + i + ".ogg")),
          "Expected bark" + i + ".ogg to exist");
    }
  }

  @Test
  @DisplayName("Golden retriever bark event should not include weighted entries")
  void goldenRetrieverBarkEventUsesEqualChanceEntries() throws IOException {
    String soundsJson = Files.readString(SOUNDS_JSON);
    String goldenRetrieverBlock = extractGoldenRetrieverEventBlock(soundsJson);

    assertFalse(goldenRetrieverBlock.contains("\"weight\""));
    assertFalse(goldenRetrieverBlock.contains("\"type\""));
  }

  private static List<Integer> extractGoldenRetrieverBarkIds(String soundsJson) {
    String goldenRetrieverBlock = extractGoldenRetrieverEventBlock(soundsJson);
    Matcher matcher =
        Pattern.compile("entity/goldenretriever/bark(\\d+)").matcher(goldenRetrieverBlock);
    List<Integer> barkIds = new ArrayList<>();
    while (matcher.find()) {
      barkIds.add(Integer.parseInt(matcher.group(1)));
    }
    return barkIds;
  }

  private static String extractGoldenRetrieverEventBlock(String soundsJson) {
    Matcher matcher =
        Pattern.compile(
                "\\\"entity\\.goldenretriever\\.bark\\\"\\s*:\\s*\\{([\\s\\S]*?)\\n\\s*}\\s*,")
            .matcher(soundsJson);
    assertTrue(matcher.find(), "Could not find entity.goldenretriever.bark in sounds.json");
    return matcher.group(1);
  }
}
