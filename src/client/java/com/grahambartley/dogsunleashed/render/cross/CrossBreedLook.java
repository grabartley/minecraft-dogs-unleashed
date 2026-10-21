package com.grahambartley.dogsunleashed.render.cross;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;

public record CrossBreedLook(List<BreedShare> composition, long seed) {

  public record Influence(UnleashedDogBreed breed, float weight) {}

  private static final float SHARE_STEPS = 1000.0f;

  public CrossBreedLook {
    composition = DogGenome.sortedComposition(composition);
  }

  public static CrossBreedLook of(final List<BreedShare> composition, final int coatVariant) {
    long seed = mix(coatVariant);
    for (final BreedShare share : DogGenome.sortedComposition(composition)) {
      seed = mix(seed ^ share.breed().serializedId().hashCode());
      seed = mix(seed ^ Math.round(share.share() * SHARE_STEPS));
    }
    return new CrossBreedLook(composition, seed);
  }

  public UnleashedDogBreed rig() {
    return this.composition.get(0).breed();
  }

  public boolean isPure() {
    return this.influences().isEmpty();
  }

  public List<Influence> influences() {
    final float total = this.totalShare();
    return this.composition.stream()
        .skip(1)
        .filter(share -> share.share() > 0.0f && share.breed() != this.rig())
        .map(share -> new Influence(share.breed(), share.share() / total))
        .toList();
  }

  public UnleashedDogBreed earSource() {
    final float total = this.totalShare();
    final float roll = unit(mix(this.seed ^ 0x45415253L)) * total;
    float reached = 0.0f;
    for (final BreedShare share : this.composition) {
      reached += Math.max(0.0f, share.share());
      if (roll < reached) {
        return share.breed();
      }
    }
    return this.rig();
  }

  public int coatIndexFor(final UnleashedDogBreed breed, final int coatCount) {
    if (coatCount <= 0) {
      return 0;
    }
    return (int) Math.floorMod(mix(this.seed ^ breed.serializedId().hashCode()), coatCount);
  }

  private float totalShare() {
    final float total =
        (float) this.composition.stream().mapToDouble(share -> Math.max(0.0f, share.share())).sum();
    return total > 0.0f ? total : 1.0f;
  }

  private static float unit(final long hash) {
    return (hash >>> 40) / (float) (1L << 24);
  }

  private static long mix(final long value) {
    long z = value + 0x9E3779B97F4A7C15L;
    z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
    z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
    return z ^ (z >>> 31);
  }
}
