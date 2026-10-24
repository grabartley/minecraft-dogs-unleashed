package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed.FurProfile;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.OptionalInt;
import org.jetbrains.annotations.Nullable;

public final class DogFurRules {

  public static final int PERCENT = 100;
  public static final int SPAWN_NATURAL_PERCENT = 70;
  public static final int SPAWN_SHAGGY_PERCENT = 25;
  public static final int BOTCHED_YIELD_MULTIPLIER = 2;

  private DogFurRules() {}

  public static FurProfile profileFor(
      final UnleashedDogBreed breed, final @Nullable DogGenome genome) {
    if (genome == null) {
      return breed.furProfile();
    }
    double maxStage = 0.0;
    double baseTicksPerStage = 0.0;
    double yield = 0.0;
    double total = 0.0;
    for (final BreedShare share : genome.composition()) {
      final FurProfile ancestor = share.breed().furProfile();
      maxStage += ancestor.maxStage().ordinal() * share.share();
      baseTicksPerStage += ancestor.baseTicksPerStage() * share.share();
      yield += ancestor.shearYieldPerStage() * share.share();
      total += share.share();
    }
    if (total <= 0.0) {
      return breed.furProfile();
    }
    return new FurProfile(
        DogFurStage.fromOrdinal((int) Math.round(maxStage / total)),
        (int) Math.round(baseTicksPerStage / total),
        Math.max(1, (int) Math.round(yield / total)));
  }

  public static OptionalInt ticksPerStage(
      final FurProfile profile,
      final DogFurStage growingFrom,
      final double furGrowthRate,
      final int rateMultiplierPercent) {
    if (furGrowthRate <= 0.0 || rateMultiplierPercent <= 0) {
      return OptionalInt.empty();
    }
    final double ticks =
        profile.baseTicksPerStage()
            * growingFrom.growthTimePercent()
            / furGrowthRate
            / (double) rateMultiplierPercent;
    return OptionalInt.of((int) Math.max(1L, Math.round(ticks)));
  }

  public static int botchChancePercent(
      final DogFurStage shearedFrom, final int chanceMultiplierPercent) {
    final int scaled = shearedFrom.botchChancePercent() * Math.max(0, chanceMultiplierPercent);
    return Math.min(PERCENT, Math.round(scaled / (float) PERCENT));
  }

  public static boolean isBotched(
      final DogFurStage shearedFrom, final int chanceMultiplierPercent, final int rollPercent) {
    return rollPercent < botchChancePercent(shearedFrom, chanceMultiplierPercent);
  }

  public static int shearYield(
      final DogFurStage shearedFrom, final int yieldPerStage, final boolean botched) {
    final int full = shearedFrom.stagesAbove(DogFurStage.TRIMMED) * yieldPerStage;
    return botched ? full * BOTCHED_YIELD_MULTIPLIER : full;
  }

  public static DogFurStage spawnStage(final int rollPercent, final DogFurStage maxStage) {
    final DogFurStage rolled;
    if (rollPercent < SPAWN_NATURAL_PERCENT) {
      rolled = DogFurStage.NATURAL;
    } else if (rollPercent < SPAWN_NATURAL_PERCENT + SPAWN_SHAGGY_PERCENT) {
      rolled = DogFurStage.SHAGGY;
    } else {
      rolled = DogFurStage.OVERGROWN;
    }
    return rolled.atMost(maxStage);
  }
}
