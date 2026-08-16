package com.grahambartley.dogsunleashed.entity.genome;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.math.random.Random;

public final class DogGenomeCombiner {

  public static final float MUTATION_JITTER_RANGE = 0.05f;
  public static final int THROWBACK_CHANCE_PERCENT = 5;
  public static final float THROWBACK_NUDGE_SHARE = 0.15f;
  private static final int PERCENT_BOUND = 100;

  private DogGenomeCombiner() {}

  public static DogGenome combine(
      final DogGenome parentA, final DogGenome parentB, final Random random) {
    Map<UnleashedDogBreed, Double> shares =
        averageShares(parentA.composition(), parentB.composition());
    if (random.nextInt(PERCENT_BOUND) < THROWBACK_CHANCE_PERCENT) {
      shares = nudgeTowardThrowback(shares, pickThrowbackBreed(shares, random));
    }
    final List<BreedShare> composition = DogGenome.compositionOf(shares);
    final UnleashedDogBreed voiceBreed =
        random.nextBoolean() ? parentA.voiceBreed() : parentB.voiceBreed();
    return new DogGenome(
        composition,
        jitter(weightedStat(shares, attributes -> attributes.maxHealth()), random),
        jitter(weightedStat(shares, attributes -> attributes.movementSpeed()), random),
        jitter(weightedStat(shares, attributes -> attributes.attackDamage()), random),
        voiceBreed);
  }

  private static Map<UnleashedDogBreed, Double> averageShares(
      final List<BreedShare> parentA, final List<BreedShare> parentB) {
    final Map<UnleashedDogBreed, Double> averaged = new EnumMap<>(UnleashedDogBreed.class);
    for (final List<BreedShare> parent : List.of(parentA, parentB)) {
      for (final BreedShare share : parent) {
        averaged.merge(share.breed(), share.share() / 2.0, Double::sum);
      }
    }
    return averaged;
  }

  private static UnleashedDogBreed pickThrowbackBreed(
      final Map<UnleashedDogBreed, Double> shares, final Random random) {
    final List<UnleashedDogBreed> ancestors = shares.keySet().stream().sorted().toList();
    return ancestors.get(random.nextInt(ancestors.size()));
  }

  private static Map<UnleashedDogBreed, Double> nudgeTowardThrowback(
      final Map<UnleashedDogBreed, Double> shares, final UnleashedDogBreed throwbackBreed) {
    final Map<UnleashedDogBreed, Double> nudged = new EnumMap<>(UnleashedDogBreed.class);
    for (final Map.Entry<UnleashedDogBreed, Double> entry : shares.entrySet()) {
      nudged.put(entry.getKey(), entry.getValue() * (1.0 - THROWBACK_NUDGE_SHARE));
    }
    nudged.merge(throwbackBreed, (double) THROWBACK_NUDGE_SHARE, Double::sum);
    return nudged;
  }

  private static double weightedStat(
      final Map<UnleashedDogBreed, Double> shares, final StatSelector selector) {
    double weighted = 0.0;
    for (final Map.Entry<UnleashedDogBreed, Double> entry : shares.entrySet()) {
      weighted += selector.statOf(entry.getKey().attributes()) * entry.getValue();
    }
    return weighted;
  }

  private static double jitter(final double stat, final Random random) {
    final double factor = 1.0 + (random.nextDouble() * 2.0 - 1.0) * MUTATION_JITTER_RANGE;
    return stat * factor;
  }

  private interface StatSelector {
    double statOf(UnleashedDogBreed.Attributes attributes);
  }
}
