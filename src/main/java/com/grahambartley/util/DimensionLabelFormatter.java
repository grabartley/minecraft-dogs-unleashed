package com.grahambartley.util;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * Maps a dimension identifier to a human-readable label for display in UI rows.
 *
 * <p>The three vanilla dimensions render as {@code Overworld}, {@code Nether}, and {@code The End}.
 * Any other dimension falls back to its identifier path with the namespace stripped and underscores
 * turned into spaces, so a modded dimension like {@code twilightforest:twilight_forest} reads as
 * {@code twilight forest} instead of leaking the raw identifier.
 */
public final class DimensionLabelFormatter {

  private DimensionLabelFormatter() {}

  public static String format(final String dimensionId) {
    if (dimensionId == null || dimensionId.isEmpty()) {
      return "";
    }
    final RegistryKey<World> worldKey =
        RegistryKey.of(RegistryKeys.WORLD, Identifier.of(dimensionId));
    if (worldKey == World.OVERWORLD) {
      return "Overworld";
    }
    if (worldKey == World.NETHER) {
      return "Nether";
    }
    if (worldKey == World.END) {
      return "The End";
    }
    final int colon = dimensionId.indexOf(':');
    final String path = colon >= 0 ? dimensionId.substring(colon + 1) : dimensionId;
    return path.replace('_', ' ');
  }
}
