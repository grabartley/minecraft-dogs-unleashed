package com.grahambartley.dogsunleashed.entity.genome;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.DogAppearance;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.variant.DogCoats;
import com.grahambartley.dogsunleashed.entity.variant.DogRarityClassifier;
import com.grahambartley.dogsunleashed.entity.variant.HuskyCoat;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import com.grahambartley.dogsunleashed.entity.variant.ShibaInuCoat;
import com.grahambartley.dogsunleashed.entity.variant.UnleashedDogCoat;
import java.util.Set;
import net.minecraft.entity.SpawnReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogGeneTraitTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("a breed has a coat gene exactly when it has coat variants")
  void coatGeneFollowsCoatVariants(final UnleashedDogBreed breed) {
    assertEquals(DogCoats.hasCoatVariants(breed), DogGeneTrait.COAT.appliesTo(breed));
    assertEquals(breed.hasEyeColorVariants(), DogGeneTrait.EYES.appliesTo(breed));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = {"HUSKY", "DACHSHUND", "BEAGLE", "SHIBA_INU", "DIRE_WOLF"})
  @DisplayName("coats rarer than uncommon in the wild are recessive, the most common is wild type")
  void coatDominanceFollowsWildRarity(final UnleashedDogBreed breed) {
    final GeneDominance dominance = DogGeneTrait.COAT.dominance(breed);
    int bestChance = -1;
    for (final UnleashedDogCoat coat : DogCoats.coatsOf(breed)) {
      final int chance = DogRarityClassifier.naturalChancePercent(breed, coat.getOrdinal());
      assertEquals(
          chance < DogRarityClassifier.UNCOMMON_MIN_CHANCE_PERCENT,
          dominance.isRecessive(coat.getOrdinal()),
          coat + " at " + chance + "%");
      bestChance = Math.max(bestChance, chance);
    }
    assertEquals(
        bestChance,
        DogRarityClassifier.naturalChancePercent(breed, dominance.wildType()),
        "the wild type should be the most common coat");
  }

  @Test
  @DisplayName("husky white and agouti are recessive, black and white is the wild type")
  void huskyCoatDominance() {
    final GeneDominance dominance = DogGeneTrait.COAT.dominance(UnleashedDogBreed.HUSKY);
    assertEquals(
        Set.of(HuskyCoat.AGOUTI.ordinal(), HuskyCoat.WHITE.ordinal()),
        dominance.recessiveAlleles());
    assertEquals(HuskyCoat.BLACK_WHITE.ordinal(), dominance.wildType());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(HuskyEyeColor.class)
  @DisplayName("odd eyes are recessive and hazel is the wild type")
  void eyeDominance(final HuskyEyeColor eyes) {
    final GeneDominance dominance = DogGeneTrait.EYES.dominance(UnleashedDogBreed.HUSKY);
    assertEquals(eyes.heterochromatic(), dominance.isRecessive(eyes.ordinal()));
    assertEquals(HuskyEyeColor.HAZEL_HAZEL.ordinal(), dominance.wildType());
  }

  @Test
  @DisplayName("a wild roll uses the breed's own spawn table")
  void wildShownRollUsesSpawnTable() {
    for (int roll = 0; roll < DogCoats.ROLL_BOUND; roll++) {
      final int fixedRoll = roll;
      assertEquals(
          DogCoats.rollResolverFor(UnleashedDogBreed.SHIBA_INU)
              .apply(SpawnReason.NATURAL, roll)
              .getOrdinal(),
          DogGeneTrait.COAT.rollShown(
              UnleashedDogBreed.SHIBA_INU, SpawnReason.NATURAL, bound -> fixedRoll));
      assertEquals(
          DogCoats.rollResolverFor(UnleashedDogBreed.SHIBA_INU)
              .apply(SpawnReason.BREEDING, roll)
              .getOrdinal(),
          DogGeneTrait.COAT.rollCarried(UnleashedDogBreed.SHIBA_INU, bound -> fixedRoll));
    }
  }

  @Test
  @DisplayName("a carried roll can land on a coat the wild never shows")
  void carriedRollReachesBreedingOnlyCoats() {
    assertFalse(
        DogRarityClassifier.naturalChancePercent(
                UnleashedDogBreed.SHIBA_INU, ShibaInuCoat.SESAME.ordinal())
            > 0);
    assertEquals(
        ShibaInuCoat.SESAME.ordinal(),
        DogGeneTrait.COAT.rollCarried(UnleashedDogBreed.SHIBA_INU, bound -> bound - 1));
  }

  @Test
  @DisplayName("each trait reads and writes its own part of the appearance")
  void traitsReadAndWriteTheirOwnAppearanceField() {
    final DogAppearance appearance = new DogAppearance(UnleashedDogBreed.HUSKY, 1, 2);
    assertEquals(1, DogGeneTrait.COAT.shownIn(appearance));
    assertEquals(2, DogGeneTrait.EYES.shownIn(appearance));
    assertEquals(
        new DogAppearance(UnleashedDogBreed.HUSKY, 4, 2),
        DogGeneTrait.COAT.withShown(appearance, 4));
    assertEquals(
        new DogAppearance(UnleashedDogBreed.HUSKY, 1, 3),
        DogGeneTrait.EYES.withShown(appearance, 3));
  }

  @Test
  @DisplayName("allele names use the coat and eye translation keys")
  void alleleTranslationKeys() {
    assertEquals(
        HuskyCoat.SABLE.translationKey(),
        DogGeneTrait.COAT.alleleTranslationKey(UnleashedDogBreed.HUSKY, HuskyCoat.SABLE.ordinal()));
    assertEquals(
        "eyes.dogs-unleashed.husky.blue_hazel",
        DogGeneTrait.EYES.alleleTranslationKey(
            UnleashedDogBreed.HUSKY, HuskyEyeColor.BLUE_HAZEL.ordinal()));
    assertTrue(DogGeneTrait.COAT.translationKey().startsWith("gene.dogs-unleashed."));
  }
}
