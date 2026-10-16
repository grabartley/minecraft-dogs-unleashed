package com.grahambartley.dogsunleashed.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class EntitySoundAssetsTest {

  private static final String ASSETS = "/assets/dogs-unleashed";
  private static final double MAX_BARK_SECONDS = 2.5;
  private static final JsonObject SOUNDS = readJson("sounds.json");
  private static final JsonObject ENGLISH = readJson("lang/en_us.json");

  private static Path assetPath(final String relative) throws URISyntaxException {
    return Path.of(EntitySoundAssetsTest.class.getResource(ASSETS + "/" + relative).toURI());
  }

  private static JsonObject readJson(final String relative) {
    try {
      return JsonParser.parseString(Files.readString(assetPath(relative), StandardCharsets.UTF_8))
          .getAsJsonObject();
    } catch (IOException | URISyntaxException ex) {
      throw new IllegalStateException("cannot read " + relative, ex);
    }
  }

  static Stream<Path> entitySoundFiles() throws Exception {
    final List<Path> files;
    try (Stream<Path> walk = Files.walk(assetPath("sounds/entity"))) {
      files = walk.filter(path -> path.toString().endsWith(".ogg")).sorted().toList();
    }
    return files.stream();
  }

  static Stream<Path> barkSoundFiles() throws Exception {
    return entitySoundFiles().filter(path -> path.getFileName().toString().startsWith("bark"));
  }

  static Stream<String> entitySoundEvents() {
    return SOUNDS.keySet().stream().filter(event -> event.startsWith("entity."));
  }

  @ParameterizedTest(name = "{0} is mono")
  @MethodSource("entitySoundFiles")
  @DisplayName(
      "every entity sound is mono so it plays from the dog's position and fades with distance")
  void entitySoundIsMono(final Path file) throws IOException {
    assertEquals(1, OggVorbisInfo.read(file).channels(), file.getFileName() + " channels");
  }

  @ParameterizedTest(name = "{0} is a single short bark")
  @MethodSource("barkSoundFiles")
  @DisplayName("no bark clip runs longer than a single bark")
  void barkClipIsShort(final Path file) throws IOException {
    final double seconds = OggVorbisInfo.read(file).durationSeconds();
    assertTrue(
        seconds <= MAX_BARK_SECONDS,
        file.getParent().getFileName() + "/" + file.getFileName() + " runs " + seconds + "s");
  }

  @ParameterizedTest(name = "{0} has a subtitle")
  @MethodSource("entitySoundEvents")
  @DisplayName("every bark and howl event declares a subtitle that has an English translation")
  void entitySoundEventHasTranslatedSubtitle(final String event) {
    final JsonObject definition = SOUNDS.getAsJsonObject(event);
    assertTrue(definition.has("subtitle"), event + " must declare a subtitle");
    final String subtitleKey = definition.get("subtitle").getAsString();
    assertTrue(ENGLISH.has(subtitleKey), subtitleKey + " must be in en_us.json");
  }
}
