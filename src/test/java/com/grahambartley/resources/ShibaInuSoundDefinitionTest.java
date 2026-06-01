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

class ShibaInuSoundDefinitionTest {

  private static final Path SOUNDS_JSON =
      Path.of("src/main/resources/assets/dogs-unleashed/sounds.json");
  private static final Path SHIBA_INU_SOUND_DIR =
      Path.of("src/main/resources/assets/dogs-unleashed/sounds/entity/shibainu");

  @Test
  @DisplayName("Shiba inu bark event should reference only bark1")
  void shibaInuBarkEventReferencesOnlyOneFile() throws IOException {
    String soundsJson = Files.readString(SOUNDS_JSON);

    List<Integer> barkIds = extractShibaInuBarkIds(soundsJson);

    assertEquals(1, barkIds.size());
    assertTrue(barkIds.contains(1), "Expected bark1 to be present in sounds.json");
  }

  @Test
  @DisplayName("Shiba inu bark files should only include bark1.ogg")
  void shibaInuBarkFilesExist() {
    assertTrue(
        Files.exists(SHIBA_INU_SOUND_DIR.resolve("bark1.ogg")), "Expected bark1.ogg to exist");
    assertFalse(Files.exists(SHIBA_INU_SOUND_DIR.resolve("bark2.ogg")), "Did not expect bark2.ogg");
    assertFalse(Files.exists(SHIBA_INU_SOUND_DIR.resolve("bark3.ogg")), "Did not expect bark3.ogg");
  }

  @Test
  @DisplayName("Shiba inu bark event should not include weighted entries")
  void shibaInuBarkEventUsesEqualChanceEntries() throws IOException {
    String soundsJson = Files.readString(SOUNDS_JSON);
    String shibaInuBlock = extractShibaInuEventBlock(soundsJson);

    assertFalse(shibaInuBlock.contains("\"weight\""));
    assertFalse(shibaInuBlock.contains("\"type\""));
  }

  private static List<Integer> extractShibaInuBarkIds(String soundsJson) {
    String shibaInuBlock = extractShibaInuEventBlock(soundsJson);
    Matcher matcher = Pattern.compile("entity/shibainu/bark(\\d+)").matcher(shibaInuBlock);
    List<Integer> barkIds = new ArrayList<>();
    while (matcher.find()) {
      barkIds.add(Integer.parseInt(matcher.group(1)));
    }
    return barkIds;
  }

  private static String extractShibaInuEventBlock(String soundsJson) {
    Matcher matcher =
        Pattern.compile("\\\"entity\\.shibainu\\.bark\\\"\\s*:\\s*\\{([\\s\\S]*?)\\n\\s*}\\s*,")
            .matcher(soundsJson);
    assertTrue(matcher.find(), "Could not find entity.shibainu.bark in sounds.json");
    return matcher.group(1);
  }
}
