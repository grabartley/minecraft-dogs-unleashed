package com.grahambartley;

import com.grahambartley.entity.UnleashedDogBreed;
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
      BiomeModifications.addSpawn(
          BiomeSelectors.includeByKey(spawnSettings.biomes()),
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
