package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.mixin.ServerWorldSpawnersAccessor;
import com.grahambartley.dogsunleashed.spawner.DogSpawner;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.biome.v1.BiomeModification;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.spawner.SpecialSpawner;

public class ModSpawns {

  public static void initialize() {
    final BiomeModification spawnModification =
        BiomeModifications.create(Identifier.of(DogsUnleashed.MOD_ID, "dog_spawns"));
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      if (!breed.isNaturallySpawning()) {
        continue;
      }
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

    ServerWorldEvents.LOAD.register(
        (server, world) -> {
          if (!World.OVERWORLD.equals(world.getRegistryKey())) {
            return;
          }
          final ServerWorldSpawnersAccessor accessor = (ServerWorldSpawnersAccessor) world;
          final List<SpecialSpawner> spawners =
              new ArrayList<>(accessor.dogsUnleashed$getSpawners());
          spawners.add(new DogSpawner());
          accessor.dogsUnleashed$setSpawners(List.copyOf(spawners));
        });
  }
}
