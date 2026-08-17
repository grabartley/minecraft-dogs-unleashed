package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.newBuf;
import static com.grahambartley.dogsunleashed.network.payload.PayloadTestFixtures.samplePet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.network.RegistryByteBuf;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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
