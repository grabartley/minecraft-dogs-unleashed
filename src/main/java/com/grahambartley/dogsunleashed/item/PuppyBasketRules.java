package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.List;
import java.util.stream.Stream;

public final class PuppyBasketRules {

  public static final List<UnleashedDogBreed> BREEDS =
      Stream.of(UnleashedDogBreed.values())
          .filter(UnleashedDogBreed::isNaturallySpawning)
          .filter(breed -> !breed.nature().tamesThroughTrust())
          .toList();

  private PuppyBasketRules() {}

  public static UnleashedDogBreed pick(final int roll) {
    return BREEDS.get(Math.floorMod(roll, BREEDS.size()));
  }
}
