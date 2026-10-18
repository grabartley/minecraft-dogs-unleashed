package com.grahambartley.dogsunleashed.spawner;

import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.List;

public final class DogSpawnRates {

  private DogSpawnRates() {}

  public static int triesPerCycle(
      final List<UnleashedDogBreed> breeds, final DogsUnleashedConfig config) {
    double highestRate = 0.0;
    for (final UnleashedDogBreed breed : breeds) {
      highestRate = Math.max(highestRate, config.spawnRate(breed.serializedId()));
    }
    return (int) Math.ceil(highestRate);
  }

  public static boolean keeps(final double rate, final int tries, final double roll) {
    return tries > 0 && roll < rate / tries;
  }

  public static int totalBaseWeight(final List<UnleashedDogBreed> breeds) {
    int total = 0;
    for (final UnleashedDogBreed breed : breeds) {
      total += breed.spawnSettings().weight();
    }
    return total;
  }

  public static UnleashedDogBreed pickByBaseWeight(
      final List<UnleashedDogBreed> candidates, final int roll) {
    int remaining = roll;
    for (final UnleashedDogBreed breed : candidates) {
      remaining -= breed.spawnSettings().weight();
      if (remaining < 0) {
        return breed;
      }
    }
    return candidates.get(candidates.size() - 1);
  }
}
