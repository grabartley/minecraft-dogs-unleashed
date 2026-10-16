package com.grahambartley.dogsunleashed.sound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModSounds;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogSoundRegistrationTest {

  static Stream<Arguments> barkingBreeds() {
    return Stream.of(
        Arguments.of(UnleashedDogBreed.DACHSHUND, ModSounds.DACHSHUND_BARK),
        Arguments.of(UnleashedDogBreed.BEAGLE, ModSounds.BEAGLE_BARK),
        Arguments.of(UnleashedDogBreed.GOLDEN_RETRIEVER, ModSounds.GOLDEN_RETRIEVER_BARK),
        Arguments.of(UnleashedDogBreed.SHIBA_INU, ModSounds.SHIBA_INU_BARK));
  }

  @ParameterizedTest(name = "{0} bark sound is in Registries.SOUND_EVENT")
  @MethodSource("barkingBreeds")
  @DisplayName("every barking breed's bark sound is registered with the canonical vanilla registry")
  void barkSoundIsRegistered(final UnleashedDogBreed breed, final SoundEvent barkSound) {
    assertNotNull(barkSound, breed.serializedId() + " must define a bark sound");
    assertTrue(
        Registries.SOUND_EVENT.containsId(Registries.SOUND_EVENT.getId(barkSound)),
        breed.serializedId() + " bark sound should be in Registries.SOUND_EVENT");
  }

  @Test
  @DisplayName("husky has no bark sound registered because it uses howling instead")
  void huskyHasNoBarkSoundRegistered() {
    assertFalse(
        Registries.SOUND_EVENT.containsId(Identifier.of(DogsUnleashed.MOD_ID, "entity.husky.bark")),
        "Husky must not register a bark sound");
  }

  @Test
  @DisplayName("husky howl sound is registered with the canonical vanilla registry")
  void huskyHowlSoundIsRegistered() {
    assertTrue(
        Registries.SOUND_EVENT.containsId(Registries.SOUND_EVENT.getId(ModSounds.HUSKY_HOWL)),
        "Husky howl sound should be in Registries.SOUND_EVENT");
  }

  private static final String ASSETS = "/assets/dogs-unleashed";
  private static final double MAX_BARK_SECONDS = 2.5;

  private static Path assetPath(final String relative) throws URISyntaxException {
    return Path.of(DogSoundRegistrationTest.class.getResource(ASSETS + "/" + relative).toURI());
  }

  private static JsonObject readJson(final String relative) throws Exception {
    return JsonParser.parseString(Files.readString(assetPath(relative), StandardCharsets.UTF_8))
        .getAsJsonObject();
  }

  static Stream<Path> entitySoundFiles() throws Exception {
    final List<Path> files;
    try (Stream<Path> walk = Files.walk(assetPath("sounds/entity"))) {
      files = walk.filter(path -> path.toString().endsWith(".ogg")).sorted().toList();
    }
    return files.stream();
  }

  static Stream<String> entitySoundEvents() throws Exception {
    return readJson("sounds.json").keySet().stream().filter(event -> event.startsWith("entity."));
  }

  @ParameterizedTest(name = "{0} is mono")
  @MethodSource("entitySoundFiles")
  @DisplayName(
      "every entity sound is mono so it plays from the dog's position and fades with distance")
  void entitySoundIsMono(final Path file) throws IOException {
    assertEquals(1, OggVorbisInfo.read(file).channels(), file.getFileName() + " channels");
  }

  @ParameterizedTest(name = "{0} is a single short bark")
  @MethodSource("entitySoundFiles")
  @DisplayName("no bark clip runs longer than a single bark")
  void barkClipIsShort(final Path file) throws IOException {
    if (!file.getFileName().toString().startsWith("bark")) {
      return;
    }
    final double seconds = OggVorbisInfo.read(file).durationSeconds();
    assertTrue(
        seconds <= MAX_BARK_SECONDS,
        file.getParent().getFileName() + "/" + file.getFileName() + " runs " + seconds + "s");
  }

  @ParameterizedTest(name = "{0} has a subtitle")
  @MethodSource("entitySoundEvents")
  @DisplayName("every bark and howl event declares a subtitle that has an English translation")
  void entitySoundEventHasTranslatedSubtitle(final String event) throws Exception {
    final JsonObject definition = readJson("sounds.json").getAsJsonObject(event);
    assertTrue(definition.has("subtitle"), event + " must declare a subtitle");
    final String subtitleKey = definition.get("subtitle").getAsString();
    final Map<String, ?> english = readJson("lang/en_us.json").asMap();
    assertTrue(english.containsKey(subtitleKey), subtitleKey + " must be in en_us.json");
  }
}
