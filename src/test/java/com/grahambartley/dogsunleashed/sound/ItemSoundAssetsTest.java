package com.grahambartley.dogsunleashed.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ItemSoundAssetsTest {

  private static final double MAX_WHISTLE_SECONDS = 0.5;
  private static final int SAMPLE_RATE = 44100;
  private static final JsonObject SOUNDS = SoundAssets.readJson("sounds.json");

  static Stream<Path> whistleSoundFiles() throws Exception {
    final List<Path> files;
    try (Stream<Path> walk = Files.list(SoundAssets.path("sounds/item/dog_whistle"))) {
      files = walk.filter(path -> path.toString().endsWith(".ogg")).sorted().toList();
    }
    return files.stream();
  }

  static Stream<String> itemSoundEvents() {
    return SOUNDS.keySet().stream().filter(event -> event.startsWith("item."));
  }

  private static List<JsonElement> entries(final String event) {
    final List<JsonElement> sounds =
        SOUNDS.getAsJsonObject(event).getAsJsonArray("sounds").asList();
    assertFalse(sounds.isEmpty(), event + " plays no sound");
    return sounds;
  }

  private static String clipName(final JsonElement entry) {
    return entry.isJsonPrimitive()
        ? entry.getAsString()
        : entry.getAsJsonObject().get("name").getAsString();
  }

  private static String onlyClip(final String event) {
    final List<JsonElement> sounds = entries(event);
    assertEquals(1, sounds.size(), event + " clip count");
    return clipName(sounds.get(0));
  }

  @ParameterizedTest(name = "{0} is mono 44.1 kHz")
  @MethodSource("whistleSoundFiles")
  @DisplayName("every whistle clip is mono so it fades with distance from the player")
  void whistleClipIsMono(final Path file) throws IOException {
    final OggVorbisInfo info = OggVorbisInfo.read(file);
    assertEquals(1, info.channels(), file.getFileName() + " channels");
    assertEquals(SAMPLE_RATE, info.sampleRate(), file.getFileName() + " sample rate");
  }

  @ParameterizedTest(name = "{0} is a short sharp blast")
  @MethodSource("whistleSoundFiles")
  @DisplayName("no whistle clip runs longer than half a second")
  void whistleClipIsShort(final Path file) throws IOException {
    final double seconds = OggVorbisInfo.read(file).durationSeconds();
    assertTrue(seconds <= MAX_WHISTLE_SECONDS, file.getFileName() + " runs " + seconds + "s");
  }

  @ParameterizedTest(name = "{0} plays files that exist, unshifted")
  @MethodSource("itemSoundEvents")
  @DisplayName("every item sound event plays clips that exist at their recorded pitch")
  void itemSoundEventPlaysExistingClipsUnshifted(final String event) {
    for (final JsonElement entry : entries(event)) {
      final String name = clipName(entry);
      assertTrue(name.startsWith(SoundAssets.NAMESPACE), name + " namespace");
      assertTrue(Files.isRegularFile(SoundAssets.clipPath(name)), name + " must exist");
      if (entry.isJsonObject() && entry.getAsJsonObject().has("pitch")) {
        assertEquals(1.0f, entry.getAsJsonObject().get("pitch").getAsFloat(), event + " pitch");
      }
    }
  }

  @Test
  @DisplayName("pointing with the whistle plays its own clip, not a pitched-up recall blow")
  void pointAndBlowPlayDifferentClips() {
    assertNotEquals(onlyClip("item.dog_whistle.blow"), onlyClip("item.dog_whistle.point"));
  }
}
