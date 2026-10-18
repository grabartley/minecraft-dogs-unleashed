package com.grahambartley.dogsunleashed.entity.genome;

import com.grahambartley.dogsunleashed.entity.DogAppearance;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.variant.DogCoats;
import com.grahambartley.dogsunleashed.entity.variant.DogRarityClassifier;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import com.grahambartley.dogsunleashed.entity.variant.UnleashedDogCoat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntUnaryOperator;
import net.minecraft.entity.SpawnReason;

public enum DogGeneTrait {
  COAT,
  EYES;

  private final Map<UnleashedDogBreed, GeneDominance> dominanceByBreed = new ConcurrentHashMap<>();

  public String serializedId() {
    return this.name().toLowerCase(Locale.ROOT);
  }

  public String translationKey() {
    return "gene.dogs-unleashed." + this.serializedId();
  }

  public boolean appliesTo(final UnleashedDogBreed breed) {
    return switch (this) {
      case COAT -> DogCoats.hasCoatVariants(breed);
      case EYES -> breed.hasEyeColorVariants();
    };
  }

  public GeneDominance dominance(final UnleashedDogBreed breed) {
    return this.dominanceByBreed.computeIfAbsent(breed, this::computeDominance);
  }

  private GeneDominance computeDominance(final UnleashedDogBreed breed) {
    return switch (this) {
      case COAT -> coatDominance(breed);
      case EYES -> eyeDominance();
    };
  }

  public String alleleTranslationKey(final UnleashedDogBreed breed, final int allele) {
    return switch (this) {
      case COAT -> DogCoats.coatsOf(breed).get(allele).translationKey();
      case EYES -> HuskyEyeColor.fromOrdinal(allele).translationKey();
    };
  }

  public int rollShown(
      final UnleashedDogBreed breed,
      final SpawnReason spawnReason,
      final IntUnaryOperator nextInt) {
    return switch (this) {
      case COAT -> rollCoat(breed, spawnReason, nextInt);
      case EYES -> nextInt.applyAsInt(HuskyEyeColor.values().length);
    };
  }

  public int rollCarried(final UnleashedDogBreed breed, final IntUnaryOperator nextInt) {
    return rollShown(breed, SpawnReason.BREEDING, nextInt);
  }

  public int shownIn(final DogAppearance appearance) {
    return switch (this) {
      case COAT -> appearance.coatVariantOrdinal();
      case EYES -> appearance.eyeColorVariantOrdinal();
    };
  }

  public DogAppearance withShown(final DogAppearance appearance, final int allele) {
    return switch (this) {
      case COAT ->
          new DogAppearance(
              appearance.rigSourceBreed(), allele, appearance.eyeColorVariantOrdinal());
      case EYES ->
          new DogAppearance(appearance.rigSourceBreed(), appearance.coatVariantOrdinal(), allele);
    };
  }

  private static int rollCoat(
      final UnleashedDogBreed breed,
      final SpawnReason spawnReason,
      final IntUnaryOperator nextInt) {
    return DogCoats.rollResolverFor(breed)
        .apply(spawnReason, nextInt.applyAsInt(DogCoats.ROLL_BOUND))
        .getOrdinal();
  }

  private static GeneDominance coatDominance(final UnleashedDogBreed breed) {
    final List<UnleashedDogCoat> coats = DogCoats.coatsOf(breed);
    final Set<Integer> recessive = new HashSet<>();
    for (final UnleashedDogCoat coat : coats) {
      if (DogRarityClassifier.naturalChancePercent(breed, coat.getOrdinal())
          < DogRarityClassifier.UNCOMMON_MIN_CHANCE_PERCENT) {
        recessive.add(coat.getOrdinal());
      }
    }
    final int wildType =
        coats.stream()
            .max(
                Comparator.comparingInt(
                    (UnleashedDogCoat coat) ->
                        DogRarityClassifier.naturalChancePercent(breed, coat.getOrdinal())))
            .orElseThrow()
            .getOrdinal();
    return new GeneDominance(coats.size(), recessive, wildType);
  }

  private static GeneDominance eyeDominance() {
    final Set<Integer> recessive = new HashSet<>();
    for (final HuskyEyeColor eyes : HuskyEyeColor.values()) {
      if (eyes.heterochromatic()) {
        recessive.add(eyes.ordinal());
      }
    }
    return new GeneDominance(
        HuskyEyeColor.values().length, recessive, HuskyEyeColor.HAZEL_HAZEL.ordinal());
  }
}
