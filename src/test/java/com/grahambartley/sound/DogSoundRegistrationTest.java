package com.grahambartley.sound;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.DogsUnleashed;
import com.grahambartley.MinecraftBootstrapExtension;
import com.grahambartley.ModSounds;
import com.grahambartley.entity.UnleashedDogBreed;
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
}
