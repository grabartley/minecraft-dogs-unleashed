package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.variant.DogRarityClassifier;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

final class DogTraitFormat {

  private DogTraitFormat() {}

  static String formatComposition(
      final List<BreedShare> composition, final Function<UnleashedDogBreed, String> breedName) {
    return composition.stream()
        .map(share -> breedName.apply(share.breed()) + " " + formatPercent(share.share()))
        .collect(Collectors.joining(", "));
  }

  static String formatPercent(final float share) {
    final float percent = share * 100.0f;
    if (percent < 1.0f) {
      return "<1%";
    }
    return Math.round(percent) + "%";
  }

  static String formatRarity(final String rarityName, final int chancePercent) {
    if (chancePercent >= DogRarityClassifier.GUARANTEED_CHANCE_PERCENT) {
      return rarityName;
    }
    return rarityName + " (" + chancePercent + "%)";
  }
}
