package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.samplePet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.config.AgingSettings;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.pet.PetAge;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class PetSyncDataTest {

  @Test
  @DisplayName("codec round-trips a cross-breed pet with genome fields")
  void codecRoundTripsCrossBreedGenomeFields() {
    final PetSyncData pet =
        samplePet(
            UnleashedDogBreed.CROSS_BREED,
            "Pixel",
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.5f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f)));
    final RegistryByteBuf buf = newBuf();
    PetSyncData.CODEC.encode(buf, pet);
    assertEquals(pet, PetSyncData.CODEC.decode(buf));
  }

  @ParameterizedTest(name = "codec round-trips a {0} pet")
  @EnumSource(PetLifeState.class)
  @DisplayName("codec round-trips every lifecycle state so the client can render undead pets")
  void codecRoundTripsEveryLifeState(final PetLifeState lifeState) {
    final PetSyncData pet =
        new PetSyncData(
            samplePet(UnleashedDogBreed.HUSKY, "Balto").petId(),
            UnleashedDogBreed.HUSKY,
            "Balto",
            9.0f,
            18.0f,
            1,
            2,
            3,
            "minecraft:overworld",
            lifeState,
            false,
            1,
            0,
            0,
            0,
            0.3f,
            5.0f,
            List.of(),
            RidingView.NONE,
            false,
            false);
    final RegistryByteBuf buf = newBuf();
    PetSyncData.CODEC.encode(buf, pet);

    final PetSyncData decoded = PetSyncData.CODEC.decode(buf);
    assertEquals(pet, decoded);
    assertEquals(lifeState.isAlive(), decoded.alive(), "alive");
    assertEquals(lifeState == PetLifeState.UNDEAD, decoded.undead(), "undead");
  }

  @ParameterizedTest(name = "baby {0}, senior {1} round-trips as {2}")
  @CsvSource({"false, false, ADULT", "false, true, SENIOR", "true, false, PUPPY"})
  @DisplayName("codec round-trips the senior flag so the client can label seniors")
  void codecRoundTripsSenior(final boolean baby, final boolean senior, final PetAge age) {
    final PetSyncData pet =
        new PetSyncData(
            samplePet(UnleashedDogBreed.BEAGLE, "Old Tom").petId(),
            UnleashedDogBreed.BEAGLE,
            "Old Tom",
            9.0f,
            18.0f,
            1,
            2,
            3,
            "minecraft:overworld",
            PetLifeState.LIVING,
            baby,
            1,
            0,
            0,
            0,
            0.27f,
            5.0f,
            List.of(),
            RidingView.NONE,
            false,
            senior);
    final RegistryByteBuf buf = newBuf();
    PetSyncData.CODEC.encode(buf, pet);
    final PetSyncData decoded = PetSyncData.CODEC.decode(buf);
    assertEquals(pet, decoded);
    assertEquals(age, decoded.age());
  }

  @ParameterizedTest(name = "aging {0}, {1}, age {2} -> senior {3}")
  @CsvSource({
    "true, LIVING, 2400000, true",
    "true, LIVING, 2399999, false",
    "false, LIVING, 2400000, false",
    "true, UNDEAD, 2400000, false",
    "true, DECEASED, 2400000, true"
  })
  @DisplayName("the synced senior label follows the live aging setting, not a stale flag")
  void seniorFollowsTheLiveSetting(
      final boolean enabled,
      final PetLifeState lifeState,
      final long ageTicks,
      final boolean expected) {
    final DogsUnleashedConfig original = DogsUnleashed.SERVER_CONFIG;
    try {
      DogsUnleashed.SERVER_CONFIG = original.withAging(new AgingSettings(enabled, 100));
      final NbtCompound nbt = new NbtCompound();
      nbt.putUuid(ModNbtKeys.PET_ID, UUID.randomUUID());
      nbt.putUuid(ModNbtKeys.OWNER_ID, UUID.randomUUID());
      nbt.putString(ModNbtKeys.BREED_TYPE, UnleashedDogBreed.BEAGLE.serializedId());
      nbt.putString(ModNbtKeys.LIFE_STATE, lifeState.serializedName());
      nbt.putLong(ModNbtKeys.ADULT_AGE_TICKS, ageTicks);
      assertEquals(expected, PetSyncData.seniorOf(PetData.fromNbt(nbt)));
    } finally {
      DogsUnleashed.SERVER_CONFIG = original;
    }
  }

  static Stream<Arguments> displayBreedCases() {
    return Stream.of(
        Arguments.of(
            "pure breed ignores composition",
            samplePet(UnleashedDogBreed.BEAGLE, "Rex"),
            UnleashedDogBreed.BEAGLE),
        Arguments.of(
            "cross-breed without composition falls back to its own breed",
            samplePet(UnleashedDogBreed.CROSS_BREED, "Pixel"),
            UnleashedDogBreed.CROSS_BREED),
        Arguments.of(
            "cross-breed resolves the dominant composition breed",
            samplePet(
                UnleashedDogBreed.CROSS_BREED,
                "Pixel",
                List.of(
                    new BreedShare(UnleashedDogBreed.SHIBA_INU, 0.25f),
                    new BreedShare(UnleashedDogBreed.HUSKY, 0.75f))),
            UnleashedDogBreed.HUSKY),
        Arguments.of(
            "cross-breed breaks share ties by serialized id",
            samplePet(
                UnleashedDogBreed.CROSS_BREED,
                "Pixel",
                List.of(
                    new BreedShare(UnleashedDogBreed.SHIBA_INU, 0.5f),
                    new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f))),
            UnleashedDogBreed.BEAGLE));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("displayBreedCases")
  @DisplayName("displayBreed resolves the breed the client should render")
  void displayBreedResolvesRenderBreed(
      final String label, final PetSyncData pet, final UnleashedDogBreed expected) {
    assertEquals(expected, pet.displayBreed());
  }
}
