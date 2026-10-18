package com.grahambartley.dogsunleashed.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
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
    SpawningSettings spawning,
    GameplaySettings gameplay,
    SleepSettings sleep,
    FurSettings fur,
    SoundSettings sound,
    DireWolfSettings direWolf) {

  private static final Logger LOGGER = LoggerFactory.getLogger(DogsUnleashedConfig.class);
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  static final String KEY_DIRE_WOLF = "direWolf";

  public DogsUnleashedConfig {
    spawning = spawning == null ? SpawningSettings.DEFAULTS : spawning;
    gameplay = gameplay == null ? GameplaySettings.DEFAULTS : gameplay;
    sleep = sleep == null ? SleepSettings.DEFAULTS : sleep;
    fur = fur == null ? FurSettings.DEFAULTS : fur;
    sound = sound == null ? SoundSettings.DEFAULTS : sound;
    direWolf = direWolf == null ? DireWolfSettings.DEFAULTS : direWolf;
  }

  public static DogsUnleashedConfig defaults() {
    return new DogsUnleashedConfig(
        SpawningSettings.DEFAULTS,
        GameplaySettings.DEFAULTS,
        SleepSettings.DEFAULTS,
        FurSettings.DEFAULTS,
        SoundSettings.DEFAULTS,
        DireWolfSettings.DEFAULTS);
  }

  public DogsUnleashedConfig withSpawning(final SpawningSettings value) {
    return new DogsUnleashedConfig(value, gameplay, sleep, fur, sound, direWolf);
  }

  public DogsUnleashedConfig withGameplay(final GameplaySettings value) {
    return new DogsUnleashedConfig(spawning, value, sleep, fur, sound, direWolf);
  }

  public DogsUnleashedConfig withSleep(final SleepSettings value) {
    return new DogsUnleashedConfig(spawning, gameplay, value, fur, sound, direWolf);
  }

  public DogsUnleashedConfig withFur(final FurSettings value) {
    return new DogsUnleashedConfig(spawning, gameplay, sleep, value, sound, direWolf);
  }

  public DogsUnleashedConfig withSound(final SoundSettings value) {
    return new DogsUnleashedConfig(spawning, gameplay, sleep, fur, value, direWolf);
  }

  public DogsUnleashedConfig withDireWolf(final DireWolfSettings value) {
    return new DogsUnleashedConfig(spawning, gameplay, sleep, fur, sound, value);
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
      final JsonElement root = JsonParser.parseString(json);
      if (root.isJsonNull()) {
        return defaults();
      }
      if (!root.isJsonObject()) {
        throw new JsonParseException("top level is not a JSON object");
      }
      return fromJson(root.getAsJsonObject());
    } catch (JsonParseException ex) {
      backupBrokenFile(path);
      LOGGER.warn(
          "Malformed Dogs Unleashed config at {} backed up and defaults loaded: {}",
          path,
          ex.getMessage());
      return defaults();
    }
  }

  public static DogsUnleashedConfig loadOrCreate(Path path) {
    if (path != null && !Files.exists(path)) {
      final DogsUnleashedConfig defaults = defaults();
      save(path, defaults);
      return defaults;
    }
    return load(path);
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
    final ConfigJsonFields fields = new ConfigJsonFields(root);
    return new DogsUnleashedConfig(
        SpawningSettings.fromJson(fields),
        GameplaySettings.fromJson(fields),
        SleepSettings.fromJson(fields),
        FurSettings.fromJson(fields),
        SoundSettings.fromJson(fields),
        DireWolfSettings.fromJson(fields.readObject(KEY_DIRE_WOLF)));
  }

  static JsonObject toJson(DogsUnleashedConfig config) {
    final JsonObject root = new JsonObject();
    config.spawning.writeJson(root);
    config.gameplay.writeJson(root);
    config.sleep.writeJson(root);
    config.sound.writeJson(root);
    config.fur.writeJson(root);
    root.add(KEY_DIRE_WOLF, config.direWolf.toJson());
    return root;
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
