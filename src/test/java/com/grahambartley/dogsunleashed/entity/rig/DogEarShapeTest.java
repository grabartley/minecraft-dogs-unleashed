package com.grahambartley.dogsunleashed.entity.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogEarShapeTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogEarShape.class)
  @DisplayName("each shape names a variant bone under both ear pivots")
  void shapeNamesVariantBones(final DogEarShape shape) {
    assertEquals("ear1_" + shape.breed().serializedId(), shape.boneName("ear1"));
    assertEquals("ear2_" + shape.breed().serializedId(), shape.boneName("ear2"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("shapedBreeds")
  @DisplayName("a pure dog always inherits its own breed's ears whatever its id")
  void pureDogInheritsOwnEars(final UnleashedDogBreed breed) {
    final DogEarShape expected = DogEarShape.of(breed);
    assertNotNull(expected);
    for (int seed = 0; seed < 200; seed++) {
      final UUID dogId = new UUID(seed * 31L, seed * 17L);
      assertEquals(expected, DogEarShape.inherit(List.of(new BreedShare(breed, 1.0f)), dogId));
    }
  }

  static Stream<Arguments> shapedBreeds() {
    return Stream.of(
        Arguments.of(UnleashedDogBreed.GOLDEN_RETRIEVER), Arguments.of(UnleashedDogBreed.BEAGLE));
  }

  @Test
  @DisplayName("breeds with no ear art of their own do not map to a shape")
  void unshapedBreedsHaveNoShape() {
    assertNull(DogEarShape.of(UnleashedDogBreed.HUSKY));
    assertNull(DogEarShape.of(UnleashedDogBreed.SHIBA_INU));
  }

  @Test
  @DisplayName("the same dog always gets the same ears, so its look does not flicker")
  void inheritanceIsStablePerDog() {
    final List<BreedShare> composition =
        List.of(
            new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f),
            new BreedShare(UnleashedDogBreed.GOLDEN_RETRIEVER, 0.5f));
    final UUID dogId = UUID.fromString("6f3a1b2c-4d5e-6f70-8192-a3b4c5d6e7f8");
    final DogEarShape first = DogEarShape.inherit(composition, dogId);
    for (int repeat = 0; repeat < 50; repeat++) {
      assertEquals(first, DogEarShape.inherit(composition, dogId));
    }
  }

  @Test
  @DisplayName("a cross takes one ancestor's ears whole, never a mix, and roughly in proportion")
  void crossInheritsOneAncestorWholeInProportion() {
    final List<BreedShare> composition =
        List.of(
            new BreedShare(UnleashedDogBreed.BEAGLE, 0.75f),
            new BreedShare(UnleashedDogBreed.GOLDEN_RETRIEVER, 0.25f));
    int beagleEars = 0;
    final int samples = 2000;
    for (int seed = 0; seed < samples; seed++) {
      final DogEarShape shape =
          DogEarShape.inherit(composition, new UUID(seed * 2654435761L, seed * 40503L));
      assertTrue(shape == DogEarShape.BEAGLE || shape == DogEarShape.GOLDEN_RETRIEVER);
      if (shape == DogEarShape.BEAGLE) {
        beagleEars++;
      }
    }
    final double beagleRate = beagleEars / (double) samples;
    assertTrue(beagleRate > 0.6 && beagleRate < 0.9, "beagle ear rate was " + beagleRate);
  }

  @Test
  @DisplayName(
      "ancestors with no ear art of their own are skipped rather than blocking inheritance")
  void ancestorsWithoutEarArtAreSkipped() {
    final List<BreedShare> composition =
        List.of(
            new BreedShare(UnleashedDogBreed.HUSKY, 0.9f),
            new BreedShare(UnleashedDogBreed.BEAGLE, 0.1f));
    for (int seed = 0; seed < 100; seed++) {
      assertEquals(
          DogEarShape.BEAGLE,
          DogEarShape.inherit(composition, new UUID(seed * 7907L, seed * 611953L)));
    }
  }
}
