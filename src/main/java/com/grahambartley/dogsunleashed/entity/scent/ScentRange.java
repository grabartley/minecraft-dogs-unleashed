package com.grahambartley.dogsunleashed.entity.scent;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;

public final class ScentRange {

  private ScentRange() {}

  public static int of(final UnleashedDogBreed breed, final List<BreedShare> composition) {
    double weighted = 0.0;
    double total = 0.0;
    for (final BreedShare share : composition) {
      weighted += share.share() * share.breed().nature().scentRange();
      total += share.share();
    }
    return total > 0.0 ? (int) Math.round(weighted / total) : breed.nature().scentRange();
  }
}
