package com.grahambartley.dogsunleashed.entity.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogRigTest {

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("breedSupport")
  @DisplayName("only breeds normalised onto the shared skeleton use it")
  void onlyNormalisedBreedsUseSharedRig(final UnleashedDogBreed breed, final boolean expected) {
    assertEquals(expected, DogRig.supports(breed));
  }

  static Stream<Arguments> breedSupport() {
    return Stream.of(
        Arguments.of(UnleashedDogBreed.GOLDEN_RETRIEVER, true),
        Arguments.of(UnleashedDogBreed.BEAGLE, true),
        Arguments.of(UnleashedDogBreed.HUSKY, false),
        Arguments.of(UnleashedDogBreed.DACHSHUND, false),
        Arguments.of(UnleashedDogBreed.SHIBA_INU, false),
        Arguments.of(UnleashedDogBreed.CROSS_BREED, false));
  }

  @Test
  @DisplayName("a cross of two normalised breeds uses the shared rig")
  void crossOfNormalisedBreedsUsesSharedRig() {
    assertTrue(
        DogRig.supports(
            List.of(
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f),
                new BreedShare(UnleashedDogBreed.GOLDEN_RETRIEVER, 0.5f))));
  }

  @Test
  @DisplayName("one un-normalised ancestor keeps the whole dog on the old per-breed rig")
  void anyUnnormalisedAncestorFallsBack() {
    assertFalse(
        DogRig.supports(
            List.of(
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.9f),
                new BreedShare(UnleashedDogBreed.HUSKY, 0.1f))));
  }

  @Test
  @DisplayName("an empty composition is not treated as shared-rig capable")
  void emptyCompositionFallsBack() {
    assertFalse(DogRig.supports(List.of()));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("a dog with no genome falls back to judging its own breed")
  void missingGenomeFallsBackToBreed(final UnleashedDogBreed breed) {
    assertEquals(DogRig.supports(breed), DogRig.supports(null, breed));
  }

  @Test
  @DisplayName("a pure genome and a bare breed agree on whether the shared rig applies")
  void pureGenomeAgreesWithBreed() {
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      final DogGenome genome = DogGenome.pure(breed);
      assertEquals(DogRig.supports(breed), DogRig.supports(genome, breed), breed.serializedId());
    }
  }
}
