package com.grahambartley.dogsunleashed.entity.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.rig.DogProportions.BoneAdjustment;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogProportionsTest {

  private static final float TOLERANCE = 1.0e-5f;

  @ParameterizedTest(name = "{0}")
  @MethodSource("sharedRigBreeds")
  @DisplayName("a breed with no per-bone shape values leaves every bone untouched")
  void breedsWithoutShapeValuesLeaveBonesUntouched(final UnleashedDogBreed breed) {
    final DogProportions proportions = DogProportions.of(breed);
    assertTrue(proportions.bones().isEmpty());
    assertTrue(proportions.forBone("Torso").isIdentity());
    assertTrue(proportions.forBone("frontleg1").isIdentity());
  }

  static Stream<Arguments> sharedRigBreeds() {
    return Stream.of(
        Arguments.of(UnleashedDogBreed.GOLDEN_RETRIEVER), Arguments.of(UnleashedDogBreed.BEAGLE));
  }

  @Test
  @DisplayName("an unknown bone reports no adjustment rather than failing")
  void unknownBoneReportsNoAdjustment() {
    assertEquals(
        BoneAdjustment.NONE, DogProportions.of(UnleashedDogBreed.BEAGLE).forBone("not_a_bone"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("sharedRigBreeds")
  @DisplayName("a single-ancestor composition blends to that breed's own proportions")
  void pureCompositionMatchesBreed(final UnleashedDogBreed breed) {
    assertEquals(
        DogProportions.of(breed), DogProportions.blend(List.of(new BreedShare(breed, 1.0f))));
  }

  @ParameterizedTest(name = "beagle share {0} gives scale {1}")
  @MethodSource("crossShares")
  @DisplayName("a cross lands between its ancestors in size, in proportion to its ancestry")
  void crossInterpolatesSizeBetweenAncestors(final float beagleShare, final float expectedScale) {
    final DogProportions blended =
        DogProportions.blend(
            List.of(
                new BreedShare(UnleashedDogBreed.BEAGLE, beagleShare),
                new BreedShare(UnleashedDogBreed.GOLDEN_RETRIEVER, 1.0f - beagleShare)));
    assertEquals(expectedScale, blended.adultScale(), TOLERANCE);
  }

  static Stream<Arguments> crossShares() {
    return Stream.of(
        Arguments.of(0.0f, 1.7f),
        Arguments.of(0.25f, 1.65f),
        Arguments.of(0.5f, 1.6f),
        Arguments.of(0.75f, 1.55f),
        Arguments.of(1.0f, 1.5f));
  }

  @Test
  @DisplayName("a cross interpolates the baby size too, so puppies are not adult-sized")
  void crossInterpolatesBabyScale() {
    final DogProportions blended =
        DogProportions.blend(
            List.of(
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f),
                new BreedShare(UnleashedDogBreed.GOLDEN_RETRIEVER, 0.5f)));
    assertEquals(0.8f, blended.babyScale(), TOLERANCE);
  }

  @Test
  @DisplayName("shares that do not add up to one are normalised before blending")
  void unnormalisedSharesAreNormalised() {
    final DogProportions blended =
        DogProportions.blend(
            List.of(
                new BreedShare(UnleashedDogBreed.BEAGLE, 2.0f),
                new BreedShare(UnleashedDogBreed.GOLDEN_RETRIEVER, 2.0f)));
    assertEquals(1.6f, blended.adultScale(), TOLERANCE);
  }

  @Test
  @DisplayName("ancestors with no share of the dog do not drag the blend toward themselves")
  void zeroShareAncestorsAreIgnored() {
    final DogProportions blended =
        DogProportions.blend(
            List.of(
                new BreedShare(UnleashedDogBreed.GOLDEN_RETRIEVER, 1.0f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.0f)));
    assertEquals(1.7f, blended.adultScale(), TOLERANCE);
  }

  @Test
  @DisplayName("bones that blend back to no adjustment are dropped rather than applied as no-ops")
  void identityBonesAreDropped() {
    final DogProportions blended =
        DogProportions.blend(
            List.of(
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f),
                new BreedShare(UnleashedDogBreed.GOLDEN_RETRIEVER, 0.5f)));
    assertEquals(Map.of(), blended.bones());
  }

  @Test
  @DisplayName("an empty composition falls back to a usable set of proportions")
  void emptyCompositionFallsBack() {
    final DogProportions blended = DogProportions.blend(List.of());
    assertEquals(DogProportions.of(UnleashedDogBreed.CROSS_BREED), blended);
  }
}
