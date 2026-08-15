package com.grahambartley.dogsunleashed.entity.variant;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.function.BiFunction;
import net.minecraft.entity.SpawnReason;

public final class DogRarityClassifier {

  public static final int GUARANTEED_CHANCE_PERCENT = 100;
  static final int COMMON_MIN_CHANCE_PERCENT = 25;
  static final int UNCOMMON_MIN_CHANCE_PERCENT = 10;
  static final int RARE_MIN_CHANCE_PERCENT = 3;

  private DogRarityClassifier() {}

  public static boolean hasCoatVariants(final UnleashedDogBreed breed) {
    return DogCoats.hasCoatVariants(breed);
  }

  public static int chancePercent(final UnleashedDogBreed breed, final int coatOrdinal) {
    final BiFunction<SpawnReason, Integer, UnleashedDogCoat> resolver =
        DogCoats.rollResolverFor(breed);
    if (resolver == null || coatOrdinal < 0) {
      return GUARANTEED_CHANCE_PERCENT;
    }
    final int naturalChance = countRolls(resolver, SpawnReason.NATURAL, coatOrdinal);
    final int breedingChance = countRolls(resolver, SpawnReason.BREEDING, coatOrdinal);
    return Math.max(naturalChance, breedingChance);
  }

  public static DogRarity classify(final int chancePercent) {
    if (chancePercent >= COMMON_MIN_CHANCE_PERCENT) {
      return DogRarity.COMMON;
    }
    if (chancePercent >= UNCOMMON_MIN_CHANCE_PERCENT) {
      return DogRarity.UNCOMMON;
    }
    if (chancePercent >= RARE_MIN_CHANCE_PERCENT) {
      return DogRarity.RARE;
    }
    return DogRarity.EPIC;
  }

  public static DogRarity rarityOf(final UnleashedDogBreed breed, final int coatOrdinal) {
    return classify(chancePercent(breed, coatOrdinal));
  }

  private static int countRolls(
      final BiFunction<SpawnReason, Integer, UnleashedDogCoat> resolver,
      final SpawnReason spawnReason,
      final int coatOrdinal) {
    int matches = 0;
    for (int roll = 0; roll < DogCoats.ROLL_BOUND; roll++) {
      if (resolver.apply(spawnReason, roll).getOrdinal() == coatOrdinal) {
        matches++;
      }
    }
    return matches;
  }
}
