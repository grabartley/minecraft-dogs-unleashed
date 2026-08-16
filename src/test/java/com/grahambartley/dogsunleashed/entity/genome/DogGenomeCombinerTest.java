package com.grahambartley.dogsunleashed.entity.genome;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.util.math.random.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class DogGenomeCombinerTest {

  private static final double SHARE_DELTA = 1e-6;
  private static final double STAT_ENVELOPE_DELTA = 1e-9;

  static Stream<Long> seeds() {
    return IntStream.range(0, 50).mapToObj(Long::valueOf);
  }

  private static DogGenome huskyBeagleChild(final long seed) {
    return DogGenomeCombiner.combine(
        DogGenome.pure(UnleashedDogBreed.HUSKY),
        DogGenome.pure(UnleashedDogBreed.BEAGLE),
        Random.create(seed));
  }

  @ParameterizedTest(name = "seed {0}")
  @MethodSource("seeds")
  @DisplayName("two pure parents produce a half-half composition modulo the throwback nudge")
  void pureParentsAverageToHalvesModuloThrowback(final long seed) {
    final DogGenome child = huskyBeagleChild(seed);
    assertEquals(
        Set.of(UnleashedDogBreed.HUSKY, UnleashedDogBreed.BEAGLE),
        Set.copyOf(child.composition().stream().map(BreedShare::breed).toList()));
    final double huskyShare = shareOf(child, UnleashedDogBreed.HUSKY);
    assertTrue(
        isOneOf(huskyShare, 0.5, 0.5 * 0.85, 0.5 * 0.85 + 0.15),
        "husky share must be the plain average or a throwback-nudged value, was " + huskyShare);
    assertEquals(
        1.0, child.composition().stream().mapToDouble(BreedShare::share).sum(), SHARE_DELTA);
  }

  @ParameterizedTest(name = "seed {0}")
  @MethodSource("seeds")
  @DisplayName("child stats stay within the mutation envelope of the composition-weighted average")
  void childStatsStayWithinMutationEnvelope(final long seed) {
    final DogGenome child = huskyBeagleChild(seed);
    assertWithinEnvelope(child.maxHealth(), weightedStat(child, StatKind.MAX_HEALTH), "max health");
    assertWithinEnvelope(
        child.movementSpeed(), weightedStat(child, StatKind.MOVEMENT_SPEED), "movement speed");
    assertWithinEnvelope(
        child.attackDamage(), weightedStat(child, StatKind.ATTACK_DAMAGE), "attack damage");
  }

  @ParameterizedTest(name = "seed {0}")
  @MethodSource("seeds")
  @DisplayName("the voice gene comes from one of the two parents")
  void voiceGeneComesFromAParent(final long seed) {
    final DogGenome crossParent =
        new DogGenome(
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.5f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f)),
            21.0,
            0.295,
            4.0,
            UnleashedDogBreed.DACHSHUND);
    final DogGenome child =
        DogGenomeCombiner.combine(
            crossParent, DogGenome.pure(UnleashedDogBreed.SHIBA_INU), Random.create(seed));
    assertTrue(
        child.voiceBreed() == UnleashedDogBreed.DACHSHUND
            || child.voiceBreed() == UnleashedDogBreed.SHIBA_INU,
        "voice must come from a parent genome, was " + child.voiceBreed());
  }

  @ParameterizedTest(name = "seed {0}")
  @MethodSource("seeds")
  @DisplayName("a cross-breed parent contributes its stored composition exactly")
  void crossBreedParentContributesStoredComposition(final long seed) {
    final DogGenome crossParent =
        new DogGenome(
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.5f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.5f)),
            21.0,
            0.295,
            4.0,
            UnleashedDogBreed.HUSKY);
    final DogGenome child =
        DogGenomeCombiner.combine(
            crossParent, DogGenome.pure(UnleashedDogBreed.SHIBA_INU), Random.create(seed));
    assertTrue(
        isOneOf(shareOf(child, UnleashedDogBreed.HUSKY), 0.25, 0.25 * 0.85, 0.25 * 0.85 + 0.15),
        "husky share must derive from the stored quarter");
    assertTrue(
        isOneOf(shareOf(child, UnleashedDogBreed.SHIBA_INU), 0.5, 0.5 * 0.85, 0.5 * 0.85 + 0.15),
        "shiba share must derive from the pure half");
    assertEquals(
        1.0, child.composition().stream().mapToDouble(BreedShare::share).sum(), SHARE_DELTA);
  }

  @ParameterizedTest(name = "seed {0}")
  @MethodSource("seeds")
  @DisplayName("combination is deterministic for a given seed")
  void combinationIsDeterministicForSeed(final long seed) {
    assertEquals(huskyBeagleChild(seed), huskyBeagleChild(seed));
  }

  private static double shareOf(final DogGenome genome, final UnleashedDogBreed breed) {
    return genome.composition().stream()
        .filter(share -> share.breed() == breed)
        .mapToDouble(BreedShare::share)
        .sum();
  }

  private static boolean isOneOf(final double actual, final double... candidates) {
    for (final double candidate : candidates) {
      if (Math.abs(actual - candidate) <= SHARE_DELTA) {
        return true;
      }
    }
    return false;
  }

  private enum StatKind {
    MAX_HEALTH,
    MOVEMENT_SPEED,
    ATTACK_DAMAGE
  }

  private static double weightedStat(final DogGenome genome, final StatKind kind) {
    return genome.composition().stream()
        .mapToDouble(
            share -> {
              final UnleashedDogBreed.Attributes attributes = share.breed().attributes();
              final double stat =
                  switch (kind) {
                    case MAX_HEALTH -> attributes.maxHealth();
                    case MOVEMENT_SPEED -> attributes.movementSpeed();
                    case ATTACK_DAMAGE -> attributes.attackDamage();
                  };
              return stat * share.share();
            })
        .sum();
  }

  private static void assertWithinEnvelope(
      final double actual, final double weighted, final String label) {
    final double jitter = DogGenomeCombiner.MUTATION_JITTER_RANGE;
    assertTrue(
        actual >= weighted * (1.0 - jitter) - STAT_ENVELOPE_DELTA
            && actual <= weighted * (1.0 + jitter) + STAT_ENVELOPE_DELTA,
        label + " must be within ±" + jitter + " of " + weighted + ", was " + actual);
  }
}
