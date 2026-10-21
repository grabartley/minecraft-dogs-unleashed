package com.grahambartley.dogsunleashed.render.sled;

import static com.grahambartley.dogsunleashed.DogsUnleashed.MOD_ID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.model.SledModel;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SledAnchors {

  public static final Identifier RELOADER_ID = Identifier.of(MOD_ID, "sled_anchors");
  static final String HARNESS_PREFIX = "harness_";
  private static final float MODEL_UNITS_PER_BLOCK = 16.0f;
  private static final Logger LOGGER = LoggerFactory.getLogger(SledAnchors.class);
  private static final SledAnchors SHARED = new SledAnchors(SledAnchors::openShipped);

  @FunctionalInterface
  public interface Source {
    Optional<Reader> open(Identifier id) throws IOException;
  }

  private final Source source;
  private List<Vector3f> eyes;

  public SledAnchors(final Source source) {
    this.source = source;
  }

  public static SledAnchors shared() {
    return SHARED;
  }

  public synchronized List<Vector3f> eyes() {
    if (this.eyes == null) {
      this.eyes = this.load();
    }
    return this.eyes.stream().map(Vector3f::new).toList();
  }

  public synchronized void clear() {
    this.eyes = null;
  }

  static List<Vector3f> harnessEyesOf(final JsonObject geometryFile) {
    final Map<Integer, Vector3f> byNumber = new TreeMap<>();
    for (final JsonElement geometry : geometryFile.getAsJsonArray("minecraft:geometry")) {
      final JsonArray bones = geometry.getAsJsonObject().getAsJsonArray("bones");
      if (bones == null) {
        continue;
      }
      for (final JsonElement bone : bones) {
        final JsonObject locators = bone.getAsJsonObject().getAsJsonObject("locators");
        if (locators == null) {
          continue;
        }
        for (final Map.Entry<String, JsonElement> locator : locators.entrySet()) {
          final Integer number = harnessNumber(locator.getKey());
          if (number != null && locator.getValue().isJsonArray()) {
            final JsonArray point = locator.getValue().getAsJsonArray();
            byNumber.put(
                number,
                new Vector3f(
                    point.get(0).getAsFloat() / MODEL_UNITS_PER_BLOCK,
                    point.get(1).getAsFloat() / MODEL_UNITS_PER_BLOCK,
                    -point.get(2).getAsFloat() / MODEL_UNITS_PER_BLOCK));
          }
        }
      }
    }
    return new ArrayList<>(byNumber.values());
  }

  private static Integer harnessNumber(final String name) {
    if (!name.startsWith(HARNESS_PREFIX)) {
      return null;
    }
    try {
      return Integer.parseInt(name.substring(HARNESS_PREFIX.length()));
    } catch (final NumberFormatException exception) {
      return null;
    }
  }

  private List<Vector3f> load() {
    try {
      final Optional<Reader> reader = this.source.open(SledModel.GEOMETRY);
      if (reader.isEmpty()) {
        return List.of();
      }
      try (Reader open = reader.get()) {
        return harnessEyesOf(JsonParser.parseReader(open).getAsJsonObject());
      }
    } catch (final IOException | RuntimeException exception) {
      LOGGER.warn("Unreadable sled geometry: {}", exception.getMessage());
      return List.of();
    }
  }

  private static Optional<Reader> openShipped(final Identifier id) throws IOException {
    final MinecraftClient client = MinecraftClient.getInstance();
    if (client == null || client.getResourceManager() == null) {
      return Optional.empty();
    }
    final Optional<Resource> resource = client.getResourceManager().getResource(id);
    return resource.isPresent() ? Optional.of(resource.get().getReader()) : Optional.empty();
  }

  static Comparator<Vector3f> outermostFirst() {
    return Comparator.comparingDouble((Vector3f eye) -> -Math.abs(eye.x));
  }
}
