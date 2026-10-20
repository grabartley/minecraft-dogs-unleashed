package com.grahambartley.dogsunleashed;

import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;

public final class ModPointsOfInterest {

  private static final int SEARCH_DISTANCE = 1;

  public static final RegistryKey<PointOfInterestType> FOOD_BOWL =
      register("food_bowl", ModBlocks.FOOD_BOWL);
  public static final RegistryKey<PointOfInterestType> WATER_BOWL =
      register("water_bowl", ModBlocks.WATER_BOWL);

  private ModPointsOfInterest() {}

  private static RegistryKey<PointOfInterestType> register(final String path, final Block block) {
    final Identifier id = Identifier.of(DogsUnleashed.MOD_ID, path);
    PointOfInterestHelper.register(id, 0, SEARCH_DISTANCE, block);
    return RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, id);
  }

  public static void initialize() {}
}
