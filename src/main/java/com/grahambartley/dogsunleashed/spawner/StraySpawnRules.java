package com.grahambartley.dogsunleashed.spawner;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.Map;

public final class StraySpawnRules {

  public static final int COOLDOWN_TICKS = 1200;
  public static final int MIN_OFFSET_BLOCKS = 8;
  public static final int MAX_OFFSET_BLOCKS = 32;
  public static final int VILLAGE_RADIUS_BLOCKS = 48;
  public static final int VILLAGE_HEIGHT_BLOCKS = 8;
  public static final int MIN_OCCUPIED_HOMES = 5;
  public static final int STRAYS_PER_VILLAGE = 2;
  private static final Map<String, UnleashedDogBreed> BREED_BY_VILLAGE_TYPE =
      Map.of(
          "snow", UnleashedDogBreed.HUSKY,
          "plains", UnleashedDogBreed.DACHSHUND,
          "taiga", UnleashedDogBreed.BEAGLE,
          "desert", UnleashedDogBreed.SHIBA_INU,
          "savanna", UnleashedDogBreed.SHIBA_INU);

  private StraySpawnRules() {}

  public static boolean canAttempt(
      final boolean villageStrays, final boolean spawnAnimals, final boolean doMobSpawning) {
    return villageStrays && spawnAnimals && doMobSpawning;
  }

  public static boolean isVillage(final long occupiedHomes) {
    return occupiedHomes >= MIN_OCCUPIED_HOMES;
  }

  public static boolean hasRoom(final int straysNearby) {
    return straysNearby < STRAYS_PER_VILLAGE;
  }

  public static int offset(final int roll, final boolean negative) {
    final int magnitude =
        MIN_OFFSET_BLOCKS + Math.floorMod(roll, MAX_OFFSET_BLOCKS - MIN_OFFSET_BLOCKS + 1);
    return negative ? -magnitude : magnitude;
  }

  public static UnleashedDogBreed breedFor(final String villageType) {
    return BREED_BY_VILLAGE_TYPE.getOrDefault(villageType, UnleashedDogBreed.GOLDEN_RETRIEVER);
  }
}
