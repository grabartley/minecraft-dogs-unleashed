package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.HuskyEntity;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.spawner.DogSpawner;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

/**
 * Verifies the observable decisions of the cap-independent {@code DogSpawner}: the config gates
 * that keep it inert (its own toggle, natural spawning, the spawn-animals flag, and the {@code
 * doMobSpawning} gamerule), breed candidate resolution from biome registry entries, the untamed
 * self-cap counting, and the despawn-flag policy on spawned dogs including its NBT round trip and
 * removal on taming. Tests that flip {@code SERVER_CONFIG} mutate and restore it synchronously
 * inside one test body; the server ticks tests on a single thread, so no other test can observe the
 * temporary value. End-to-end pack spawning is exercised in manual QA because a real attempt places
 * entities at uncontrolled world positions outside any test structure.
 */
public final class DogSpawnerGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final int TICK_LIMIT = 20;
  private static final int TOGGLE_OFF_SPAWN_CALLS = 3000;

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = TICK_LIMIT)
  public void spawnerIsInertWhenToggleOff(final TestContext context) {
    final DogSpawner spawner = new DogSpawner();
    final int creaturesBefore = countCreatures(context);
    int spawnedTotal = 0;
    for (int i = 0; i < TOGGLE_OFF_SPAWN_CALLS; i++) {
      spawnedTotal += spawner.spawn(context.getWorld(), true, true);
    }
    context.assertTrue(
        spawnedTotal == 0, "Spawner should never spawn with the toggle off (default config)");
    context.assertTrue(
        countCreatures(context) == creaturesBefore,
        "Spawner should not have changed the world's creature count with the toggle off");
    context.complete();
  }

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = TICK_LIMIT)
  public void spawnerIsInertWhenNaturalSpawningDisabled(final TestContext context) {
    final DogsUnleashedConfig original = DogsUnleashed.SERVER_CONFIG;
    try {
      DogsUnleashed.SERVER_CONFIG =
          DogsUnleashedConfig.defaults()
              .withCapIndependentSpawningEnabled(true)
              .withEnableNaturalSpawning(false);
      final int spawned = new DogSpawner().spawn(context.getWorld(), true, true);
      context.assertTrue(
          spawned == 0, "Spawner should be inert when enableNaturalSpawning is false");
    } finally {
      DogsUnleashed.SERVER_CONFIG = original;
    }
    context.complete();
  }

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = TICK_LIMIT)
  public void spawnerRespectsSpawnAnimalsFlag(final TestContext context) {
    final DogsUnleashedConfig original = DogsUnleashed.SERVER_CONFIG;
    try {
      DogsUnleashed.SERVER_CONFIG =
          DogsUnleashedConfig.defaults().withCapIndependentSpawningEnabled(true);
      final int spawned = new DogSpawner().spawn(context.getWorld(), true, false);
      context.assertTrue(spawned == 0, "Spawner should be inert when spawnAnimals is false");
    } finally {
      DogsUnleashed.SERVER_CONFIG = original;
    }
    context.complete();
  }

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = TICK_LIMIT)
  public void spawnerRespectsDoMobSpawningGamerule(final TestContext context) {
    context.assertFalse(
        context.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING),
        "Precondition: the gametest server runs with doMobSpawning disabled");
    final DogsUnleashedConfig original = DogsUnleashed.SERVER_CONFIG;
    try {
      DogsUnleashed.SERVER_CONFIG =
          DogsUnleashedConfig.defaults().withCapIndependentSpawningEnabled(true);
      final int spawned = new DogSpawner().spawn(context.getWorld(), true, true);
      context.assertTrue(spawned == 0, "Spawner should be inert while doMobSpawning is false");
    } finally {
      DogsUnleashed.SERVER_CONFIG = original;
    }
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void untamedDogCountTracksSpawnsAndTaming(final TestContext context) {
    final BlockPos absCenter = context.getAbsolutePos(new BlockPos(3, 2, 3));
    // Delta-based against a baseline so untamed dogs from neighboring test structures within the
    // 64-block radius cannot fail this test.
    final int baseline = DogSpawner.countUntamedDogsNear(context.getWorld(), absCenter);

    final HuskyEntity first =
        DogTestHelper.spawnDog(context, DogTestData.HUSKY, new BlockPos(2, 2, 2));
    first.setAiDisabled(true);
    final HuskyEntity second =
        DogTestHelper.spawnDog(context, DogTestData.HUSKY, new BlockPos(4, 2, 4));
    second.setAiDisabled(true);
    final HuskyEntity tamed =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, new BlockPos(3, 2, 4));
    tamed.setAiDisabled(true);

    context.assertTrue(
        DogSpawner.countUntamedDogsNear(context.getWorld(), absCenter) == baseline + 2,
        "Two untamed dogs should count toward the cap; the tamed dog should not");

    first.setTamed(true, true);
    context.assertTrue(
        DogSpawner.countUntamedDogsNear(context.getWorld(), absCenter) == baseline + 1,
        "Taming a dog should remove it from the untamed cap count");
    context.complete();
  }

  @CustomTestProvider
  public List<TestFunction> breedCandidateResolutionPerBreed() {
    return DogTestData.getAllBreeds().stream()
        .map(
            data ->
                new TestFunction(
                    "defaultBatch",
                    "dogspawnertest.breedcandidates." + data.breed().serializedId(),
                    EMPTY_STRUCTURE,
                    TICK_LIMIT,
                    0L,
                    true,
                    ctx -> testBreedCandidateResolution(ctx, data.breed())))
        .toList();
  }

  private void testBreedCandidateResolution(
      final TestContext context, final UnleashedDogBreed breed) {
    final Registry<Biome> biomes = context.getWorld().getRegistryManager().get(RegistryKeys.BIOME);
    final DogsUnleashedConfig defaults = DogsUnleashedConfig.defaults();
    final DogsUnleashedConfig breedDisabled =
        defaults.withBreedSpawnRateMultiplierPercent(breed.serializedId(), 0);

    for (final RegistryKey<Biome> biomeKey : breed.spawnSettings().biomes()) {
      final RegistryEntry<Biome> entry = biomes.entryOf(biomeKey);
      context.assertTrue(
          DogSpawner.spawnableBreedsIn(entry, defaults).contains(breed),
          breed.serializedId() + " should be a candidate in " + biomeKey.getValue());
      context.assertFalse(
          DogSpawner.spawnableBreedsIn(entry, breedDisabled).contains(breed),
          breed.serializedId() + " at 0% should not be a candidate in " + biomeKey.getValue());
    }
    context.complete();
  }

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = TICK_LIMIT)
  public void noBreedIsCandidateInDesert(final TestContext context) {
    final Registry<Biome> biomes = context.getWorld().getRegistryManager().get(RegistryKeys.BIOME);
    final List<UnleashedDogBreed> candidates =
        DogSpawner.spawnableBreedsIn(
            biomes.entryOf(BiomeKeys.DESERT), DogsUnleashedConfig.defaults());
    context.assertTrue(
        candidates.isEmpty(), "No dog breed lists desert, so it should have no candidates");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void despawnFlagRoundTripsThroughNbt(final TestContext context) {
    final HuskyEntity original =
        DogTestHelper.spawnDog(context, DogTestData.HUSKY, new BlockPos(2, 2, 2));
    original.setAiDisabled(true);
    original.setSpawnedByDogSpawner(true);

    final NbtCompound nbt = new NbtCompound();
    original.writeCustomDataToNbt(nbt);

    final HuskyEntity restored =
        DogTestHelper.spawnDog(context, DogTestData.HUSKY, new BlockPos(4, 2, 4));
    restored.setAiDisabled(true);
    restored.readCustomDataFromNbt(nbt);

    context.assertTrue(
        restored.isSpawnedByDogSpawner(),
        "SpawnedByDogSpawner should survive an NBT write/read round trip");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void untamedSpawnerDogIsDespawnableAndTamingMakesItPermanent(final TestContext context) {
    final HuskyEntity dog =
        DogTestHelper.spawnDog(context, DogTestData.HUSKY, new BlockPos(3, 2, 3));
    dog.setAiDisabled(true);
    dog.setSpawnedByDogSpawner(true);

    context.assertTrue(
        dog.canImmediatelyDespawn(0.0),
        "An untamed spawner-spawned dog should be eligible to despawn");

    dog.setTamed(true, true);
    context.assertFalse(
        dog.isSpawnedByDogSpawner(), "Taming should clear the spawner-spawned flag");
    context.assertFalse(
        dog.canImmediatelyDespawn(0.0), "A tamed dog should never be eligible to despawn");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void chunkGenerationDogsKeepPermanentPersistence(final TestContext context) {
    final HuskyEntity dog =
        DogTestHelper.spawnDog(context, DogTestData.HUSKY, new BlockPos(3, 2, 3));
    dog.setAiDisabled(true);

    context.assertFalse(
        dog.isSpawnedByDogSpawner(), "Dogs not spawned by the DogSpawner should carry no flag");
    context.assertFalse(
        dog.canImmediatelyDespawn(0.0),
        "An unflagged wild dog should keep vanilla animal persistence");
    context.complete();
  }

  private static int countCreatures(final TestContext context) {
    int count = 0;
    for (final var entity : context.getWorld().iterateEntities()) {
      if (entity.getType().getSpawnGroup() == SpawnGroup.CREATURE) {
        count++;
      }
    }
    return count;
  }
}
