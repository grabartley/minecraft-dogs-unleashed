package com.grahambartley.dogsunleashed.entity.rig;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

/**
 * Breeds normalised onto the shared skeleton in {@code geo/dog.geo.json}. Breeds outside this set
 * still render from their own rig, animation set and static coat texture.
 */
public final class DogRig {

  public static final String MODEL_ID = "dog";

  private static final Set<UnleashedDogBreed> SUPPORTED =
      Set.of(UnleashedDogBreed.GOLDEN_RETRIEVER, UnleashedDogBreed.BEAGLE);

  private DogRig() {}

  public static boolean supports(final UnleashedDogBreed breed) {
    return SUPPORTED.contains(breed);
  }

  public static boolean supports(final @Nullable DogGenome genome, final UnleashedDogBreed breed) {
    if (genome == null) {
      return supports(breed);
    }
    return supports(genome.composition());
  }

  public static boolean supports(final List<BreedShare> composition) {
    return !composition.isEmpty()
        && composition.stream().allMatch(share -> supports(share.breed()));
  }
}
