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

    BiomeModifications.addSpawn(
        BiomeSelectors.includeByKey(BiomeKeys.PLAINS),
        SpawnGroup.CREATURE,
        ModEntities.DACHSHUND,
        ModConstants.DACHSHUND_SPAWN_WEIGHT,
        ModConstants.DACHSHUND_SPAWN_MIN_GROUP,
        ModConstants.DACHSHUND_SPAWN_MAX_GROUP);

    BiomeModifications.addSpawn(
        BiomeSelectors.includeByKey(BiomeKeys.FLOWER_FOREST),
        SpawnGroup.CREATURE,
        ModEntities.BEAGLE,
        ModConstants.BEAGLE_SPAWN_WEIGHT,
        ModConstants.BEAGLE_SPAWN_MIN_GROUP,
        ModConstants.BEAGLE_SPAWN_MAX_GROUP);

    BiomeModifications.addSpawn(
        BiomeSelectors.includeByKey(BiomeKeys.BEACH),
        SpawnGroup.CREATURE,
        ModEntities.GOLDEN_RETRIEVER,
        ModConstants.GOLDEN_RETRIEVER_SPAWN_WEIGHT,
        ModConstants.GOLDEN_RETRIEVER_SPAWN_MIN_GROUP,
        ModConstants.GOLDEN_RETRIEVER_SPAWN_MAX_GROUP);

    BiomeModifications.addSpawn(
        BiomeSelectors.includeByKey(BiomeKeys.CHERRY_GROVE),
        SpawnGroup.CREATURE,
        ModEntities.SHIBA_INU,
        ModConstants.SHIBA_INU_SPAWN_WEIGHT,
        ModConstants.SHIBA_INU_SPAWN_MIN_GROUP,
        ModConstants.SHIBA_INU_SPAWN_MAX_GROUP);

    SpawnRestriction.register(
        ModEntities.HUSKY,
        SpawnLocationTypes.ON_GROUND,
        Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
        AnimalEntity::isValidNaturalSpawn);

    SpawnRestriction.register(
        ModEntities.DACHSHUND,
        SpawnLocationTypes.ON_GROUND,
        Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
        AnimalEntity::isValidNaturalSpawn);

    SpawnRestriction.register(
        ModEntities.BEAGLE,
        SpawnLocationTypes.ON_GROUND,
        Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
        AnimalEntity::isValidNaturalSpawn);

    SpawnRestriction.register(
        ModEntities.GOLDEN_RETRIEVER,
        SpawnLocationTypes.ON_GROUND,
        Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
        AnimalEntity::isValidNaturalSpawn);

    SpawnRestriction.register(
        ModEntities.SHIBA_INU,
        SpawnLocationTypes.ON_GROUND,
        Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
        AnimalEntity::isValidNaturalSpawn);
  }
}
