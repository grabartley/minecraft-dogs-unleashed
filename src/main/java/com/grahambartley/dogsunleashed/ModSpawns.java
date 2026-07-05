package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModification;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.SpawnSettings;

public class ModSpawns {

  public static void initialize() {
    // Biome modifications bake once per server start, after ServerConfigService.loadFromWorld has
    // populated SERVER_CONFIG. Resolving the config inside the callback (rather than the eager
    // BiomeModifications.addSpawn overload) means spawn toggles and rate multipliers read the
    // world's config with restart-required semantics instead of freezing values at mod init.
    final BiomeModification spawnModification =
        BiomeModifications.create(Identifier.of(DogsUnleashed.MOD_ID, "dog_spawns"));
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      final UnleashedDogBreed.SpawnSettings spawnSettings = breed.spawnSettings();
      spawnModification.add(
          ModificationPhase.ADDITIONS,
          BiomeSelectors.includeByKey(spawnSettings.biomes()),
          context -> {
            final DogsUnleashedConfig config = DogsUnleashed.SERVER_CONFIG;
            if (!config.enableNaturalSpawning()) {
              return;
            }
            final int weight =
                config.effectiveSpawnWeight(spawnSettings.weight(), breed.serializedId());
            if (weight == 0) {
              return;
            }
            context
                .getSpawnSettings()
                .addSpawn(
                    SpawnGroup.CREATURE,
                    new SpawnSettings.SpawnEntry(
                        ModEntities.getDogEntityType(breed),
                        weight,
                        spawnSettings.minGroupSize(),
                        spawnSettings.maxGroupSize()));
          });

      SpawnRestriction.register(
          ModEntities.getDogEntityType(breed),
          SpawnLocationTypes.ON_GROUND,
          Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
          UnleashedDogEntity::canSpawn);
    }
  }
}
