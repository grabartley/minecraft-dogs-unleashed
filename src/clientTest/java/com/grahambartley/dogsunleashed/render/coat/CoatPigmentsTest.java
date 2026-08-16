package com.grahambartley.dogsunleashed.render.coat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.render.coat.CoatPigments.Donor;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CoatPigmentsTest {

  private static final CoatRecipe BLACK_AND_WHITE =
      new CoatRecipe(
          List.of("pigment1", "pigment2"),
          Map.of("pigment1", 0xFFFFFF, "pigment2", 0x000000),
          Map.of("pigment1", 128, "pigment2", 128));
  private static final CoatRecipe MID_GREY =
      new CoatRecipe(
          List.of("pigment1", "pigment2"),
          Map.of("pigment1", 0x808080, "pigment2", 0x808080),
          Map.of("pigment1", 128, "pigment2", 128));
  private static final CoatRecipe SINGLE_RED =
      new CoatRecipe(List.of("pigment1"), Map.of("pigment1", 0xFF0000), Map.of("pigment1", 128));

  @Test
  @DisplayName(
      "a dog with one ancestor keeps its painted pigments exactly, so pure breeds do not shift")
  void singleAncestorKeepsPigments() {
    final Map<String, Integer> blended =
        CoatPigments.blend(BLACK_AND_WHITE, List.of(new Donor(BLACK_AND_WHITE, 1.0f)));
    assertEquals(0xFFFFFF, blended.get("pigment1"));
    assertEquals(0x000000, blended.get("pigment2"));
  }

  @Test
  @DisplayName("no donors at all leaves the layout's own pigments untouched")
  void noDonorsKeepsLayoutPigments() {
    final Map<String, Integer> blended = CoatPigments.blend(BLACK_AND_WHITE, List.of());
    assertEquals(0xFFFFFF, blended.get("pigment1"));
    assertEquals(0x000000, blended.get("pigment2"));
  }

  @ParameterizedTest(name = "grey share {0} gives pigment1 {1}")
  @MethodSource("blendShares")
  @DisplayName("pigments move toward each ancestor in proportion to its share")
  void pigmentsMoveTowardAncestors(final float greyShare, final int expected) {
    final Map<String, Integer> blended =
        CoatPigments.blend(
            BLACK_AND_WHITE,
            List.of(new Donor(BLACK_AND_WHITE, 1.0f - greyShare), new Donor(MID_GREY, greyShare)));
    assertEquals(expected, blended.get("pigment1"));
  }

  static Stream<Arguments> blendShares() {
    return Stream.of(
        Arguments.of(0.0f, 0xFFFFFF), Arguments.of(0.5f, 0xC0C0C0), Arguments.of(1.0f, 0x808080));
  }

  @Test
  @DisplayName("shares that do not add up to one are normalised before blending")
  void sharesAreNormalised() {
    final Map<String, Integer> blended =
        CoatPigments.blend(
            BLACK_AND_WHITE, List.of(new Donor(BLACK_AND_WHITE, 3.0f), new Donor(MID_GREY, 3.0f)));
    assertEquals(0xC0C0C0, blended.get("pigment1"));
  }

  @Test
  @DisplayName(
      "a donor with fewer pigments than the layout contributes its last one rather than dropping out")
  void shorterDonorClampsToItsLastPigment() {
    final Map<String, Integer> blended =
        CoatPigments.blend(
            BLACK_AND_WHITE,
            List.of(new Donor(BLACK_AND_WHITE, 0.5f), new Donor(SINGLE_RED, 0.5f)));
    assertEquals(0xFF8080, blended.get("pigment1"));
    assertEquals(0x800000, blended.get("pigment2"));
  }

  @Test
  @DisplayName("negative or zero shares are ignored instead of skewing the blend")
  void nonPositiveSharesAreIgnored() {
    final Map<String, Integer> blended =
        CoatPigments.blend(
            BLACK_AND_WHITE, List.of(new Donor(BLACK_AND_WHITE, 1.0f), new Donor(MID_GREY, 0.0f)));
    assertEquals(0xFFFFFF, blended.get("pigment1"));
  }

  @Test
  @DisplayName("composition shares are normalised for callers building donor lists")
  void compositionSharesAreNormalised() {
    final List<BreedShare> normalised =
        CoatPigments.normalisedShares(
            List.of(
                new BreedShare(UnleashedDogBreed.BEAGLE, 2.0f),
                new BreedShare(UnleashedDogBreed.GOLDEN_RETRIEVER, 6.0f)));
    assertEquals(0.25f, normalised.get(0).share(), 1.0e-5f);
    assertEquals(0.75f, normalised.get(1).share(), 1.0e-5f);
  }

  @Test
  @DisplayName("a composition with no positive shares yields nothing to blend from")
  void emptyCompositionYieldsNoShares() {
    assertTrue(
        CoatPigments.normalisedShares(List.of(new BreedShare(UnleashedDogBreed.BEAGLE, 0.0f)))
            .isEmpty());
  }
}
