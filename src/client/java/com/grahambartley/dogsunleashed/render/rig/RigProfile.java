package com.grahambartley.dogsunleashed.render.rig;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record RigProfile(
    Map<Slot, Set<String>> bones,
    Map<Overlay, Identifier> overlays,
    List<Texel> pupils,
    Map<Identifier, Map<Slot, Identifier>> sheetOverrides) {

  private static final Logger LOGGER = LoggerFactory.getLogger(RigProfile.class);

  private static final String PUPILS_KEY = "pupils";
  private static final String SHEET_OVERRIDES_KEY = "sheet_overrides";

  public static final RigProfile EMPTY = new RigProfile(Map.of(), Map.of(), List.of(), Map.of());

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
    pupils = List.copyOf(pupils);
    sheetOverrides =
        sheetOverrides.entrySet().stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    Map.Entry::getKey, entry -> Map.copyOf(entry.getValue())));
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
    final List<Texel> pupils = new ArrayList<>();
    if (json.get(PUPILS_KEY) instanceof JsonArray texels) {
      for (final JsonElement texel : texels) {
        texelOf(texel).ifPresentOrElse(pupils::add, () -> LOGGER.warn("Skipping pupil {}", texel));
      }
    } else if (json.has(PUPILS_KEY)) {
      LOGGER.warn("Ignoring pupils {}, expected a list of [x, y] pairs", json.get(PUPILS_KEY));
    }
    return new RigProfile(bones, overlays, pupils, sheetOverridesOf(json));
  }

  private static Map<Identifier, Map<Slot, Identifier>> sheetOverridesOf(final JsonObject json) {
    final Map<Identifier, Map<Slot, Identifier>> sheets = new HashMap<>();
    if (!(json.get(SHEET_OVERRIDES_KEY) instanceof JsonObject bySheet)) {
      if (json.has(SHEET_OVERRIDES_KEY)) {
        LOGGER.warn(
            "Ignoring sheet overrides {}, expected an object", json.get(SHEET_OVERRIDES_KEY));
      }
      return sheets;
    }
    for (final Map.Entry<String, JsonElement> sheet : bySheet.entrySet()) {
      final Identifier sheetId = Identifier.tryParse(sheet.getKey());
      if (sheetId == null || !(sheet.getValue() instanceof JsonObject bySlot)) {
        LOGGER.warn("Skipping sheet override {}", sheet.getKey());
        continue;
      }
      final Map<Slot, Identifier> slots = new EnumMap<>(Slot.class);
      for (final Map.Entry<String, JsonElement> entry : bySlot.entrySet()) {
        final Optional<Slot> slot = slotOf(entry.getKey());
        final Identifier texture =
            entry.getValue() instanceof JsonPrimitive value && value.isString()
                ? Identifier.tryParse(value.getAsString())
                : null;
        if (slot.isEmpty() || texture == null) {
          LOGGER.warn(
              "Skipping {} override {} of sheet {}", entry.getKey(), entry.getValue(), sheetId);
          continue;
        }
        slots.put(slot.get(), texture);
      }
      if (slots.isEmpty()) {
        continue;
      }
      sheets.put(sheetId, slots);
    }
    return sheets;
  }

  private static Optional<Texel> texelOf(final JsonElement element) {
    if (element instanceof JsonArray xy
        && xy.size() == 2
        && isWholeNumber(xy.get(0))
        && isWholeNumber(xy.get(1))) {
      return Optional.of(new Texel(xy.get(0).getAsInt(), xy.get(1).getAsInt()));
    }
    return Optional.empty();
  }

  private static boolean isWholeNumber(final JsonElement element) {
    if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
      return false;
    }
    final double value = element.getAsDouble();
    return value == Math.rint(value);
  }

  private static Optional<Slot> slotOf(final String key) {
    for (final Slot slot : Slot.values()) {
      if (slot.key().equals(key)) {
        return Optional.of(slot);
      }
    }
    return Optional.empty();
  }

  public Set<String> bonesIn(final Slot slot) {
    return this.bones.getOrDefault(slot, Set.of());
  }

  public Optional<String> mouthBone() {
    return this.bonesIn(Slot.MOUTH).stream().findFirst();
  }

  public Optional<Identifier> sheetOverride(final Identifier sheet, final Slot slot) {
    return Optional.ofNullable(this.sheetOverrides.getOrDefault(sheet, Map.of()).get(slot));
  }

  public Optional<Identifier> overlay(final Overlay overlay) {
    return Optional.ofNullable(this.overlays.get(overlay));
  }
}
