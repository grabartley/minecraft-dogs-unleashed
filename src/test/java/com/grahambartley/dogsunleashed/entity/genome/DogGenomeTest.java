package com.grahambartley.dogsunleashed.entity.genome;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogGenomeTest {

  private static final List<BreedShare> HALF_HUSKY_HALF_BEAGLE =
      List.of(
          new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f),
          new BreedShare(UnleashedDogBreed.HUSKY, 0.5f));

  @ParameterizedTest(name = "{0}")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("a pure genome carries the breed's documented attributes and voice")
  void pureGenomeCarriesBreedAttributesAndVoice(final UnleashedDogBreed breed) {
    final DogGenome genome = DogGenome.pure(breed);
    assertEquals(List.of(new BreedShare(breed, 1.0f)), genome.composition());
    assertEquals(breed.attributes().maxHealth(), genome.maxHealth());
    assertEquals(breed.attributes().movementSpeed(), genome.movementSpeed());
    assertEquals(breed.attributes().attackDamage(), genome.attackDamage());
    assertEquals(breed, genome.voiceBreed());
    assertEquals(breed, genome.dominantBreed());
  }

  static Stream<Arguments> dominantCompositions() {
    return Stream.of(
        Arguments.of(
            "largest share wins",
            List.of(
                new BreedShare(UnleashedDogBreed.SHIBA_INU, 0.25f),
                new BreedShare(UnleashedDogBreed.HUSKY, 0.75f)),
            UnleashedDogBreed.HUSKY),
        Arguments.of(
            "ties break by serialized id",
            List.of(
                new BreedShare(UnleashedDogBreed.SHIBA_INU, 0.5f),
                new BreedShare(UnleashedDogBreed.DACHSHUND, 0.5f)),
            UnleashedDogBreed.DACHSHUND));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("dominantCompositions")
  @DisplayName("the dominant breed is the largest share with id tie-breaking")
  void dominantBreedIsLargestShare(
      final String label, final List<BreedShare> composition, final UnleashedDogBreed expected) {
    final DogGenome genome = new DogGenome(composition, 18.0, 0.29, 3.5, UnleashedDogBreed.HUSKY);
    assertEquals(expected, genome.dominantBreed());
  }

  @Test
  @DisplayName("the constructor sorts the composition by descending share then id")
  void constructorSortsComposition() {
    final DogGenome genome =
        new DogGenome(
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.25f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.75f)),
            18.0,
            0.29,
            3.5,
            UnleashedDogBreed.HUSKY);
    assertEquals(
        List.of(
            new BreedShare(UnleashedDogBreed.BEAGLE, 0.75f),
            new BreedShare(UnleashedDogBreed.HUSKY, 0.25f)),
        genome.composition());
  }

  @Test
  @DisplayName("a genome survives an NBT round trip")
  void genomeSurvivesNbtRoundTrip() {
    final DogGenome genome =
        new DogGenome(HALF_HUSKY_HALF_BEAGLE, 21.25, 0.301, 4.125, UnleashedDogBreed.HUSKY);
    assertEquals(genome, DogGenome.fromNbt(genome.toNbt()));
  }

  @Test
  @DisplayName("an empty compound reads as no genome")
  void emptyCompoundReadsAsNoGenome() {
    assertNull(DogGenome.fromNbt(new NbtCompound()));
  }

  @Test
  @DisplayName("unknown breed keys in a stored composition are skipped")
  void unknownBreedKeysAreSkipped() {
    final DogGenome genome =
        new DogGenome(HALF_HUSKY_HALF_BEAGLE, 21.0, 0.3, 4.0, UnleashedDogBreed.BEAGLE);
    final NbtCompound nbt = genome.toNbt();
    nbt.getCompound(ModNbtKeys.GENOME_COMPOSITION).putFloat("labrador", 0.5f);
    assertEquals(HALF_HUSKY_HALF_BEAGLE, DogGenome.fromNbt(nbt).composition());
  }

  @Test
  @DisplayName("a missing voice breed falls back to the dominant composition breed")
  void missingVoiceBreedFallsBackToDominant() {
    final DogGenome genome =
        new DogGenome(
            List.of(
                new BreedShare(UnleashedDogBreed.SHIBA_INU, 0.75f),
                new BreedShare(UnleashedDogBreed.HUSKY, 0.25f)),
            18.0,
            0.3,
            3.5,
            UnleashedDogBreed.HUSKY);
    final NbtCompound nbt = genome.toNbt();
    nbt.remove(ModNbtKeys.GENOME_VOICE_BREED);
    assertEquals(UnleashedDogBreed.SHIBA_INU, DogGenome.fromNbt(nbt).voiceBreed());
  }
}
