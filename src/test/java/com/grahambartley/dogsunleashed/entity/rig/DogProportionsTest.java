package com.grahambartley.dogsunleashed.entity.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.rig.DogProportions.BoneAdjustment;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
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

  @Test
  @DisplayName(
      "the golden retriever is the shared rig's reference shape, so it needs no adjustment")
  void goldenRetrieverNeedsNoAdjustment() {
    final DogProportions proportions = DogProportions.of(UnleashedDogBreed.GOLDEN_RETRIEVER);
    assertTrue(proportions.bones().isEmpty());
    assertTrue(proportions.forBone("Torso").isIdentity());
  }

  @Test
  @DisplayName("the beagle drops its body without dragging the legs down with it")
  void beagleDropsBodyButNotLegs() {
    final DogProportions beagle = DogProportions.of(UnleashedDogBreed.BEAGLE);
    assertEquals(-1.7f, beagle.forBone("Torso").offsetY(), TOLERANCE);
    for (final String leg : List.of("frontleg1", "frontleg2", "backleg1", "backleg2")) {
      assertEquals(1.6f, beagle.forBone(leg).offsetY(), TOLERANCE, leg);
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("pureCompositions")
  @DisplayName("a single-ancestor composition blends to that breed's own proportions")
  void pureCompositionMatchesBreed(final UnleashedDogBreed breed) {
    final DogProportions blended = DogProportions.blend(List.of(new BreedShare(breed, 1.0f)));
    assertEquals(DogProportions.of(breed), blended);
  }

  static Stream<Arguments> pureCompositions() {
    return Stream.of(
        Arguments.of(UnleashedDogBreed.GOLDEN_RETRIEVER), Arguments.of(UnleashedDogBreed.BEAGLE));
  }

  @ParameterizedTest(name = "beagle share {0} gives torso offset {1}")
  @MethodSource("crossShares")
  @DisplayName("a cross lands between its ancestors in proportion to its ancestry")
  void crossInterpolatesBetweenAncestors(
      final float beagleShare, final float expectedTorsoOffset, final float expectedScale) {
    final DogProportions blended =
        DogProportions.blend(
            List.of(
                new BreedShare(UnleashedDogBreed.BEAGLE, beagleShare),
                new BreedShare(UnleashedDogBreed.GOLDEN_RETRIEVER, 1.0f - beagleShare)));
    assertEquals(expectedTorsoOffset, blended.forBone("Torso").offsetY(), TOLERANCE);
    assertEquals(expectedScale, blended.adultScale(), TOLERANCE);
  }

  static Stream<Arguments> crossShares() {
    return Stream.of(
        Arguments.of(0.25f, -0.425f, 1.65f),
        Arguments.of(0.5f, -0.85f, 1.6f),
        Arguments.of(0.75f, -1.275f, 1.55f));
  }

  @Test
  @DisplayName("a cross keeps the leg cancellation in step with the body drop")
  void crossKeepsLegsInStepWithBody() {
    final DogProportions blended =
        DogProportions.blend(
            List.of(
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f),
                new BreedShare(UnleashedDogBreed.GOLDEN_RETRIEVER, 0.5f)));
    final BoneAdjustment torso = blended.forBone("Torso");
    final BoneAdjustment leg = blended.forBone("frontleg1");
    assertEquals(-0.85f, torso.offsetY(), TOLERANCE);
    assertEquals(0.8f, leg.offsetY(), TOLERANCE);
  }

  @Test
  @DisplayName("shares that do not add up to one are normalised before blending")
  void unnormalisedSharesAreNormalised() {
    final DogProportions blended =
        DogProportions.blend(
            List.of(
                new BreedShare(UnleashedDogBreed.BEAGLE, 2.0f),
                new BreedShare(UnleashedDogBreed.GOLDEN_RETRIEVER, 2.0f)));
    assertEquals(-0.85f, blended.forBone("Torso").offsetY(), TOLERANCE);
  }

  @Test
  @DisplayName("bones that blend back to no adjustment are dropped rather than applied as no-ops")
  void identityBonesAreDropped() {
    final DogProportions blended =
        DogProportions.blend(List.of(new BreedShare(UnleashedDogBreed.GOLDEN_RETRIEVER, 1.0f)));
    assertTrue(blended.bones().isEmpty());
  }
}
