package com.grahambartley.dogsunleashed.render.rig;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
        variants.put(
            entry.getKey(),
            names.asList().stream().map(JsonElement::getAsString).collect(Collectors.toSet()));
      }
    }
    return new RigEars(shape, variants);
  }

  public Set<String> variantBones() {
    final Set<String> bones = new HashSet<>();
    this.variants.values().forEach(bones::addAll);
    return bones;
  }

  public Set<String> hiddenFor(final Optional<String> wanted, final Set<String> nativeBones) {
    final Set<String> hidden = new LinkedHashSet<>(this.variantBones());
    if (wanted.isEmpty() || wanted.equals(this.shape) || !this.variants.containsKey(wanted.get())) {
      return Set.copyOf(hidden);
    }
    hidden.removeAll(this.variants.get(wanted.get()));
    hidden.addAll(nativeBones);
    return Set.copyOf(hidden);
  }

  public Set<String> shownFor(final Optional<String> wanted, final Set<String> nativeBones) {
    if (wanted.isEmpty() || wanted.equals(this.shape) || !this.variants.containsKey(wanted.get())) {
      return nativeBones;
    }
    return this.variants.get(wanted.get());
  }
}
