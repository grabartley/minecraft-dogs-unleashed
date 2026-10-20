package com.grahambartley.dogsunleashed.spawner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StraySpawnRulesTest {

  @ParameterizedTest
  @CsvSource({
    "true, true, true, true",
    "false, true, true, false",
    "true, false, true, false",
    "true, true, false, false",
    "false, false, false, false"
  })
  void anAttemptNeedsTheSettingAnimalSpawningAndTheGamerule(
      final boolean villageStrays,
      final boolean spawnAnimals,
      final boolean doMobSpawning,
      final boolean expected) {
    assertEquals(expected, StraySpawnRules.canAttempt(villageStrays, spawnAnimals, doMobSpawning));
  }

  @ParameterizedTest
  @CsvSource({"0, false", "4, false", "5, true", "20, true"})
  void aVillageNeedsMoreThanFourOccupiedHomes(final long homes, final boolean village) {
    assertEquals(village, StraySpawnRules.isVillage(homes));
  }

  @ParameterizedTest
  @CsvSource({"0, true", "1, true", "2, false", "3, false"})
  void aVillageHoldsAtMostTwoStrays(final int strays, final boolean room) {
    assertEquals(room, StraySpawnRules.hasRoom(strays));
  }

  @ParameterizedTest
  @CsvSource({
    "0, false, 8",
    "0, true, -8",
    "24, false, 32",
    "24, true, -32",
    "25, false, 8",
    "-1, false, 32",
    "10, true, -18"
  })
  void offsetsStayBetweenEightAndThirtyTwoBlocks(
      final int roll, final boolean negative, final int expected) {
    assertEquals(expected, StraySpawnRules.offset(roll, negative));
  }

  @ParameterizedTest
  @CsvSource({
    "snow, HUSKY",
    "plains, DACHSHUND",
    "taiga, BEAGLE",
    "desert, SHIBA_INU",
    "savanna, SHIBA_INU",
    "jungle, GOLDEN_RETRIEVER",
    "swamp, GOLDEN_RETRIEVER",
    "unknown, GOLDEN_RETRIEVER"
  })
  void theVillageBiomePicksTheBreed(final String villageType, final UnleashedDogBreed breed) {
    assertEquals(breed, StraySpawnRules.breedFor(villageType));
  }
}
