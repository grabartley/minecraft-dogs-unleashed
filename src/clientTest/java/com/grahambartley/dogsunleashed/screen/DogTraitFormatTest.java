package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class DogTraitFormatTest {

  private static String fakeName(final UnleashedDogBreed breed) {
    final String id = breed.serializedId();
    return id.substring(0, 1).toUpperCase(Locale.ROOT) + id.substring(1);
  }

  static Stream<Arguments> compositions() {
    return Stream.of(
        Arguments.of(
            "pure breed", List.of(new BreedShare(UnleashedDogBreed.HUSKY, 1.0f)), "Husky 100%"),
        Arguments.of(
            "even mix",
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.5f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f)),
            "Husky 50%, Beagle 50%"),
        Arguments.of(
            "uneven mix",
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.75f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.25f)),
            "Husky 75%, Beagle 25%"),
        Arguments.of(
            "tiny share",
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.9921875f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.0078125f)),
            "Husky 99%, Beagle <1%"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("compositions")
  @DisplayName("compositions format as a comma separated list of breed percentages")
  void compositionsFormatAsPercentageList(
      final String label, final List<BreedShare> composition, final String expected) {
    assertEquals(
        expected, DogTraitFormat.formatComposition(composition, DogTraitFormatTest::fakeName));
  }

  @ParameterizedTest(name = "share {0} formats as {1}")
  @CsvSource({
    "1.0, 100%",
    "0.5, 50%",
    "0.25, 25%",
    "0.125, 13%",
    "0.0625, 6%",
    "0.01, 1%",
    "0.0099, <1%",
    "0.0, <1%",
  })
  @DisplayName("shares round to whole percentages with a floor label under one percent")
  void sharesFormatAsPercent(final float share, final String expected) {
    assertEquals(expected, DogTraitFormat.formatPercent(share));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "Rare, 4, Rare (4%)",
    "Epic, 1, Epic (1%)",
    "Common, 40, Common (40%)",
    "Common, 100, Common",
  })
  @DisplayName("rarity shows its chance except when the roll is guaranteed")
  void rarityFormatsWithChance(
      final String rarityName, final int chancePercent, final String expected) {
    assertEquals(expected, DogTraitFormat.formatRarity(rarityName, chancePercent));
  }

  static Stream<Arguments> mixNameCompositions() {
    return Stream.of(
        Arguments.of(
            "even mix names both breeds",
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.5f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f)),
            List.of(UnleashedDogBreed.BEAGLE, UnleashedDogBreed.HUSKY)),
        Arguments.of(
            "dominant breed leads the pair",
            List.of(
                new BreedShare(UnleashedDogBreed.SHIBA_INU, 0.25f),
                new BreedShare(UnleashedDogBreed.HUSKY, 0.75f)),
            List.of(UnleashedDogBreed.HUSKY, UnleashedDogBreed.SHIBA_INU)),
        Arguments.of(
            "one meaningful share still names the top two",
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.9f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.1f)),
            List.of(UnleashedDogBreed.HUSKY, UnleashedDogBreed.BEAGLE)),
        Arguments.of(
            "three meaningful shares fall back to the generic name",
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.34f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.33f),
                new BreedShare(UnleashedDogBreed.SHIBA_INU, 0.33f)),
            List.of()),
        Arguments.of(
            "a single entry falls back to the generic name",
            List.of(new BreedShare(UnleashedDogBreed.HUSKY, 1.0f)),
            List.of()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("mixNameCompositions")
  @DisplayName("mix names use the top two breeds unless three or more shares are meaningful")
  void mixNameBreedsPickTopTwoOrFallBack(
      final String label,
      final List<BreedShare> composition,
      final List<UnleashedDogBreed> expected) {
    assertEquals(expected, DogTraitFormat.mixNameBreeds(composition));
  }
}
