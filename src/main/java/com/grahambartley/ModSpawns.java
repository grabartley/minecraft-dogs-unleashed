package com.grahambartley;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.BiomeKeys;

public class ModSpawns {

  public static void initialize() {
    BiomeModifications.addSpawn(
        BiomeSelectors.includeByKey(
            BiomeKeys.SNOWY_TAIGA,
            BiomeKeys.SNOWY_PLAINS,
            BiomeKeys.ICE_SPIKES,
            BiomeKeys.FROZEN_PEAKS,
            BiomeKeys.SNOWY_SLOPES,
            BiomeKeys.GROVE),
        SpawnGroup.CREATURE,
        ModEntities.HUSKY,
        ModConstants.HUSKY_SPAWN_WEIGHT,
        ModConstants.HUSKY_SPAWN_MIN_GROUP,
        ModConstants.HUSKY_SPAWN_MAX_GROUP);

    SpawnRestriction.register(
        ModEntities.HUSKY,
        SpawnLocationTypes.ON_GROUND,
        Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
        AnimalEntity::isValidNaturalSpawn);
  }
}
