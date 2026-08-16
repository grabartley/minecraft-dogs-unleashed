package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.variant.DogRarityClassifier;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

final class DogTraitFormat {

  static final float MEANINGFUL_SHARE_THRESHOLD = 0.25f;

  private DogTraitFormat() {}

  static List<UnleashedDogBreed> mixNameBreeds(final List<BreedShare> composition) {
    if (composition.size() < 2) {
      return List.of();
    }
    final long meaningfulShares =
        composition.stream().filter(share -> share.share() >= MEANINGFUL_SHARE_THRESHOLD).count();
    if (meaningfulShares >= 3) {
      return List.of();
    }
    final List<BreedShare> sorted = DogGenome.sortedComposition(composition);
    return List.of(sorted.get(0).breed(), sorted.get(1).breed());
  }

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
