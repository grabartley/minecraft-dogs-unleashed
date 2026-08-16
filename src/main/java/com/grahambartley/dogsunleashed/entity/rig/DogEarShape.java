package com.grahambartley.dogsunleashed.entity.rig;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

/**
 * Ear geometry is a discrete feature: a cross inherits one ancestor's ear shape whole rather than
 * an average of both, the same way eyes and nose are stamped rather than blended. Colour is not
 * carried across with the shape, the inherited ears are painted with the dog's own blended pigment.
 *
 * <p>Each shape is a pair of variant bones under the animated {@code ear1} and {@code ear2} pivots.
 * Exactly one pair is shown per dog and the rest are hidden.
 */
public enum DogEarShape {
  GOLDEN_RETRIEVER(UnleashedDogBreed.GOLDEN_RETRIEVER),
  BEAGLE(UnleashedDogBreed.BEAGLE);

  private final UnleashedDogBreed breed;

  DogEarShape(final UnleashedDogBreed breed) {
    this.breed = breed;
  }

  public UnleashedDogBreed breed() {
    return this.breed;
  }

  public String boneName(final String earPivot) {
    return earPivot + "_" + this.breed.serializedId();
  }

  public static @Nullable DogEarShape of(final UnleashedDogBreed breed) {
    for (final DogEarShape shape : values()) {
      if (shape.breed == breed) {
        return shape;
      }
    }
    return null;
  }

  /**
   * Picks one ancestor's ears, weighted by how much of the dog that ancestor makes up. The dog's id
   * seeds the choice so a given dog always has the same ears.
   */
  public static DogEarShape inherit(final List<BreedShare> composition, final UUID dogId) {
    float total = 0;
    for (final BreedShare share : composition) {
      if (of(share.breed()) != null && share.share() > 0) {
        total += share.share();
      }
    }
    if (total <= 0) {
      return GOLDEN_RETRIEVER;
    }

    final float roll = rollOf(dogId) * total;
    float cumulative = 0;
    DogEarShape last = GOLDEN_RETRIEVER;
    for (final BreedShare share : composition) {
      final DogEarShape shape = of(share.breed());
      if (shape == null || share.share() <= 0) {
        continue;
      }
      last = shape;
      cumulative += share.share();
      if (roll < cumulative) {
        return shape;
      }
    }
    return last;
  }

  private static float rollOf(final @Nullable UUID dogId) {
    if (dogId == null) {
      return 0.0f;
    }
    long bits =
        dogId.getMostSignificantBits() * 0x9E3779B97F4A7C15L ^ dogId.getLeastSignificantBits();
    bits ^= bits >>> 30;
    bits *= 0xBF58476D1CE4E5B9L;
    bits ^= bits >>> 27;
    bits *= 0x94D049BB133111EBL;
    bits ^= bits >>> 31;
    return (float) ((bits >>> 11) / (double) (1L << 53));
  }
}
