package com.grahambartley.dogsunleashed.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SoundAssetsTest {

  @ParameterizedTest(name = "{0} reads as a JSON object")
  @ValueSource(strings = {"sounds.json", "lang/en_us.json"})
  @DisplayName("shipped JSON assets read as objects")
  void readJsonReadsShippedAssets(final String relative) {
    assertFalse(SoundAssets.readJson(relative).isEmpty(), relative);
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "dogs-unleashed:item/dog_whistle/blow1, sounds/item/dog_whistle/blow1.ogg",
    "dogs-unleashed:entity/husky/howl1, sounds/entity/husky/howl1.ogg"
  })
  @DisplayName("a sound id maps to its ogg file under the mod's sounds folder")
  void clipPathMapsIdToFile(final String soundId, final String relative) {
    assertEquals(SoundAssets.path(relative), SoundAssets.clipPath(soundId));
    assertTrue(Files.isRegularFile(SoundAssets.clipPath(soundId)), soundId);
  }

  @ParameterizedTest(name = "{0} resolves to a missing path")
  @ValueSource(strings = {"sounds/item/dog_whistle/missing.ogg", "no_such_folder/clip.ogg"})
  @DisplayName("a missing asset resolves to a path that does not exist instead of throwing")
  void missingAssetResolvesWithoutThrowing(final String relative) {
    assertFalse(Files.exists(SoundAssets.path(relative)), relative);
  }

  @ParameterizedTest(name = "{0} cannot be read")
  @ValueSource(strings = {"missing.json", "lang/xx_xx.json"})
  @DisplayName("reading a missing JSON asset fails with its name")
  void readJsonFailsForMissingAsset(final String relative) {
    final IllegalStateException ex =
        assertThrows(IllegalStateException.class, () -> SoundAssets.readJson(relative));
    assertTrue(ex.getMessage().contains(relative), ex.getMessage());
  }
}
