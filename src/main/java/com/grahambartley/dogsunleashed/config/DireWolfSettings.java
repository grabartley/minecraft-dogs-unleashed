package com.grahambartley.dogsunleashed.config;

import com.google.gson.JsonObject;
import com.grahambartley.dogsunleashed.entity.DogGrowthRules;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;

public record DireWolfSettings(
    int growthRateMultiplierPercent, int battlesToEvolve, int smallestPack, int largestPack) {

  public static final int GROWTH_RATE_MIN = 0;
  public static final int GROWTH_RATE_MAX = 500;
  public static final int BATTLES_MIN = 1;
  public static final int BATTLES_MAX = 200;
  public static final int PACK_MIN = 4;
  public static final int PACK_MAX = 16;

  public static final DireWolfSettings DEFAULTS =
      new DireWolfSettings(
          100,
          DogGrowthRules.BATTLES_TO_EVOLVE,
          UnleashedDogBreed.DIRE_WOLF.spawnSettings().minGroupSize(),
          UnleashedDogBreed.DIRE_WOLF.spawnSettings().maxGroupSize());

  static final String KEY_GROWTH_RATE_MULTIPLIER_PERCENT = "growthRateMultiplierPercent";
  static final String KEY_BATTLES_TO_EVOLVE = "battlesToEvolve";
  static final String KEY_SMALLEST_PACK = "smallestPack";
  static final String KEY_LARGEST_PACK = "largestPack";

  public DireWolfSettings {
    growthRateMultiplierPercent =
        clamp(growthRateMultiplierPercent, GROWTH_RATE_MIN, GROWTH_RATE_MAX);
    battlesToEvolve = clamp(battlesToEvolve, BATTLES_MIN, BATTLES_MAX);
    smallestPack = clamp(smallestPack, PACK_MIN, PACK_MAX);
    largestPack = Math.max(smallestPack, clamp(largestPack, PACK_MIN, PACK_MAX));
  }

  public DireWolfSettings withGrowthRateMultiplierPercent(final int value) {
    return new DireWolfSettings(value, battlesToEvolve, smallestPack, largestPack);
  }

  public DireWolfSettings withBattlesToEvolve(final int value) {
    return new DireWolfSettings(growthRateMultiplierPercent, value, smallestPack, largestPack);
  }

  public DireWolfSettings withSmallestPack(final int value) {
    return new DireWolfSettings(growthRateMultiplierPercent, battlesToEvolve, value, largestPack);
  }

  public DireWolfSettings withLargestPack(final int value) {
    return new DireWolfSettings(
        growthRateMultiplierPercent, battlesToEvolve, Math.min(smallestPack, value), value);
  }

  static DireWolfSettings fromJson(final JsonObject json) {
    final ConfigJsonFields fields = new ConfigJsonFields(json);
    return new DireWolfSettings(
        fields.readInt(KEY_GROWTH_RATE_MULTIPLIER_PERCENT, DEFAULTS.growthRateMultiplierPercent),
        fields.readInt(KEY_BATTLES_TO_EVOLVE, DEFAULTS.battlesToEvolve),
        fields.readInt(KEY_SMALLEST_PACK, DEFAULTS.smallestPack),
        fields.readInt(KEY_LARGEST_PACK, DEFAULTS.largestPack));
  }

  JsonObject toJson() {
    final JsonObject json = new JsonObject();
    json.addProperty(KEY_GROWTH_RATE_MULTIPLIER_PERCENT, growthRateMultiplierPercent);
    json.addProperty(KEY_BATTLES_TO_EVOLVE, battlesToEvolve);
    json.addProperty(KEY_SMALLEST_PACK, smallestPack);
    json.addProperty(KEY_LARGEST_PACK, largestPack);
    return json;
  }

  private static int clamp(final int value, final int min, final int max) {
    return Math.max(min, Math.min(max, value));
  }
}
