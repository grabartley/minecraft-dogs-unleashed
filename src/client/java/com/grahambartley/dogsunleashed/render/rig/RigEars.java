package com.grahambartley.dogsunleashed.render.rig;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record RigEars(Optional<String> shape, Map<String, Set<String>> variants) {

  private static final Logger LOGGER = LoggerFactory.getLogger(RigEars.class);

  private static final String SHAPE_KEY = "shape";
  private static final String VARIANTS_KEY = "variants";

  public static final RigEars NONE = new RigEars(Optional.empty(), Map.of());

  public RigEars {
    variants =
        variants.entrySet().stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    Map.Entry::getKey, entry -> Set.copyOf(entry.getValue())));
  }

  public static RigEars parse(final JsonElement element) {
    if (!(element instanceof JsonObject json)) {
      LOGGER.warn("Ignoring ears {}, expected an object", element);
      return NONE;
    }
    final Optional<String> shape =
        json.get(SHAPE_KEY) instanceof JsonPrimitive value && value.isString()
            ? Optional.of(value.getAsString())
            : Optional.empty();
    final Map<String, Set<String>> variants = new HashMap<>();
    if (json.get(VARIANTS_KEY) instanceof JsonObject byShape) {
      for (final Map.Entry<String, JsonElement> entry : byShape.entrySet()) {
        if (!(entry.getValue() instanceof JsonArray names) || names.isEmpty()) {
          LOGGER.warn("Skipping ear variant {}", entry.getKey());
          continue;
        }
        final Set<String> bones = new HashSet<>();
        for (final JsonElement name : names) {
          if (name instanceof JsonPrimitive value && value.isString()) {
            bones.add(value.getAsString());
          } else {
            LOGGER.warn("Skipping ear bone {} of variant {}", name, entry.getKey());
          }
        }
        if (!bones.isEmpty()) {
          variants.put(entry.getKey(), bones);
        }
      }
    }
    return new RigEars(shape, variants);
  }

  public Set<String> variantBones() {
    if (this.variants.isEmpty()) {
      return Set.of();
    }
    final Set<String> bones = new HashSet<>();
    this.variants.values().forEach(bones::addAll);
    return bones;
  }

  public Set<String> hiddenFor(
      final Optional<String> wanted,
      final Set<String> nativeBones,
      final Set<String> hiddenByHeadwear) {
    if (this.variants.isEmpty()) {
      return hiddenByHeadwear;
    }
    final Set<String> hidden = new HashSet<>(this.variantBones());
    hidden.addAll(hiddenByHeadwear);
    if (wanted.isEmpty() || wanted.equals(this.shape) || !this.variants.containsKey(wanted.get())) {
      return Set.copyOf(hidden);
    }
    final Set<String> swapped = this.variants.get(wanted.get());
    hidden.addAll(nativeBones);
    if (hiddenByHeadwear.isEmpty()) {
      hidden.removeAll(swapped);
    }
    return Set.copyOf(hidden);
  }
}
