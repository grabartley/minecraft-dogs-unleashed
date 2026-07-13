package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings;

/**
 * Verifies the spawn entries that {@code ModSpawns}' biome modification callback bakes into the
 * server's biome registry. The gametest server has no world config file, so {@code SERVER_CONFIG}
 * holds defaults (all multipliers at 100%), and every breed must appear in each of its configured
 * biomes with exactly its base weight and group sizes. This pins the acceptance criterion that
 * default multipliers leave baked spawn entries identical to pre-multiplier behavior, and catches
 * regressions in the bake-time config resolution (weight math, enableNaturalSpawning fold-in, and
 * the config-before-bake load ordering).
 */
public final class DogSpawnRegistrationGameTest implements FabricGameTest {

  private static final int TICK_LIMIT = 20;

  @CustomTestProvider
  public List<TestFunction> bakedSpawnEntryMatchesBaseSettingsPerBreed() {
    return DogTestData.getAllBreeds().stream()
        .map(
            data ->
                new TestFunction(
                    "defaultBatch",
                    "dogspawnregistrationtest.bakedentry." + data.breed().serializedId(),
                    FabricGameTest.EMPTY_STRUCTURE,
                    TICK_LIMIT,
                    0L,
                    true,
                    ctx -> testBakedSpawnEntries(ctx, data)))
        .toList();
  }

  private void testBakedSpawnEntries(
      final TestContext context, final DogTestData<? extends UnleashedDogEntity> data) {
    final UnleashedDogBreed.SpawnSettings breedSettings = data.breed().spawnSettings();
    final Registry<Biome> biomes = context.getWorld().getRegistryManager().get(RegistryKeys.BIOME);

    for (final RegistryKey<Biome> biomeKey : breedSettings.biomes()) {
      final Biome biome = biomes.get(biomeKey);
      context.assertTrue(
          biome != null, "Biome " + biomeKey.getValue() + " must exist in the registry");

      final SpawnSettings.SpawnEntry entry =
          biome.getSpawnSettings().getSpawnEntries(SpawnGroup.CREATURE).getEntries().stream()
              .filter(candidate -> candidate.type == data.entityType())
              .findFirst()
              .orElse(null);
      final String breedInBiome = data.breed().serializedId() + " in " + biomeKey.getValue();

      context.assertTrue(entry != null, breedInBiome + " must have a baked CREATURE spawn entry");
      context.assertTrue(
          entry.getWeight().getValue() == breedSettings.weight(),
          breedInBiome
              + " baked weight should be "
              + breedSettings.weight()
              + " at default multipliers but was "
              + entry.getWeight().getValue());
      context.assertTrue(
          entry.minGroupSize == breedSettings.minGroupSize()
              && entry.maxGroupSize == breedSettings.maxGroupSize(),
          breedInBiome
              + " baked group sizes should be "
              + breedSettings.minGroupSize()
              + ".."
              + breedSettings.maxGroupSize()
              + " but were "
              + entry.minGroupSize
              + ".."
              + entry.maxGroupSize);
    }
    context.complete();
  }
}
