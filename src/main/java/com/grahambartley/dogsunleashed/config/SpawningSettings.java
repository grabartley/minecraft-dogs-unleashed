package com.grahambartley.dogsunleashed.config;

import com.google.gson.JsonObject;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record SpawningSettings(
    boolean enableNaturalSpawning,
    int spawnRateMultiplierPercent,
    Map<String, Integer> breedSpawnRateMultipliersPercent,
    boolean capIndependentSpawningEnabled) {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpawningSettings.class);

  public static final int RATE_MIN = 0;
  public static final int RATE_MAX = 500;
  public static final int DEFAULT_RATE_PERCENT = 100;

  public static final SpawningSettings DEFAULTS =
      new SpawningSettings(true, DEFAULT_RATE_PERCENT, Map.of(), true);

  static final String KEY_ENABLE_NATURAL_SPAWNING = "enableNaturalSpawning";
  static final String KEY_SPAWN_RATE_MULTIPLIER_PERCENT = "spawnRateMultiplierPercent";
  static final String KEY_BREED_SPAWN_RATE_MULTIPLIERS_PERCENT = "breedSpawnRateMultipliersPercent";
  static final String KEY_CAP_INDEPENDENT_SPAWNING_ENABLED = "capIndependentSpawningEnabled";

  public SpawningSettings {
    spawnRateMultiplierPercent = ConfigBounds.clamp(spawnRateMultiplierPercent, RATE_MIN, RATE_MAX);
    breedSpawnRateMultipliersPercent = normalizedBreedMultipliers(breedSpawnRateMultipliersPercent);
  }

  public double spawnRate(final String breedSerializedId) {
    return spawnRateMultiplierPercent * breedSpawnRatePercent(breedSerializedId) / 10000.0;
  }

  public int effectiveSpawnWeight(final int baseWeight, final String breedSerializedId) {
    final int breedPercent = breedSpawnRatePercent(breedSerializedId);
    if (spawnRateMultiplierPercent == 0 || breedPercent == 0) {
      return 0;
    }
    final long scaled =
        Math.round(baseWeight * spawnRateMultiplierPercent * breedPercent / 10000.0);
    return (int) Math.max(1L, scaled);
  }

  public boolean biomeSpawnsDifferFrom(final SpawningSettings other) {
    return enableNaturalSpawning != other.enableNaturalSpawning
        || spawnRateMultiplierPercent != other.spawnRateMultiplierPercent
        || !breedSpawnRateMultipliersPercent.equals(other.breedSpawnRateMultipliersPercent);
  }

  private int breedSpawnRatePercent(final String breedSerializedId) {
    return breedSpawnRateMultipliersPercent.getOrDefault(breedSerializedId, DEFAULT_RATE_PERCENT);
  }

  public SpawningSettings withEnableNaturalSpawning(final boolean value) {
    return new SpawningSettings(
        value,
        spawnRateMultiplierPercent,
        breedSpawnRateMultipliersPercent,
        capIndependentSpawningEnabled);
  }

  public SpawningSettings withSpawnRateMultiplierPercent(final int value) {
    return new SpawningSettings(
        enableNaturalSpawning,
        value,
        breedSpawnRateMultipliersPercent,
        capIndependentSpawningEnabled);
  }

  public SpawningSettings withBreedSpawnRateMultiplierPercent(
      final String breedSerializedId, final int value) {
    final Map<String, Integer> updated = new LinkedHashMap<>(breedSpawnRateMultipliersPercent);
    updated.put(breedSerializedId, value);
    return new SpawningSettings(
        enableNaturalSpawning, spawnRateMultiplierPercent, updated, capIndependentSpawningEnabled);
  }

  public SpawningSettings withCapIndependentSpawningEnabled(final boolean value) {
    return new SpawningSettings(
        enableNaturalSpawning, spawnRateMultiplierPercent, breedSpawnRateMultipliersPercent, value);
  }

  static SpawningSettings fromJson(final ConfigJsonFields fields) {
    return new SpawningSettings(
        fields.readBoolean(KEY_ENABLE_NATURAL_SPAWNING, DEFAULTS.enableNaturalSpawning),
        fields.readInt(
            KEY_SPAWN_RATE_MULTIPLIER_PERCENT,
            DEFAULTS.spawnRateMultiplierPercent,
            RATE_MIN,
            RATE_MAX),
        fields.readIntMap(KEY_BREED_SPAWN_RATE_MULTIPLIERS_PERCENT),
        fields.readBoolean(
            KEY_CAP_INDEPENDENT_SPAWNING_ENABLED, DEFAULTS.capIndependentSpawningEnabled));
  }

  void writeJson(final JsonObject root) {
    root.addProperty(KEY_ENABLE_NATURAL_SPAWNING, enableNaturalSpawning);
    root.addProperty(KEY_SPAWN_RATE_MULTIPLIER_PERCENT, spawnRateMultiplierPercent);
    final JsonObject breedRates = new JsonObject();
    breedSpawnRateMultipliersPercent.forEach(breedRates::addProperty);
    root.add(KEY_BREED_SPAWN_RATE_MULTIPLIERS_PERCENT, breedRates);
    root.addProperty(KEY_CAP_INDEPENDENT_SPAWNING_ENABLED, capIndependentSpawningEnabled);
  }

  private static Map<String, Integer> normalizedBreedMultipliers(
      @Nullable final Map<String, Integer> raw) {
    final Map<String, Integer> normalized = new LinkedHashMap<>();
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      if (breed.isNaturallySpawning()) {
        normalized.put(breed.serializedId(), DEFAULT_RATE_PERCENT);
      }
    }
    if (raw == null) {
      return Collections.unmodifiableMap(normalized);
    }
    for (final Map.Entry<String, Integer> entry : raw.entrySet()) {
      if (!normalized.containsKey(entry.getKey())) {
        LOGGER.warn(
            "Dogs Unleashed config dropping unknown breed key {} under {}",
            entry.getKey(),
            KEY_BREED_SPAWN_RATE_MULTIPLIERS_PERCENT);
        continue;
      }
      final int value = entry.getValue() == null ? DEFAULT_RATE_PERCENT : entry.getValue();
      ConfigBounds.warnIfOutside(
          KEY_BREED_SPAWN_RATE_MULTIPLIERS_PERCENT + "." + entry.getKey(),
          value,
          RATE_MIN,
          RATE_MAX);
      normalized.put(entry.getKey(), ConfigBounds.clamp(value, RATE_MIN, RATE_MAX));
    }
    return Collections.unmodifiableMap(normalized);
  }
}
