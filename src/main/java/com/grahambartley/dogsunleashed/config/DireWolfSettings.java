package com.grahambartley.dogsunleashed.config;

import com.google.gson.JsonObject;
import com.grahambartley.dogsunleashed.entity.DogGrowthRules;

public record DireWolfSettings(
    int growthRateMultiplierPercent,
    int battlesToEvolve,
    int smallestPack,
    int largestPack,
    DireWolfPackSettings packs,
    DireWolfStaminaSettings stamina) {

  public static final int GROWTH_RATE_MIN = 0;
  public static final int GROWTH_RATE_MAX = 500;
  public static final int BATTLES_MIN = 1;
  public static final int BATTLES_MAX = 200;
  public static final int PACK_MIN = 4;
  public static final int PACK_MAX = 16;
  public static final int DEFAULT_SMALLEST_PACK = 5;
  public static final int DEFAULT_LARGEST_PACK = 10;

  public static final DireWolfSettings DEFAULTS =
      new DireWolfSettings(
          100,
          DogGrowthRules.BATTLES_TO_EVOLVE,
          DEFAULT_SMALLEST_PACK,
          DEFAULT_LARGEST_PACK,
          DireWolfPackSettings.DEFAULTS,
          DireWolfStaminaSettings.DEFAULTS);

  static final String KEY_GROWTH_RATE_MULTIPLIER_PERCENT = "growthRateMultiplierPercent";
  static final String KEY_BATTLES_TO_EVOLVE = "battlesToEvolve";
  static final String KEY_SMALLEST_PACK = "smallestPack";
  static final String KEY_LARGEST_PACK = "largestPack";
  static final String KEY_PACKS = "packs";
  static final String KEY_STAMINA = "stamina";

  public DireWolfSettings {
    growthRateMultiplierPercent =
        ConfigBounds.clamp(growthRateMultiplierPercent, GROWTH_RATE_MIN, GROWTH_RATE_MAX);
    battlesToEvolve = ConfigBounds.clamp(battlesToEvolve, BATTLES_MIN, BATTLES_MAX);
    smallestPack = ConfigBounds.clamp(smallestPack, PACK_MIN, PACK_MAX);
    largestPack = Math.max(smallestPack, ConfigBounds.clamp(largestPack, PACK_MIN, PACK_MAX));
    packs = packs == null ? DireWolfPackSettings.DEFAULTS : packs;
    stamina = stamina == null ? DireWolfStaminaSettings.DEFAULTS : stamina;
  }

  public DireWolfSettings withGrowthRateMultiplierPercent(final int value) {
    return new DireWolfSettings(value, battlesToEvolve, smallestPack, largestPack, packs, stamina);
  }

  public DireWolfSettings withBattlesToEvolve(final int value) {
    return new DireWolfSettings(
        growthRateMultiplierPercent, value, smallestPack, largestPack, packs, stamina);
  }

  public DireWolfSettings withSmallestPack(final int value) {
    return new DireWolfSettings(
        growthRateMultiplierPercent, battlesToEvolve, value, largestPack, packs, stamina);
  }

  public DireWolfSettings withLargestPack(final int value) {
    return new DireWolfSettings(
        growthRateMultiplierPercent,
        battlesToEvolve,
        Math.min(smallestPack, value),
        value,
        packs,
        stamina);
  }

  public DireWolfSettings withPacks(final DireWolfPackSettings value) {
    return new DireWolfSettings(
        growthRateMultiplierPercent, battlesToEvolve, smallestPack, largestPack, value, stamina);
  }

  public DireWolfSettings withStamina(final DireWolfStaminaSettings value) {
    return new DireWolfSettings(
        growthRateMultiplierPercent, battlesToEvolve, smallestPack, largestPack, packs, value);
  }

  static DireWolfSettings fromJson(final JsonObject json) {
    final ConfigJsonFields fields = new ConfigJsonFields(json);
    return new DireWolfSettings(
        fields.readInt(
            KEY_GROWTH_RATE_MULTIPLIER_PERCENT,
            DEFAULTS.growthRateMultiplierPercent,
            GROWTH_RATE_MIN,
            GROWTH_RATE_MAX),
        fields.readInt(KEY_BATTLES_TO_EVOLVE, DEFAULTS.battlesToEvolve, BATTLES_MIN, BATTLES_MAX),
        fields.readInt(KEY_SMALLEST_PACK, DEFAULTS.smallestPack, PACK_MIN, PACK_MAX),
        fields.readInt(KEY_LARGEST_PACK, DEFAULTS.largestPack, PACK_MIN, PACK_MAX),
        DireWolfPackSettings.fromJson(fields.readObject(KEY_PACKS)),
        DireWolfStaminaSettings.fromJson(fields.readObject(KEY_STAMINA)));
  }

  JsonObject toJson() {
    final JsonObject json = new JsonObject();
    json.addProperty(KEY_GROWTH_RATE_MULTIPLIER_PERCENT, growthRateMultiplierPercent);
    json.addProperty(KEY_BATTLES_TO_EVOLVE, battlesToEvolve);
    json.addProperty(KEY_SMALLEST_PACK, smallestPack);
    json.addProperty(KEY_LARGEST_PACK, largestPack);
    json.add(KEY_PACKS, packs.toJson());
    json.add(KEY_STAMINA, stamina.toJson());
    return json;
  }
}
