package com.grahambartley.dogsunleashed.spawner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.config.DireWolfSettings;
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

class DogSpawnRatesTest {

  private static final List<UnleashedDogBreed> NATURAL_BREEDS =
      Stream.of(UnleashedDogBreed.values()).filter(UnleashedDogBreed::isNaturallySpawning).toList();
  private static final List<UnleashedDogBreed> SNOWY_TAIGA =
      List.of(UnleashedDogBreed.HUSKY, UnleashedDogBreed.DIRE_WOLF);
  private static final int KEEP_ROLL_STEPS = 10_000;

  private static DogsUnleashedConfig config(
      final int globalPercent, final Map<String, Integer> breedPercents) {
    return new DogsUnleashedConfig(
        true,
        globalPercent,
        breedPercents,
        true,
        true,
        true,
        true,
        32,
        1.0f,
        1.5f,
        true,
        true,
        100,
        100,
        DireWolfSettings.DEFAULTS);
  }

  private static double expectedGroupsPerCycle(
      final UnleashedDogBreed breed,
      final List<UnleashedDogBreed> candidates,
      final DogsUnleashedConfig config) {
    final int tries = DogSpawnRates.triesPerCycle(NATURAL_BREEDS, config);
    final int totalBase = DogSpawnRates.totalBaseWeight(candidates);
    double expected = 0.0;
    for (int pick = 0; pick < totalBase; pick++) {
      if (DogSpawnRates.pickByBaseWeight(candidates, pick) != breed) {
        continue;
      }
      int kept = 0;
      for (int step = 0; step < KEEP_ROLL_STEPS; step++) {
        final double roll = (step + 0.5) / KEEP_ROLL_STEPS;
        if (DogSpawnRates.keeps(config.spawnRate(breed.serializedId()), tries, roll)) {
          kept++;
        }
      }
      expected += (double) kept / KEEP_ROLL_STEPS / totalBase;
    }
    return tries * expected;
  }

  static Stream<Arguments> rateCases() {
    return Stream.of(
        Arguments.of(UnleashedDogBreed.DIRE_WOLF, 100, Map.of("direwolf", 300), 3.0),
        Arguments.of(UnleashedDogBreed.DIRE_WOLF, 100, Map.of("direwolf", 50), 0.5),
        Arguments.of(UnleashedDogBreed.DIRE_WOLF, 100, Map.of("direwolf", 0), 0.0),
        Arguments.of(UnleashedDogBreed.DIRE_WOLF, 200, Map.of("direwolf", 150), 3.0),
        Arguments.of(UnleashedDogBreed.DIRE_WOLF, 500, Map.of(), 5.0),
        Arguments.of(UnleashedDogBreed.HUSKY, 100, Map.of("husky", 250), 2.5),
        Arguments.of(UnleashedDogBreed.HUSKY, 25, Map.of(), 0.25),
        Arguments.of(UnleashedDogBreed.DIRE_WOLF, 100, Map.of("husky", 0), 1.0),
        Arguments.of(UnleashedDogBreed.DIRE_WOLF, 100, Map.of("husky", 500), 1.0),
        Arguments.of(UnleashedDogBreed.HUSKY, 100, Map.of("beagle", 500), 1.0),
        Arguments.of(UnleashedDogBreed.HUSKY, 100, Map.of("direwolf", 400, "beagle", 300), 1.0));
  }

  @ParameterizedTest(name = "{0} at global {1}% and {2} spawns {3}x its default groups")
  @MethodSource("rateCases")
  @DisplayName("a breed's top-up groups per cycle scale with its own rate and nobody else's")
  void groupsPerCycleScaleWithTheBreedsOwnRate(
      final UnleashedDogBreed breed,
      final int globalPercent,
      final Map<String, Integer> breedPercents,
      final double multiple) {
    final double atDefault =
        expectedGroupsPerCycle(breed, SNOWY_TAIGA, DogsUnleashedConfig.defaults());
    final double atRate =
        expectedGroupsPerCycle(breed, SNOWY_TAIGA, config(globalPercent, breedPercents));
    assertEquals(multiple * atDefault, atRate, 1e-3);
  }

  @Test
  @DisplayName("at default rates every cycle makes one try and keeps every pick")
  void defaultsMakeOneTryAndKeepEveryPick() {
    assertEquals(1, DogSpawnRates.triesPerCycle(NATURAL_BREEDS, DogsUnleashedConfig.defaults()));
    assertTrue(DogSpawnRates.keeps(1.0, 1, 0.999));
  }

  static Stream<Arguments> tryCases() {
    return Stream.of(
        Arguments.of(100, Map.of(), 1),
        Arguments.of(300, Map.of(), 3),
        Arguments.of(100, Map.of("direwolf", 250), 3),
        Arguments.of(50, Map.of(), 1),
        Arguments.of(500, Map.of("husky", 500), 25),
        Arguments.of(0, Map.of(), 0));
  }

  @ParameterizedTest(name = "global {0}% and {1} -> {2} tries")
  @MethodSource("tryCases")
  @DisplayName("each cycle makes as many tries as the highest breed rate rounds up to")
  void triesPerCycleRoundUpTheHighestRate(
      final int globalPercent, final Map<String, Integer> breedPercents, final int expected) {
    assertEquals(
        expected,
        DogSpawnRates.triesPerCycle(NATURAL_BREEDS, config(globalPercent, breedPercents)));
  }

  @Test
  @DisplayName("a breed at 0% is never kept, and nothing is kept with no tries")
  void zeroRateOrNoTriesKeepsNothing() {
    assertFalse(DogSpawnRates.keeps(0.0, 1, 0.0));
    assertFalse(DogSpawnRates.keeps(1.0, 0, 0.0));
  }

  static Stream<Arguments> pickCases() {
    return Stream.of(
        Arguments.of(0, UnleashedDogBreed.HUSKY),
        Arguments.of(9, UnleashedDogBreed.HUSKY),
        Arguments.of(10, UnleashedDogBreed.DIRE_WOLF),
        Arguments.of(11, UnleashedDogBreed.DIRE_WOLF));
  }

  @ParameterizedTest(name = "roll {0} picks {1}")
  @MethodSource("pickCases")
  @DisplayName("pickByBaseWeight splits the roll range by base weight")
  void pickByBaseWeightSplitsTheRollRange(final int roll, final UnleashedDogBreed expected) {
    assertEquals(expected, DogSpawnRates.pickByBaseWeight(SNOWY_TAIGA, roll));
  }

  @Test
  @DisplayName("totalBaseWeight sums the base weight of every candidate")
  void totalBaseWeightSumsCandidates() {
    assertEquals(12, DogSpawnRates.totalBaseWeight(SNOWY_TAIGA));
  }
}
