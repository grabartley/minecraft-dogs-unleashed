package com.grahambartley.dogsunleashed.render.coat;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A coat taken apart into the pieces the compositor can recombine: one pigment colour per region,
 * and the scale the base layer was encoded against.
 *
 * <p>A pixel is rebuilt as {@code pigment * base / baseScale}, which keeps the painted shading and
 * hue variation intact through a recolour. Slots are ordered lightest pigment first, so blending
 * two coats pairs like with like.
 */
public record CoatRecipe(
    List<String> slots, Map<String, Integer> pigments, Map<String, Integer> baseScale) {

  public static final int DEFAULT_BASE_SCALE = 128;

  public CoatRecipe {
    slots = List.copyOf(slots);
    pigments = Map.copyOf(pigments);
    baseScale = Map.copyOf(baseScale);
  }

  public int pigment(final String slot) {
    return this.pigments.getOrDefault(slot, 0xFFFFFF);
  }

  public int baseScaleOf(final String slot) {
    return this.baseScale.getOrDefault(slot, DEFAULT_BASE_SCALE);
  }

  /** Returns the pigment for {@code index}, clamped to the last slot for shorter recipes. */
  public int pigmentAt(final int index) {
    if (this.slots.isEmpty()) {
      return 0xFFFFFF;
    }
    return pigment(this.slots.get(Math.min(index, this.slots.size() - 1)));
  }

  public static CoatRecipe fromJson(final JsonObject json) {
    final JsonObject pigmentsJson = json.getAsJsonObject("pigments");
    final JsonObject scalesJson = json.getAsJsonObject("baseScale");
    final List<String> slots = new ArrayList<>(pigmentsJson.keySet());
    slots.sort(String::compareTo);

    final Map<String, Integer> pigments = new LinkedHashMap<>();
    final Map<String, Integer> scales = new LinkedHashMap<>();
    for (final String slot : slots) {
      pigments.put(slot, Integer.parseInt(pigmentsJson.get(slot).getAsString().substring(1), 16));
      scales.put(
          slot,
          scalesJson != null && scalesJson.has(slot)
              ? scalesJson.get(slot).getAsInt()
              : DEFAULT_BASE_SCALE);
    }
    return new CoatRecipe(slots, pigments, scales);
  }
}
