package com.grahambartley.dogsunleashed.spawner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DogSpawnerTest {

  private static final List<UnleashedDogBreed> ALL_BREEDS = List.of(UnleashedDogBreed.values());

  private static DogsUnleashedConfig configWithBreedRates(final Map<String, Integer> breedRates) {
    return new DogsUnleashedConfig(true, 100, breedRates, true, true, true, 32, 1.0f, 1.5f);
  }

  @Test
  @DisplayName("totalBaseWeight sums the base spawn weight of every candidate")
  void totalBaseWeightSumsCandidates() {
    final int expected =
        ALL_BREEDS.stream().mapToInt(breed -> breed.spawnSettings().weight()).sum();
    assertEquals(expected, DogSpawner.totalBaseWeight(ALL_BREEDS));
  }

  @Test
  @DisplayName("totalEffectiveWeight equals totalBaseWeight at default multipliers")
  void totalEffectiveWeightMatchesBaseAtDefaults() {
    assertEquals(
        DogSpawner.totalBaseWeight(ALL_BREEDS),
        DogSpawner.totalEffectiveWeight(ALL_BREEDS, DogsUnleashedConfig.defaults()));
  }

  @Test
  @DisplayName("totalEffectiveWeight applies per-breed multipliers")
  void totalEffectiveWeightAppliesMultipliers() {
    final DogsUnleashedConfig config =
        configWithBreedRates(Map.of("husky", 300, "dachshund", 0, "beagle", 50));
    final List<UnleashedDogBreed> candidates =
        List.of(UnleashedDogBreed.HUSKY, UnleashedDogBreed.DACHSHUND, UnleashedDogBreed.BEAGLE);
    assertEquals(30 + 0 + 5, DogSpawner.totalEffectiveWeight(candidates, config));
  }

  static Stream<Arguments> defaultWeightPickCases() {
    return Stream.of(
        Arguments.of(0, UnleashedDogBreed.HUSKY),
        Arguments.of(9, UnleashedDogBreed.HUSKY),
        Arguments.of(10, UnleashedDogBreed.DACHSHUND),
        Arguments.of(19, UnleashedDogBreed.DACHSHUND),
        Arguments.of(20, UnleashedDogBreed.BEAGLE),
        Arguments.of(30, UnleashedDogBreed.GOLDEN_RETRIEVER),
        Arguments.of(40, UnleashedDogBreed.SHIBA_INU),
        Arguments.of(49, UnleashedDogBreed.SHIBA_INU));
  }

  @ParameterizedTest(name = "roll {0} picks {1}")
  @MethodSource("defaultWeightPickCases")
  @DisplayName("pickWeighted partitions the roll range by breed weight at default multipliers")
  void pickWeightedPartitionsRollRangeAtDefaults(final int roll, final UnleashedDogBreed expected) {
    assertEquals(
        expected, DogSpawner.pickWeighted(ALL_BREEDS, DogsUnleashedConfig.defaults(), roll));
  }

  static Stream<Arguments> boostedWeightPickCases() {
    return Stream.of(
        Arguments.of(0, UnleashedDogBreed.HUSKY),
        Arguments.of(29, UnleashedDogBreed.HUSKY),
        Arguments.of(30, UnleashedDogBreed.DACHSHUND),
        Arguments.of(39, UnleashedDogBreed.DACHSHUND));
  }

  @ParameterizedTest(name = "roll {0} picks {1}")
  @MethodSource("boostedWeightPickCases")
  @DisplayName("pickWeighted widens a breed's share of the roll range with its multiplier")
  void pickWeightedWidensBoostedBreedShare(final int roll, final UnleashedDogBreed expected) {
    final DogsUnleashedConfig config = configWithBreedRates(Map.of("husky", 300));
    final List<UnleashedDogBreed> candidates =
        List.of(UnleashedDogBreed.HUSKY, UnleashedDogBreed.DACHSHUND);
    assertEquals(expected, DogSpawner.pickWeighted(candidates, config, roll));
  }

  @Test
  @DisplayName("pickWeighted with a single candidate always picks it")
  void pickWeightedSingleCandidate() {
    assertEquals(
        UnleashedDogBreed.SHIBA_INU,
        DogSpawner.pickWeighted(
            List.of(UnleashedDogBreed.SHIBA_INU), DogsUnleashedConfig.defaults(), 0));
  }
}
