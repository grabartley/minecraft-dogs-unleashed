package com.grahambartley.dogsunleashed.pet;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;

public final class BreedComposition {

  static final int MAX_GENERATIONS = 8;

  private BreedComposition() {}

  public record BreedShare(UnleashedDogBreed breed, float share) {}

  public static List<BreedShare> compute(
      final UUID dogId,
      final UnleashedDogBreed fallbackBreed,
      final Function<UUID, PetData> lookup) {
    final Map<UnleashedDogBreed, Double> shares = sharesOf(dogId, lookup, MAX_GENERATIONS);
    final Map<UnleashedDogBreed, Double> resolved =
        shares != null ? shares : pureShares(fallbackBreed);
    return resolved.entrySet().stream()
        .map(entry -> new BreedShare(entry.getKey(), entry.getValue().floatValue()))
        .sorted(
            Comparator.comparingDouble((BreedShare share) -> -share.share())
                .thenComparing(share -> share.breed().serializedId()))
        .toList();
  }

  private static @Nullable Map<UnleashedDogBreed, Double> sharesOf(
      final @Nullable UUID dogId,
      final Function<UUID, PetData> lookup,
      final int remainingGenerations) {
    if (dogId == null) {
      return null;
    }
    final PetData pet = lookup.apply(dogId);
    if (pet == null) {
      return null;
    }
    if (remainingGenerations == 0 || (pet.getParentAId() == null && pet.getParentBId() == null)) {
      return pureShares(pet.getBreed());
    }
    final Map<UnleashedDogBreed, Double> parentA =
        sharesOf(pet.getParentAId(), lookup, remainingGenerations - 1);
    final Map<UnleashedDogBreed, Double> parentB =
        sharesOf(pet.getParentBId(), lookup, remainingGenerations - 1);
    final Map<UnleashedDogBreed, Double> fallback = pureShares(pet.getBreed());
    return averageOf(parentA != null ? parentA : fallback, parentB != null ? parentB : fallback);
  }

  private static Map<UnleashedDogBreed, Double> pureShares(final UnleashedDogBreed breed) {
    final Map<UnleashedDogBreed, Double> shares = new EnumMap<>(UnleashedDogBreed.class);
    shares.put(breed, 1.0);
    return shares;
  }

  private static Map<UnleashedDogBreed, Double> averageOf(
      final Map<UnleashedDogBreed, Double> parentA, final Map<UnleashedDogBreed, Double> parentB) {
    final Map<UnleashedDogBreed, Double> averaged = new EnumMap<>(UnleashedDogBreed.class);
    for (final Map<UnleashedDogBreed, Double> parent : List.of(parentA, parentB)) {
      for (final Map.Entry<UnleashedDogBreed, Double> entry : parent.entrySet()) {
        averaged.merge(entry.getKey(), entry.getValue() / 2.0, Double::sum);
      }
    }
    return averaged;
  }

  public static List<BreedShare> pureComposition(final UnleashedDogBreed breed) {
    return List.of(new BreedShare(breed, 1.0f));
  }
}
