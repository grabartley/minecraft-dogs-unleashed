package com.grahambartley.dogsunleashed.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record DogsUnleashedConfig(
    boolean enableNaturalSpawning,
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

  public static final boolean DEFAULT_ENABLE_NATURAL_SPAWNING = true;
  public static final boolean DEFAULT_GRAVES_ENABLED = true;
  public static final boolean DEFAULT_AUTO_SLEEP_ENABLED = true;
  public static final int DEFAULT_AUTO_SLEEP_RANGE_BLOCKS = 32;
  public static final float DEFAULT_BARK_VOLUME = 1.0f;
  public static final float DEFAULT_HOWL_VOLUME = 1.5f;

  static final String KEY_ENABLE_NATURAL_SPAWNING = "enableNaturalSpawning";
  static final String KEY_GRAVES_ENABLED = "gravesEnabled";
  static final String KEY_AUTO_SLEEP_ENABLED = "autoSleepEnabled";
  static final String KEY_AUTO_SLEEP_RANGE_BLOCKS = "autoSleepRangeBlocks";
  static final String KEY_BARK_VOLUME = "barkVolume";
  static final String KEY_HOWL_VOLUME = "howlVolume";

  public DogsUnleashedConfig {
    autoSleepRangeBlocks =
        clampInt(autoSleepRangeBlocks, AUTO_SLEEP_RANGE_MIN, AUTO_SLEEP_RANGE_MAX);
    barkVolume = clampFloat(barkVolume, VOLUME_MIN, VOLUME_MAX);
    howlVolume = clampFloat(howlVolume, VOLUME_MIN, VOLUME_MAX);
  }

  public static DogsUnleashedConfig defaults() {
    return new DogsUnleashedConfig(
        DEFAULT_ENABLE_NATURAL_SPAWNING,
        DEFAULT_GRAVES_ENABLED,
        DEFAULT_AUTO_SLEEP_ENABLED,
        DEFAULT_AUTO_SLEEP_RANGE_BLOCKS,
        DEFAULT_BARK_VOLUME,
        DEFAULT_HOWL_VOLUME);
  }

  public DogsUnleashedConfig withEnableNaturalSpawning(boolean value) {
    return new DogsUnleashedConfig(
        value, gravesEnabled, autoSleepEnabled, autoSleepRangeBlocks, barkVolume, howlVolume);
  }

  public DogsUnleashedConfig withGravesEnabled(boolean value) {
    return new DogsUnleashedConfig(
        enableNaturalSpawning,
        value,
        autoSleepEnabled,
        autoSleepRangeBlocks,
        barkVolume,
        howlVolume);
  }

  public DogsUnleashedConfig withAutoSleepEnabled(boolean value) {
    return new DogsUnleashedConfig(
        enableNaturalSpawning, gravesEnabled, value, autoSleepRangeBlocks, barkVolume, howlVolume);
  }

  public DogsUnleashedConfig withAutoSleepRangeBlocks(int value) {
    return new DogsUnleashedConfig(
        enableNaturalSpawning, gravesEnabled, autoSleepEnabled, value, barkVolume, howlVolume);
  }

  public DogsUnleashedConfig withBarkVolume(float value) {
    return new DogsUnleashedConfig(
        enableNaturalSpawning,
        gravesEnabled,
        autoSleepEnabled,
        autoSleepRangeBlocks,
        value,
        howlVolume);
  }

  public DogsUnleashedConfig withHowlVolume(float value) {
    return new DogsUnleashedConfig(
        enableNaturalSpawning,
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
    final boolean gravesEnabled =
        root.has(KEY_GRAVES_ENABLED)
            ? root.get(KEY_GRAVES_ENABLED).getAsBoolean()
            : defaults.gravesEnabled;
    final boolean autoSleepEnabled =
        root.has(KEY_AUTO_SLEEP_ENABLED)
            ? root.get(KEY_AUTO_SLEEP_ENABLED).getAsBoolean()
            : defaults.autoSleepEnabled;

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
        enableNaturalSpawning, gravesEnabled, autoSleepEnabled, rawRange, rawBark, rawHowl);
  }

  static JsonObject toJson(DogsUnleashedConfig config) {
    final JsonObject root = new JsonObject();
    root.addProperty(KEY_ENABLE_NATURAL_SPAWNING, config.enableNaturalSpawning);
    root.addProperty(KEY_GRAVES_ENABLED, config.gravesEnabled);
    root.addProperty(KEY_AUTO_SLEEP_ENABLED, config.autoSleepEnabled);
    root.addProperty(KEY_AUTO_SLEEP_RANGE_BLOCKS, config.autoSleepRangeBlocks);
    root.addProperty(KEY_BARK_VOLUME, config.barkVolume);
    root.addProperty(KEY_HOWL_VOLUME, config.howlVolume);
    return root;
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
