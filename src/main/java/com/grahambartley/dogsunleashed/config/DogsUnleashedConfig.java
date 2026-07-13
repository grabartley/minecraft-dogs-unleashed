package com.grahambartley.dogsunleashed.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record DogsUnleashedConfig(
    boolean enableNaturalSpawning,
    int spawnRateMultiplierPercent,
    Map<String, Integer> breedSpawnRateMultipliersPercent,
    boolean capIndependentSpawningEnabled,
    boolean gravesEnabled,
    boolean autoSleepEnabled,
    int autoSleepRangeBlocks,
    float barkVolume,
    float howlVolume) {

  private static final Logger LOGGER = LoggerFactory.getLogger(DogsUnleashedConfig.class);
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  public static final int AUTO_SLEEP_RANGE_MIN = 4;
  public static final int AUTO_SLEEP_RANGE_MAX = 128;
  public static final float VOLUME_MIN = 0.0f;
  public static final float VOLUME_MAX = 2.0f;
  public static final int SPAWN_RATE_MULTIPLIER_MIN = 0;
  public static final int SPAWN_RATE_MULTIPLIER_MAX = 500;

  public static final boolean DEFAULT_ENABLE_NATURAL_SPAWNING = true;
  public static final int DEFAULT_SPAWN_RATE_MULTIPLIER_PERCENT = 100;
  public static final boolean DEFAULT_CAP_INDEPENDENT_SPAWNING_ENABLED = false;
  public static final boolean DEFAULT_GRAVES_ENABLED = true;
  public static final boolean DEFAULT_AUTO_SLEEP_ENABLED = true;
  public static final int DEFAULT_AUTO_SLEEP_RANGE_BLOCKS = 32;
  public static final float DEFAULT_BARK_VOLUME = 1.0f;
  public static final float DEFAULT_HOWL_VOLUME = 1.5f;

  static final String KEY_ENABLE_NATURAL_SPAWNING = "enableNaturalSpawning";
  static final String KEY_SPAWN_RATE_MULTIPLIER_PERCENT = "spawnRateMultiplierPercent";
  static final String KEY_BREED_SPAWN_RATE_MULTIPLIERS_PERCENT = "breedSpawnRateMultipliersPercent";
  static final String KEY_CAP_INDEPENDENT_SPAWNING_ENABLED = "capIndependentSpawningEnabled";
  static final String KEY_GRAVES_ENABLED = "gravesEnabled";
  static final String KEY_AUTO_SLEEP_ENABLED = "autoSleepEnabled";
  static final String KEY_AUTO_SLEEP_RANGE_BLOCKS = "autoSleepRangeBlocks";
  static final String KEY_BARK_VOLUME = "barkVolume";
  static final String KEY_HOWL_VOLUME = "howlVolume";

  public DogsUnleashedConfig {
    spawnRateMultiplierPercent =
        clampInt(spawnRateMultiplierPercent, SPAWN_RATE_MULTIPLIER_MIN, SPAWN_RATE_MULTIPLIER_MAX);
    breedSpawnRateMultipliersPercent = normalizedBreedMultipliers(breedSpawnRateMultipliersPercent);
    autoSleepRangeBlocks =
        clampInt(autoSleepRangeBlocks, AUTO_SLEEP_RANGE_MIN, AUTO_SLEEP_RANGE_MAX);
    barkVolume = clampFloat(barkVolume, VOLUME_MIN, VOLUME_MAX);
    howlVolume = clampFloat(howlVolume, VOLUME_MIN, VOLUME_MAX);
  }

  public static DogsUnleashedConfig defaults() {
    return new DogsUnleashedConfig(
        DEFAULT_ENABLE_NATURAL_SPAWNING,
        DEFAULT_SPAWN_RATE_MULTIPLIER_PERCENT,
        Map.of(),
        DEFAULT_CAP_INDEPENDENT_SPAWNING_ENABLED,
        DEFAULT_GRAVES_ENABLED,
        DEFAULT_AUTO_SLEEP_ENABLED,
        DEFAULT_AUTO_SLEEP_RANGE_BLOCKS,
        DEFAULT_BARK_VOLUME,
        DEFAULT_HOWL_VOLUME);
  }

  /**
   * Spawn weight for one breed after applying the global and per-breed multipliers. Returns 0 when
   * either multiplier is 0, which callers must treat as "do not register the spawn"; a non-zero
   * result is floored at 1 so tiny multipliers still spawn rarely instead of silently never.
   */
  public int effectiveSpawnWeight(final int baseWeight, final String breedSerializedId) {
    final int breedPercent =
        breedSpawnRateMultipliersPercent.getOrDefault(
            breedSerializedId, DEFAULT_SPAWN_RATE_MULTIPLIER_PERCENT);
    if (spawnRateMultiplierPercent == 0 || breedPercent == 0) {
      return 0;
    }
    final long scaled =
        Math.round(baseWeight * spawnRateMultiplierPercent * breedPercent / 10000.0);
    return (int) Math.max(1L, scaled);
  }

  public DogsUnleashedConfig withEnableNaturalSpawning(boolean value) {
    return new DogsUnleashedConfig(
        value,
        spawnRateMultiplierPercent,
        breedSpawnRateMultipliersPercent,
        capIndependentSpawningEnabled,
        gravesEnabled,
        autoSleepEnabled,
        autoSleepRangeBlocks,
        barkVolume,
        howlVolume);
  }

  public DogsUnleashedConfig withSpawnRateMultiplierPercent(int value) {
    return new DogsUnleashedConfig(
        enableNaturalSpawning,
        value,
        breedSpawnRateMultipliersPercent,
        capIndependentSpawningEnabled,
        gravesEnabled,
        autoSleepEnabled,
        autoSleepRangeBlocks,
        barkVolume,
        howlVolume);
  }

  public DogsUnleashedConfig withBreedSpawnRateMultiplierPercent(
      final String breedSerializedId, final int value) {
    final Map<String, Integer> updated = new LinkedHashMap<>(breedSpawnRateMultipliersPercent);
    updated.put(breedSerializedId, value);
    return new DogsUnleashedConfig(
        enableNaturalSpawning,
        spawnRateMultiplierPercent,
        updated,
        capIndependentSpawningEnabled,
        gravesEnabled,
        autoSleepEnabled,
        autoSleepRangeBlocks,
        barkVolume,
        howlVolume);
  }

  public DogsUnleashedConfig withCapIndependentSpawningEnabled(boolean value) {
    return new DogsUnleashedConfig(
        enableNaturalSpawning,
        spawnRateMultiplierPercent,
        breedSpawnRateMultipliersPercent,
        value,
        gravesEnabled,
        autoSleepEnabled,
        autoSleepRangeBlocks,
        barkVolume,
        howlVolume);
  }

  public DogsUnleashedConfig withGravesEnabled(boolean value) {
    return new DogsUnleashedConfig(
        enableNaturalSpawning,
        spawnRateMultiplierPercent,
        breedSpawnRateMultipliersPercent,
        capIndependentSpawningEnabled,
        value,
        autoSleepEnabled,
        autoSleepRangeBlocks,
        barkVolume,
        howlVolume);
  }

  public DogsUnleashedConfig withAutoSleepEnabled(boolean value) {
    return new DogsUnleashedConfig(
        enableNaturalSpawning,
        spawnRateMultiplierPercent,
        breedSpawnRateMultipliersPercent,
        capIndependentSpawningEnabled,
        gravesEnabled,
        value,
        autoSleepRangeBlocks,
        barkVolume,
        howlVolume);
  }

  public DogsUnleashedConfig withAutoSleepRangeBlocks(int value) {
    return new DogsUnleashedConfig(
        enableNaturalSpawning,
        spawnRateMultiplierPercent,
        breedSpawnRateMultipliersPercent,
        capIndependentSpawningEnabled,
        gravesEnabled,
        autoSleepEnabled,
        value,
        barkVolume,
        howlVolume);
  }

  public DogsUnleashedConfig withBarkVolume(float value) {
    return new DogsUnleashedConfig(
        enableNaturalSpawning,
        spawnRateMultiplierPercent,
        breedSpawnRateMultipliersPercent,
        capIndependentSpawningEnabled,
        gravesEnabled,
        autoSleepEnabled,
        autoSleepRangeBlocks,
        value,
        howlVolume);
  }

  public DogsUnleashedConfig withHowlVolume(float value) {
    return new DogsUnleashedConfig(
        enableNaturalSpawning,
        spawnRateMultiplierPercent,
        breedSpawnRateMultipliersPercent,
        capIndependentSpawningEnabled,
        gravesEnabled,
        autoSleepEnabled,
        autoSleepRangeBlocks,
        barkVolume,
        value);
  }

  public static DogsUnleashedConfig load(Path path) {
    if (path == null || !Files.exists(path)) {
      return defaults();
    }
    final String json;
    try {
      json = Files.readString(path, StandardCharsets.UTF_8);
    } catch (IOException ex) {
      LOGGER.warn(
          "Failed to read Dogs Unleashed config at {}, using defaults: {}", path, ex.getMessage());
      return defaults();
    }
    try {
      final JsonObject root = GSON.fromJson(json, JsonObject.class);
      if (root == null) {
        return defaults();
      }
      return fromJson(root);
    } catch (JsonParseException | IllegalStateException ex) {
      backupBrokenFile(path);
      LOGGER.warn(
          "Malformed Dogs Unleashed config at {} backed up and defaults loaded: {}",
          path,
          ex.getMessage());
      return defaults();
    }
  }

  public static boolean save(Path path, DogsUnleashedConfig config) {
    if (path == null || config == null) {
      return false;
    }
    try {
      Files.createDirectories(path.getParent());
      final JsonObject root = toJson(config);
      final Path tempFile = path.resolveSibling(path.getFileName().toString() + ".tmp");
      Files.writeString(tempFile, GSON.toJson(root), StandardCharsets.UTF_8);
      Files.move(
          tempFile, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      return true;
    } catch (IOException ex) {
      LOGGER.error("Failed to save Dogs Unleashed config at {}: {}", path, ex.getMessage());
      return false;
    }
  }

  static DogsUnleashedConfig fromJson(JsonObject root) {
    final DogsUnleashedConfig defaults = defaults();
    final boolean enableNaturalSpawning =
        root.has(KEY_ENABLE_NATURAL_SPAWNING)
            ? root.get(KEY_ENABLE_NATURAL_SPAWNING).getAsBoolean()
            : defaults.enableNaturalSpawning;
    final boolean capIndependentSpawningEnabled =
        root.has(KEY_CAP_INDEPENDENT_SPAWNING_ENABLED)
            ? root.get(KEY_CAP_INDEPENDENT_SPAWNING_ENABLED).getAsBoolean()
            : defaults.capIndependentSpawningEnabled;
    final boolean gravesEnabled =
        root.has(KEY_GRAVES_ENABLED)
            ? root.get(KEY_GRAVES_ENABLED).getAsBoolean()
            : defaults.gravesEnabled;
    final boolean autoSleepEnabled =
        root.has(KEY_AUTO_SLEEP_ENABLED)
            ? root.get(KEY_AUTO_SLEEP_ENABLED).getAsBoolean()
            : defaults.autoSleepEnabled;

    final int rawSpawnRate =
        root.has(KEY_SPAWN_RATE_MULTIPLIER_PERCENT)
            ? root.get(KEY_SPAWN_RATE_MULTIPLIER_PERCENT).getAsInt()
            : defaults.spawnRateMultiplierPercent;
    warnIfClampedInt(
        KEY_SPAWN_RATE_MULTIPLIER_PERCENT,
        rawSpawnRate,
        SPAWN_RATE_MULTIPLIER_MIN,
        SPAWN_RATE_MULTIPLIER_MAX);

    final Map<String, Integer> rawBreedRates = new LinkedHashMap<>();
    if (root.has(KEY_BREED_SPAWN_RATE_MULTIPLIERS_PERCENT)
        && root.get(KEY_BREED_SPAWN_RATE_MULTIPLIERS_PERCENT).isJsonObject()) {
      for (final Map.Entry<String, JsonElement> entry :
          root.getAsJsonObject(KEY_BREED_SPAWN_RATE_MULTIPLIERS_PERCENT).entrySet()) {
        rawBreedRates.put(entry.getKey(), entry.getValue().getAsInt());
      }
    }

    final int rawRange =
        root.has(KEY_AUTO_SLEEP_RANGE_BLOCKS)
            ? root.get(KEY_AUTO_SLEEP_RANGE_BLOCKS).getAsInt()
            : defaults.autoSleepRangeBlocks;
    warnIfClampedInt(
        KEY_AUTO_SLEEP_RANGE_BLOCKS, rawRange, AUTO_SLEEP_RANGE_MIN, AUTO_SLEEP_RANGE_MAX);

    final float rawBark =
        root.has(KEY_BARK_VOLUME) ? root.get(KEY_BARK_VOLUME).getAsFloat() : defaults.barkVolume;
    warnIfClampedFloat(KEY_BARK_VOLUME, rawBark, VOLUME_MIN, VOLUME_MAX);

    final float rawHowl =
        root.has(KEY_HOWL_VOLUME) ? root.get(KEY_HOWL_VOLUME).getAsFloat() : defaults.howlVolume;
    warnIfClampedFloat(KEY_HOWL_VOLUME, rawHowl, VOLUME_MIN, VOLUME_MAX);

    return new DogsUnleashedConfig(
        enableNaturalSpawning,
        rawSpawnRate,
        rawBreedRates,
        capIndependentSpawningEnabled,
        gravesEnabled,
        autoSleepEnabled,
        rawRange,
        rawBark,
        rawHowl);
  }

  static JsonObject toJson(DogsUnleashedConfig config) {
    final JsonObject root = new JsonObject();
    root.addProperty(KEY_ENABLE_NATURAL_SPAWNING, config.enableNaturalSpawning);
    root.addProperty(KEY_SPAWN_RATE_MULTIPLIER_PERCENT, config.spawnRateMultiplierPercent);
    final JsonObject breedRates = new JsonObject();
    for (final Map.Entry<String, Integer> entry :
        config.breedSpawnRateMultipliersPercent.entrySet()) {
      breedRates.addProperty(entry.getKey(), entry.getValue());
    }
    root.add(KEY_BREED_SPAWN_RATE_MULTIPLIERS_PERCENT, breedRates);
    root.addProperty(KEY_CAP_INDEPENDENT_SPAWNING_ENABLED, config.capIndependentSpawningEnabled);
    root.addProperty(KEY_GRAVES_ENABLED, config.gravesEnabled);
    root.addProperty(KEY_AUTO_SLEEP_ENABLED, config.autoSleepEnabled);
    root.addProperty(KEY_AUTO_SLEEP_RANGE_BLOCKS, config.autoSleepRangeBlocks);
    root.addProperty(KEY_BARK_VOLUME, config.barkVolume);
    root.addProperty(KEY_HOWL_VOLUME, config.howlVolume);
    return root;
  }

  private static Map<String, Integer> normalizedBreedMultipliers(
      @Nullable final Map<String, Integer> raw) {
    final Map<String, Integer> normalized = new LinkedHashMap<>();
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      normalized.put(breed.serializedId(), DEFAULT_SPAWN_RATE_MULTIPLIER_PERCENT);
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
      final int value =
          entry.getValue() == null ? DEFAULT_SPAWN_RATE_MULTIPLIER_PERCENT : entry.getValue();
      warnIfClampedInt(
          KEY_BREED_SPAWN_RATE_MULTIPLIERS_PERCENT + "." + entry.getKey(),
          value,
          SPAWN_RATE_MULTIPLIER_MIN,
          SPAWN_RATE_MULTIPLIER_MAX);
      normalized.put(
          entry.getKey(), clampInt(value, SPAWN_RATE_MULTIPLIER_MIN, SPAWN_RATE_MULTIPLIER_MAX));
    }
    return Collections.unmodifiableMap(normalized);
  }

  private static int clampInt(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }

  private static float clampFloat(float value, float min, float max) {
    return Math.max(min, Math.min(max, value));
  }

  private static void warnIfClampedInt(String key, int value, int min, int max) {
    if (value < min || value > max) {
      LOGGER.warn(
          "Dogs Unleashed config value {}={} out of range [{}, {}], clamping",
          key,
          value,
          min,
          max);
    }
  }

  private static void warnIfClampedFloat(String key, float value, float min, float max) {
    if (value < min || value > max) {
      LOGGER.warn(
          "Dogs Unleashed config value {}={} out of range [{}, {}], clamping",
          key,
          value,
          min,
          max);
    }
  }

  private static void backupBrokenFile(Path path) {
    try {
      final String fileName = path.getFileName().toString();
      final String baseName =
          fileName.endsWith(".json") ? fileName.substring(0, fileName.length() - 5) : fileName;
      final String timestamp =
          DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replace(":", "-");
      final Path backup = path.resolveSibling(baseName + ".broken." + timestamp + ".json");
      Files.move(path, backup, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException ex) {
      LOGGER.warn("Failed to back up malformed Dogs Unleashed config: {}", ex.getMessage());
    }
  }
}
