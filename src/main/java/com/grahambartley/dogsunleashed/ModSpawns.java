package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.Heightmap;

public class ModSpawns {

  public static void initialize() {
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      final UnleashedDogBreed.SpawnSettings spawnSettings = breed.spawnSettings();
      // BiomeModifications predicate is evaluated at world load, so
      // SERVER_CONFIG.enableNaturalSpawning
      // captures the flag value at the moment biomes are frozen. Changes apply on next world load.
      BiomeModifications.addSpawn(
          BiomeSelectors.includeByKey(spawnSettings.biomes())
              .and(ctx -> DogsUnleashed.SERVER_CONFIG.enableNaturalSpawning()),
          SpawnGroup.CREATURE,
          ModEntities.getDogEntityType(breed),
          spawnSettings.weight(),
          spawnSettings.minGroupSize(),
          spawnSettings.maxGroupSize());

      SpawnRestriction.register(
          ModEntities.getDogEntityType(breed),
          SpawnLocationTypes.ON_GROUND,
          Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
          AnimalEntity::isValidNaturalSpawn);
    }
  }
}
