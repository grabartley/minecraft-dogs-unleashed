package com.grahambartley.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BarkSoundAssetParityTest {

  private static final Path SOUNDS_JSON =
      Path.of("src/main/resources/assets/dogs-unleashed/sounds.json");

  private static final Set<String> FIRST_RELEASE_BREEDS =
      Set.of("beagle", "dachshund", "goldenretriever", "husky", "shibainu");

  @Test
  @DisplayName("All first-release bark sound definitions should map to existing bark assets")
  void barkSoundDefinitionsShouldMapToExistingAssets() throws IOException {
    String soundsJson = Files.readString(SOUNDS_JSON);
    Map<String, List<String>> barkEventsByBreed = extractBarkEventsByBreed(soundsJson);

    assertEquals(FIRST_RELEASE_BREEDS, barkEventsByBreed.keySet());

    List<String> missingAssets = new ArrayList<>();
    for (Map.Entry<String, List<String>> entry : barkEventsByBreed.entrySet()) {
      assertFalse(entry.getValue().isEmpty(), "Expected bark sounds for " + entry.getKey());

      for (String soundRef : entry.getValue()) {
        String relativePath = soundRef.replace("dogs-unleashed:", "");
        Path soundFile =
            Path.of("src/main/resources/assets/dogs-unleashed/sounds")
                .resolve(relativePath + ".ogg");
        if (!Files.exists(soundFile)) {
          missingAssets.add(soundFile.toString());
        }
      }
    }

    assertTrue(
        missingAssets.isEmpty(),
        "Missing bark assets referenced by sounds.json: " + String.join(", ", missingAssets));
  }

  private static Map<String, List<String>> extractBarkEventsByBreed(String soundsJson) {
    Map<String, List<String>> result = new LinkedHashMap<>();
    Matcher eventMatcher =
        Pattern.compile("\\\"entity\\.([a-z]+)\\.bark\\\"\\s*:\\s*\\{([\\s\\S]*?)\\n\\s*}\\s*,?")
            .matcher(soundsJson);

    while (eventMatcher.find()) {
      String breed = eventMatcher.group(1);
      String eventBody = eventMatcher.group(2);
      Matcher soundMatcher =
          Pattern.compile("\\\"(dogs-unleashed:entity/" + breed + "/bark\\d+)\\\"")
              .matcher(eventBody);

      List<String> soundRefs = new ArrayList<>();
      Set<String> seen = new HashSet<>();
      while (soundMatcher.find()) {
        String soundRef = soundMatcher.group(1);
        if (seen.add(soundRef)) {
          soundRefs.add(soundRef);
        }
      }

      result.put(breed, soundRefs);
    }

    return result;
  }
}
