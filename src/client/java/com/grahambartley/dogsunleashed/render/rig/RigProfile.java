package com.grahambartley.dogsunleashed.render.rig;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.Identifier;

public record RigProfile(Map<Slot, Set<String>> bones, Map<Overlay, Identifier> overlays) {

  public static final RigProfile EMPTY = new RigProfile(Map.of(), Map.of());

  public enum Slot {
    TORSO,
    MOUTH,
    HEAD,
    EYES,
    NECK,
    TAIL,
    EAR_LEFT,
    EAR_RIGHT,
    LEG_FRONT_LEFT,
    LEG_FRONT_RIGHT,
    LEG_BACK_LEFT,
    LEG_BACK_RIGHT,
    FUR_SHAGGY,
    FUR_OVERGROWN;

    String key() {
      return this.name().toLowerCase(Locale.ROOT);
    }
  }

  public enum Overlay {
    TRIMMED,
    BOTCHED;

    String key() {
      return this.name().toLowerCase(Locale.ROOT) + "_overlay";
    }
  }

  public RigProfile {
    bones = Map.copyOf(bones);
    overlays = Map.copyOf(overlays);
  }

  public static RigProfile parse(final JsonObject json) {
    final Map<Slot, Set<String>> bones = new EnumMap<>(Slot.class);
    for (final Slot slot : Slot.values()) {
      if (json.get(slot.key()) instanceof JsonArray names) {
        bones.put(
            slot,
            names.asList().stream().map(JsonElement::getAsString).collect(Collectors.toSet()));
      }
    }
    final Map<Overlay, Identifier> overlays = new EnumMap<>(Overlay.class);
    for (final Overlay overlay : Overlay.values()) {
      if (json.has(overlay.key())) {
        overlays.put(overlay, Identifier.of(json.get(overlay.key()).getAsString()));
      }
    }
    return new RigProfile(bones, overlays);
  }

  public Set<String> bonesIn(final Slot slot) {
    return this.bones.getOrDefault(slot, Set.of());
  }

  public Optional<String> mouthBone() {
    return this.bonesIn(Slot.MOUTH).stream().findFirst();
  }

  public Optional<Identifier> overlay(final Overlay overlay) {
    return Optional.ofNullable(this.overlays.get(overlay));
  }
}
