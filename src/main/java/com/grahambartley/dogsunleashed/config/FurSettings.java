package com.grahambartley.dogsunleashed.config;

import com.google.gson.JsonObject;

public record FurSettings(
    boolean furGrowthEnabled,
    int furGrowthRateMultiplierPercent,
    int badHairDayChanceMultiplierPercent) {

  public static final int PERCENT_MIN = 0;
  public static final int PERCENT_MAX = 500;

  public static final FurSettings DEFAULTS = new FurSettings(true, 100, 100);

  static final String KEY_FUR_GROWTH_ENABLED = "furGrowthEnabled";
  static final String KEY_FUR_GROWTH_RATE_MULTIPLIER_PERCENT = "furGrowthRateMultiplierPercent";
  static final String KEY_BAD_HAIR_DAY_CHANCE_MULTIPLIER_PERCENT =
      "badHairDayChanceMultiplierPercent";

  public FurSettings {
    furGrowthRateMultiplierPercent =
        ConfigBounds.clamp(furGrowthRateMultiplierPercent, PERCENT_MIN, PERCENT_MAX);
    badHairDayChanceMultiplierPercent =
        ConfigBounds.clamp(badHairDayChanceMultiplierPercent, PERCENT_MIN, PERCENT_MAX);
  }

  public FurSettings withFurGrowthEnabled(final boolean value) {
    return new FurSettings(
        value, furGrowthRateMultiplierPercent, badHairDayChanceMultiplierPercent);
  }

  public FurSettings withFurGrowthRateMultiplierPercent(final int value) {
    return new FurSettings(furGrowthEnabled, value, badHairDayChanceMultiplierPercent);
  }

  public FurSettings withBadHairDayChanceMultiplierPercent(final int value) {
    return new FurSettings(furGrowthEnabled, furGrowthRateMultiplierPercent, value);
  }

  static FurSettings fromJson(final ConfigJsonFields fields) {
    return new FurSettings(
        fields.readBoolean(KEY_FUR_GROWTH_ENABLED, DEFAULTS.furGrowthEnabled),
        fields.readInt(
            KEY_FUR_GROWTH_RATE_MULTIPLIER_PERCENT,
            DEFAULTS.furGrowthRateMultiplierPercent,
            PERCENT_MIN,
            PERCENT_MAX),
        fields.readInt(
            KEY_BAD_HAIR_DAY_CHANCE_MULTIPLIER_PERCENT,
            DEFAULTS.badHairDayChanceMultiplierPercent,
            PERCENT_MIN,
            PERCENT_MAX));
  }

  void writeJson(final JsonObject root) {
    root.addProperty(KEY_FUR_GROWTH_ENABLED, furGrowthEnabled);
    root.addProperty(KEY_FUR_GROWTH_RATE_MULTIPLIER_PERCENT, furGrowthRateMultiplierPercent);
    root.addProperty(KEY_BAD_HAIR_DAY_CHANCE_MULTIPLIER_PERCENT, badHairDayChanceMultiplierPercent);
  }
}
