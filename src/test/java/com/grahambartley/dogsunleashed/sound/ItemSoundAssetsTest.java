package com.grahambartley.dogsunleashed.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ItemSoundAssetsTest {

  private static final String ASSETS = "/assets/dogs-unleashed";
  private static final String NAMESPACE = "dogs-unleashed:";
  private static final double MAX_WHISTLE_SECONDS = 0.5;
  private static final int SAMPLE_RATE = 44100;
  private static final JsonObject SOUNDS = readJson("sounds.json");

  private static Path assetPath(final String relative) throws URISyntaxException {
    return Path.of(ItemSoundAssetsTest.class.getResource(ASSETS + "/" + relative).toURI());
  }

  private static JsonObject readJson(final String relative) {
    try {
      return JsonParser.parseString(Files.readString(assetPath(relative), StandardCharsets.UTF_8))
          .getAsJsonObject();
    } catch (IOException | URISyntaxException ex) {
      throw new IllegalStateException("cannot read " + relative, ex);
    }
  }

  static Stream<Path> whistleSoundFiles() throws Exception {
    final List<Path> files;
    try (Stream<Path> walk = Files.list(assetPath("sounds/item/dog_whistle"))) {
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
  void itemSoundEventPlaysExistingClipsUnshifted(final String event) throws URISyntaxException {
    for (final JsonElement entry : entries(event)) {
      final String name = clipName(entry);
      assertTrue(name.startsWith(NAMESPACE), name + " namespace");
      final String relative = "sounds/" + name.substring(NAMESPACE.length()) + ".ogg";
      assertTrue(Files.isRegularFile(assetPath(relative)), relative + " must exist");
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
