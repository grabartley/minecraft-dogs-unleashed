package com.grahambartley.dogsunleashed.util;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

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
