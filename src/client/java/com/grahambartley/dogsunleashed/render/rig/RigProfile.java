package com.grahambartley.dogsunleashed.render.rig;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.grahambartley.dogsunleashed.riding.RiderPoseRange;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record RigProfile(
    Map<Slot, Set<String>> bones,
    Map<Overlay, Identifier> overlays,
    List<Texel> pupils,
    Map<Identifier, Map<Slot, Identifier>> sheetOverrides,
    List<String> seats,
    RiderPoseRange riderPose,
    RigEars ears,
    Map<String, Slot> suitSlots) {

  private static final Logger LOGGER = LoggerFactory.getLogger(RigProfile.class);

  private static final String PUPILS_KEY = "pupils";
  private static final String SHEET_OVERRIDES_KEY = "sheet_overrides";
  private static final String SEATS_KEY = "seats";
  private static final String RIDER_POSE_KEY = "rider_pose";
  private static final String EARS_KEY = "ears";
  private static final String SUIT_SLOTS_KEY = "suit_slots";

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

    public boolean isFur() {
      return this == FUR_SHAGGY || this == FUR_OVERGROWN;
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
    seats = List.copyOf(seats);
    suitSlots = Map.copyOf(suitSlots);
    sheetOverrides =
        sheetOverrides.entrySet().stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    Map.Entry::getKey, entry -> Map.copyOf(entry.getValue())));
  }

  public RigProfile(
      final Map<Slot, Set<String>> bones,
      final Map<Overlay, Identifier> overlays,
      final List<Texel> pupils,
      final Map<Identifier, Map<Slot, Identifier>> sheetOverrides) {
    this(bones, overlays, pupils, sheetOverrides, List.of(), RiderPoseRange.UPRIGHT);
  }

  public RigProfile(
      final Map<Slot, Set<String>> bones,
      final Map<Overlay, Identifier> overlays,
      final List<Texel> pupils,
      final Map<Identifier, Map<Slot, Identifier>> sheetOverrides,
      final List<String> seats,
      final RiderPoseRange riderPose) {
    this(bones, overlays, pupils, sheetOverrides, seats, riderPose, RigEars.NONE, Map.of());
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
    return new RigProfile(
        bones,
        overlays,
        pupils,
        sheetOverridesOf(json),
        seatsOf(json),
        json.get(RIDER_POSE_KEY) instanceof JsonObject pose
            ? RiderPoseRange.parse(pose)
            : RiderPoseRange.UPRIGHT,
        json.has(EARS_KEY) ? RigEars.parse(json.get(EARS_KEY)) : RigEars.NONE,
        suitSlotsOf(json, bones));
  }

  private static Map<String, Slot> suitSlotsOf(
      final JsonObject json, final Map<Slot, Set<String>> bones) {
    final Map<String, Slot> slots = new HashMap<>();
    if (!(json.get(SUIT_SLOTS_KEY) instanceof JsonObject byBone)) {
      if (json.has(SUIT_SLOTS_KEY)) {
        LOGGER.warn("Ignoring suit slots {}, expected an object", json.get(SUIT_SLOTS_KEY));
      }
      return slots;
    }
    for (final Map.Entry<String, JsonElement> entry : byBone.entrySet()) {
      final Optional<Slot> slot =
          entry.getValue() instanceof JsonPrimitive value && value.isString()
              ? slotOf(value.getAsString())
              : Optional.empty();
      final boolean named = bones.values().stream().anyMatch(set -> set.contains(entry.getKey()));
      if (slot.isEmpty() || slot.get().isFur() || !named) {
        LOGGER.warn("Skipping suit slot {} of bone {}", entry.getValue(), entry.getKey());
        continue;
      }
      slots.put(entry.getKey(), slot.get());
    }
    return slots;
  }

  private static List<String> seatsOf(final JsonObject json) {
    final List<String> seats = new ArrayList<>();
    if (json.get(SEATS_KEY) instanceof JsonArray names) {
      for (final JsonElement name : names) {
        if (name.isJsonPrimitive() && name.getAsJsonPrimitive().isString()) {
          seats.add(name.getAsString());
        } else {
          LOGGER.warn("Skipping seat {}", name);
        }
      }
    } else if (json.has(SEATS_KEY)) {
      LOGGER.warn("Ignoring seats {}, expected a list of bone names", json.get(SEATS_KEY));
    }
    return seats;
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
        if (slot.isEmpty() || slot.get().isFur() || texture == null) {
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

  public static Optional<Slot> slotOf(final String key) {
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

  public Set<String> suitBonesIn(final Slot slot) {
    return Stream.concat(
            this.bonesIn(slot).stream()
                .filter(bone -> this.suitSlots.getOrDefault(bone, slot) == slot),
            this.suitSlots.entrySet().stream()
                .filter(entry -> entry.getValue() == slot)
                .map(Map.Entry::getKey))
        .collect(Collectors.toUnmodifiableSet());
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
