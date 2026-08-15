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
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-only state that persists across launches, kept separate from {@link DogsUnleashedConfig}
 * because it is per-installation rather than server-authoritative.
 */
public record ClientState(boolean keybindNudgeShown) {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientState.class);
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private static final String DOGS_UNLEASHED_DIR = "dogs-unleashed";
  private static final String CLIENT_STATE_FILE = "client-state.json";

  public static final boolean DEFAULT_KEYBIND_NUDGE_SHOWN = false;

  static final String KEY_KEYBIND_NUDGE_SHOWN = "keybindNudgeShown";

  public static ClientState defaults() {
    return new ClientState(DEFAULT_KEYBIND_NUDGE_SHOWN);
  }

  public ClientState withKeybindNudgeShown(final boolean value) {
    return new ClientState(value);
  }

  public static Path defaultPath() {
    return FabricLoader.getInstance()
        .getConfigDir()
        .resolve(DOGS_UNLEASHED_DIR)
        .resolve(CLIENT_STATE_FILE);
  }

  public static ClientState load(final Path path) {
    if (path == null || !Files.exists(path)) {
      return defaults();
    }
    final String json;
    try {
      json = Files.readString(path, StandardCharsets.UTF_8);
    } catch (IOException ex) {
      LOGGER.warn(
          "Failed to read Dogs Unleashed client state at {}, using defaults: {}",
          path,
          ex.getMessage());
      return defaults();
    }
    try {
      final JsonObject root = GSON.fromJson(json, JsonObject.class);
      if (root == null) {
        return defaults();
      }
      return fromJson(root);
    } catch (JsonParseException | IllegalStateException | UnsupportedOperationException ex) {
      LOGGER.warn(
          "Malformed Dogs Unleashed client state at {}, using defaults: {}", path, ex.getMessage());
      return defaults();
    }
  }

  public static boolean save(final Path path, final ClientState state) {
    if (path == null || state == null) {
      return false;
    }
    try {
      Files.createDirectories(path.getParent());
      final Path tempFile = path.resolveSibling(path.getFileName().toString() + ".tmp");
      Files.writeString(tempFile, GSON.toJson(toJson(state)), StandardCharsets.UTF_8);
      Files.move(
          tempFile, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      return true;
    } catch (IOException ex) {
      LOGGER.error("Failed to save Dogs Unleashed client state at {}: {}", path, ex.getMessage());
      return false;
    }
  }

  static ClientState fromJson(final JsonObject root) {
    final ClientState defaults = defaults();
    final boolean keybindNudgeShown =
        root.has(KEY_KEYBIND_NUDGE_SHOWN)
            ? root.get(KEY_KEYBIND_NUDGE_SHOWN).getAsBoolean()
            : defaults.keybindNudgeShown;
    return new ClientState(keybindNudgeShown);
  }

  static JsonObject toJson(final ClientState state) {
    final JsonObject root = new JsonObject();
    root.addProperty(KEY_KEYBIND_NUDGE_SHOWN, state.keybindNudgeShown);
    return root;
  }
}
