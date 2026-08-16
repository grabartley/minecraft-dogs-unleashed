package com.grahambartley.dogsunleashed.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogRarityClassifierTest {

  static Stream<Arguments> coatChances() {
    return Stream.of(
        Arguments.of("husky black white", UnleashedDogBreed.HUSKY, HuskyCoat.BLACK_WHITE, 40),
        Arguments.of("husky red white", UnleashedDogBreed.HUSKY, HuskyCoat.RED_WHITE, 15),
        Arguments.of("husky sable", UnleashedDogBreed.HUSKY, HuskyCoat.SABLE, 13),
        Arguments.of("husky agouti", UnleashedDogBreed.HUSKY, HuskyCoat.AGOUTI, 4),
        Arguments.of("husky white", UnleashedDogBreed.HUSKY, HuskyCoat.WHITE, 3),
        Arguments.of(
            "dachshund black tan", UnleashedDogBreed.DACHSHUND, DachshundCoat.BLACK_TAN, 36),
        Arguments.of(
            "dachshund red piebald", UnleashedDogBreed.DACHSHUND, DachshundCoat.RED_PIEBALD, 6),
        Arguments.of("dachshund blue tan", UnleashedDogBreed.DACHSHUND, DachshundCoat.BLUE_TAN, 1),
        Arguments.of("dachshund albino", UnleashedDogBreed.DACHSHUND, DachshundCoat.ALBINO, 1),
        Arguments.of("beagle tri 1", UnleashedDogBreed.BEAGLE, BeagleCoat.TRI_1, 18),
        Arguments.of("beagle lilac 1", UnleashedDogBreed.BEAGLE, BeagleCoat.LILAC_1, 7),
        Arguments.of("beagle lilac 2", UnleashedDogBreed.BEAGLE, BeagleCoat.LILAC_2, 5),
        Arguments.of("shiba red", UnleashedDogBreed.SHIBA_INU, ShibaInuCoat.RED, 65),
        Arguments.of("shiba black", UnleashedDogBreed.SHIBA_INU, ShibaInuCoat.BLACK, 35),
        Arguments.of("shiba sesame", UnleashedDogBreed.SHIBA_INU, ShibaInuCoat.SESAME, 15));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("coatChances")
  @DisplayName("coat chance matches the best odds across natural and breeding roll tables")
  void coatChanceMatchesRollTables(
      final String label,
      final UnleashedDogBreed breed,
      final UnleashedDogCoat coat,
      final int expectedChance) {
    assertEquals(expectedChance, DogRarityClassifier.chancePercent(breed, coat.getOrdinal()));
  }

  static Stream<Arguments> rarityTiers() {
    return Stream.of(
        Arguments.of("guaranteed", 100, DogRarity.COMMON),
        Arguments.of("common floor", 25, DogRarity.COMMON),
        Arguments.of("uncommon ceiling", 24, DogRarity.UNCOMMON),
        Arguments.of("uncommon floor", 10, DogRarity.UNCOMMON),
        Arguments.of("rare ceiling", 9, DogRarity.RARE),
        Arguments.of("rare floor", 3, DogRarity.RARE),
        Arguments.of("epic ceiling", 2, DogRarity.EPIC),
        Arguments.of("epic floor", 0, DogRarity.EPIC));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rarityTiers")
  @DisplayName("chance percentages classify into the expected rarity tiers")
  void chanceClassifiesIntoTier(
      final String label, final int chancePercent, final DogRarity expectedRarity) {
    assertEquals(expectedRarity, DogRarityClassifier.classify(chancePercent));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("every breed except the golden retriever and the cross-breed has coat variants")
  void coatVariantAvailabilityPerBreed(final UnleashedDogBreed breed) {
    if (breed == UnleashedDogBreed.GOLDEN_RETRIEVER || breed == UnleashedDogBreed.CROSS_BREED) {
      assertFalse(DogRarityClassifier.hasCoatVariants(breed));
    } else {
      assertTrue(DogRarityClassifier.hasCoatVariants(breed));
    }
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("an unset coat variant is always a guaranteed common roll")
  void unsetCoatVariantIsGuaranteedCommon(final UnleashedDogBreed breed) {
    assertEquals(
        DogRarityClassifier.GUARANTEED_CHANCE_PERCENT,
        DogRarityClassifier.chancePercent(breed, -1));
    assertEquals(DogRarity.COMMON, DogRarityClassifier.rarityOf(breed, -1));
  }
}
