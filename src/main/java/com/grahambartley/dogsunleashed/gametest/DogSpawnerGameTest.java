package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.mixin.ServerWorldSpawnersAccessor;
import com.grahambartley.dogsunleashed.spawner.DogSpawner;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

/**
 * Verifies the observable behavior of the cap-independent {@code DogSpawner}: every config and flag
 * gate that keeps it inert, its registration exactly once in the overworld spawner list (and
 * absence from other dimensions), untamed self-cap counting including the radius boundary, breed
 * candidate resolution from live biome registry entries (exact per-biome sets, per-breed and global
 * opt-outs), and the despawn-flag policy on dogs including its NBT round trip in both directions,
 * removal on taming, and absence on chunk-generation dogs. Tests that flip {@code SERVER_CONFIG}
 * mutate and restore it synchronously inside one test body; the server ticks tests on a single
 * thread, so no other test can observe the temporary value. End-to-end pack spawning is exercised
 * in manual QA because a real attempt places entities at uncontrolled world positions outside any
 * test structure.
 */
public final class DogSpawnerGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String BATCH = "defaultBatch";
  private static final int TICK_LIMIT = 20;
  private static final int INERT_SPAWN_CALLS = 3000;
  private static final BlockPos REL_ARENA_CENTER = new BlockPos(3, 2, 3);

  private record InertnessCase(
      String id,
      UnaryOperator<DogsUnleashedConfig> config,
      boolean spawnAnimals,
      boolean expectsMobSpawningGameruleOff) {}

  private static final List<InertnessCase> INERTNESS_CASES =
      List.of(
          new InertnessCase(
              "optedout", cfg -> cfg.withCapIndependentSpawningEnabled(false), true, false),
          new InertnessCase(
              "naturalspawningoff", cfg -> cfg.withEnableNaturalSpawning(false), true, false),
          new InertnessCase("spawnanimalsoff", UnaryOperator.identity(), false, false),
          new InertnessCase("mobspawninggameruleoff", UnaryOperator.identity(), true, true));

  @CustomTestProvider
  public List<TestFunction> spawnerIsInertPerDisabledGate() {
    return INERTNESS_CASES.stream()
        .map(
            inertnessCase ->
                new TestFunction(
                    BATCH,
                    "dogspawnertest.inert." + inertnessCase.id(),
                    EMPTY_STRUCTURE,
                    TICK_LIMIT,
                    0L,
                    true,
                    ctx -> testSpawnerIsInert(ctx, inertnessCase)))
        .toList();
  }

  private void testSpawnerIsInert(final TestContext context, final InertnessCase inertnessCase) {
    if (inertnessCase.expectsMobSpawningGameruleOff()) {
      context.assertFalse(
          context.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING),
          "Precondition: the gametest server runs with doMobSpawning disabled");
    }
    final DogsUnleashedConfig original = DogsUnleashed.SERVER_CONFIG;
    try {
      DogsUnleashed.SERVER_CONFIG = inertnessCase.config().apply(DogsUnleashedConfig.defaults());
      final DogSpawner spawner = new DogSpawner();
      final int creaturesBefore = countCreatures(context);
      int spawnedTotal = 0;
      for (int i = 0; i < INERT_SPAWN_CALLS; i++) {
        spawnedTotal += spawner.spawn(context.getWorld(), true, inertnessCase.spawnAnimals());
      }
      context.assertTrue(
          spawnedTotal == 0, "Spawner should never spawn for gate " + inertnessCase.id());
      context.assertTrue(
          countCreatures(context) == creaturesBefore,
          "Spawner should not change the world's creature count for gate " + inertnessCase.id());
    } finally {
      DogsUnleashed.SERVER_CONFIG = original;
    }
    context.complete();
  }

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = TICK_LIMIT)
  public void spawnerIsRegisteredExactlyOnceInOverworld(final TestContext context) {
    final ServerWorld overworld = context.getWorld().getServer().getWorld(World.OVERWORLD);
    context.assertTrue(overworld != null, "Overworld must exist");
    final long dogSpawners = countDogSpawners(overworld);
    context.assertTrue(
        dogSpawners == 1,
        "Overworld should carry exactly one registered DogSpawner but had " + dogSpawners);
    context.assertTrue(
        ((ServerWorldSpawnersAccessor) overworld).dogsUnleashed$getSpawners().size() > 1,
        "Registration should append to the vanilla spawner list, not replace it");
    context.complete();
  }

  @CustomTestProvider
  public List<TestFunction> spawnerAbsentPerNonOverworldDimension() {
    return Stream.of(World.NETHER, World.END)
        .map(
            dimension ->
                new TestFunction(
                    BATCH,
                    "dogspawnertest.notregistered." + dimension.getValue().getPath(),
                    EMPTY_STRUCTURE,
                    TICK_LIMIT,
                    0L,
                    true,
                    ctx -> testSpawnerAbsentIn(ctx, dimension)))
        .toList();
  }

  private void testSpawnerAbsentIn(final TestContext context, final RegistryKey<World> dimension) {
    final ServerWorld world = context.getWorld().getServer().getWorld(dimension);
    context.assertTrue(world != null, dimension.getValue() + " must exist");
    final long dogSpawners = countDogSpawners(world);
    context.assertTrue(
        dogSpawners == 0,
        dimension.getValue() + " should carry no DogSpawner but had " + dogSpawners);
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void untamedDogCountTracksSpawnsAndTaming(final TestContext context) {
    final BlockPos absCenter = context.getAbsolutePos(REL_ARENA_CENTER);
    // Delta-based against a baseline so untamed dogs from neighboring test structures within the
    // 64-block radius cannot fail this test; the body runs synchronously in one tick, so no other
    // test can change the count between the baseline and the assertions.
    final int baseline = DogSpawner.countUntamedDogsNear(context.getWorld(), absCenter);

    final List<UnleashedDogEntity> untamed =
        DogTestData.getAllBreeds().stream()
            .map(
                data ->
                    (UnleashedDogEntity) DogTestHelper.spawnDog(context, data, REL_ARENA_CENTER))
            .peek(dog -> dog.setAiDisabled(true))
            .toList();
    final UnleashedDogEntity tamed =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, new BlockPos(3, 2, 4));
    tamed.setAiDisabled(true);

    context.assertTrue(
        DogSpawner.countUntamedDogsNear(context.getWorld(), absCenter) == baseline + untamed.size(),
        "Every untamed dog of every breed should count toward the cap; the tamed dog should not");

    untamed.get(0).setTamed(true, true);
    context.assertTrue(
        DogSpawner.countUntamedDogsNear(context.getWorld(), absCenter)
            == baseline + untamed.size() - 1,
        "Taming a dog should remove it from the untamed cap count");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TICK_LIMIT)
  public void untamedDogsBeyondCapRadiusAreNotCounted(final TestContext context) {
    final BlockPos farCenter =
        context
            .getAbsolutePos(REL_ARENA_CENTER)
            .add(DogSpawner.UNTAMED_DOG_CAP_RADIUS_BLOCKS + 16, 0, 0);
    final int baseline = DogSpawner.countUntamedDogsNear(context.getWorld(), farCenter);
    final int nearBaseline =
        DogSpawner.countUntamedDogsNear(
            context.getWorld(), context.getAbsolutePos(REL_ARENA_CENTER));

    final UnleashedDogEntity first =
        DogTestHelper.spawnDog(context, DogTestData.HUSKY, new BlockPos(2, 2, 2));
    first.setAiDisabled(true);
    final UnleashedDogEntity second =
        DogTestHelper.spawnDog(context, DogTestData.BEAGLE, new BlockPos(4, 2, 4));
    second.setAiDisabled(true);

    context.assertTrue(
        DogSpawner.countUntamedDogsNear(
                context.getWorld(), context.getAbsolutePos(REL_ARENA_CENTER))
            == nearBaseline + 2,
        "Precondition: both dogs count from a center inside the structure");
    context.assertTrue(
        DogSpawner.countUntamedDogsNear(context.getWorld(), farCenter) == baseline,
        "Dogs beyond the cap radius should not count toward the cap");
    context.complete();
  }

  @CustomTestProvider
  public List<TestFunction> breedCandidateResolutionPerBreed() {
    return DogTestData.getAllBreeds().stream()
        .map(
            data ->
                new TestFunction(
                    BATCH,
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
    final Registry<Biome> biomes = biomeRegistry(context);
    final DogsUnleashedConfig defaults = DogsUnleashedConfig.defaults();
    final DogsUnleashedConfig breedDisabled =
        defaults.withBreedSpawnRateMultiplierPercent(breed.serializedId(), 0);
    final DogsUnleashedConfig globalDisabled = defaults.withSpawnRateMultiplierPercent(0);

    for (final RegistryKey<Biome> biomeKey : breed.spawnSettings().biomes()) {
      final RegistryEntry<Biome> entry = biomes.entryOf(biomeKey);
      context.assertTrue(
          DogSpawner.spawnableBreedsIn(entry, defaults).contains(breed),
          breed.serializedId() + " should be a candidate in " + biomeKey.getValue());
      context.assertFalse(
          DogSpawner.spawnableBreedsIn(entry, breedDisabled).contains(breed),
          breed.serializedId() + " at 0% should not be a candidate in " + biomeKey.getValue());
      context.assertFalse(
          DogSpawner.spawnableBreedsIn(entry, globalDisabled).contains(breed),
          breed.serializedId()
              + " should not be a candidate in "
              + biomeKey.getValue()
              + " at 0% global spawn rate");
    }
    context.complete();
  }

  private record BiomeCandidatesCase(RegistryKey<Biome> biome, List<UnleashedDogBreed> expected) {}

  private static final List<BiomeCandidatesCase> BIOME_CANDIDATES_CASES =
      List.of(
          new BiomeCandidatesCase(BiomeKeys.SNOWY_TAIGA, List.of(UnleashedDogBreed.HUSKY)),
          new BiomeCandidatesCase(
              BiomeKeys.MEADOW, List.of(UnleashedDogBreed.DACHSHUND, UnleashedDogBreed.BEAGLE)),
          new BiomeCandidatesCase(BiomeKeys.BEACH, List.of(UnleashedDogBreed.GOLDEN_RETRIEVER)),
          new BiomeCandidatesCase(BiomeKeys.CHERRY_GROVE, List.of(UnleashedDogBreed.SHIBA_INU)),
          new BiomeCandidatesCase(BiomeKeys.DESERT, List.of()),
          new BiomeCandidatesCase(BiomeKeys.OCEAN, List.of()));

  @CustomTestProvider
  public List<TestFunction> exactCandidateSetPerBiome() {
    return BIOME_CANDIDATES_CASES.stream()
        .map(
            biomeCase ->
                new TestFunction(
                    BATCH,
                    "dogspawnertest.biomecandidates." + biomeCase.biome().getValue().getPath(),
                    EMPTY_STRUCTURE,
                    TICK_LIMIT,
                    0L,
                    true,
                    ctx -> testExactCandidateSet(ctx, biomeCase)))
        .toList();
  }

  private void testExactCandidateSet(
      final TestContext context, final BiomeCandidatesCase biomeCase) {
    final List<UnleashedDogBreed> candidates =
        DogSpawner.spawnableBreedsIn(
            biomeRegistry(context).entryOf(biomeCase.biome()), DogsUnleashedConfig.defaults());
    context.assertTrue(
        candidates.equals(biomeCase.expected()),
        biomeCase.biome().getValue()
            + " should resolve candidates "
            + biomeCase.expected()
            + " but resolved "
            + candidates);
    context.complete();
  }

  @GameTest(templateName = EMPTY_STRUCTURE, tickLimit = TICK_LIMIT)
  public void disablingOneBreedKeepsOthersInSharedBiome(final TestContext context) {
    final DogsUnleashedConfig dachshundDisabled =
        DogsUnleashedConfig.defaults()
            .withBreedSpawnRateMultiplierPercent(UnleashedDogBreed.DACHSHUND.serializedId(), 0);
    final List<UnleashedDogBreed> candidates =
        DogSpawner.spawnableBreedsIn(
            biomeRegistry(context).entryOf(BiomeKeys.MEADOW), dachshundDisabled);
    context.assertTrue(
        candidates.equals(List.of(UnleashedDogBreed.BEAGLE)),
        "Meadow with dachshund at 0% should still offer the beagle but resolved " + candidates);
    context.complete();
  }

  @CustomTestProvider
  public List<TestFunction> despawnFlagNbtRoundTripPerBreedAndValue() {
    return DogTestData.getAllBreeds().stream()
        .flatMap(
            data ->
                Stream.of(true, false)
                    .map(
                        flag ->
                            new TestFunction(
                                BATCH,
                                "dogspawnertest.nbtroundtrip."
                                    + data.breed().serializedId()
                                    + "."
                                    + flag,
                                ARENA,
                                TICK_LIMIT,
                                0L,
                                true,
                                ctx -> testDespawnFlagNbtRoundTrip(ctx, data, flag))))
        .toList();
  }

  private void testDespawnFlagNbtRoundTrip(
      final TestContext context,
      final DogTestData<? extends UnleashedDogEntity> data,
      final boolean flag) {
    final UnleashedDogEntity original =
        DogTestHelper.spawnDog(context, data, new BlockPos(2, 2, 2));
    original.setAiDisabled(true);
    original.setSpawnedByDogSpawner(flag);

    final NbtCompound nbt = new NbtCompound();
    original.writeCustomDataToNbt(nbt);

    // The restored dog starts with the opposite flag to prove the read overrides it.
    final UnleashedDogEntity restored =
        DogTestHelper.spawnDog(context, data, new BlockPos(4, 2, 4));
    restored.setAiDisabled(true);
    restored.setSpawnedByDogSpawner(!flag);
    restored.readCustomDataFromNbt(nbt);

    context.assertTrue(
        restored.isSpawnedByDogSpawner() == flag,
        data.breed().serializedId()
            + " SpawnedByDogSpawner="
            + flag
            + " should survive an NBT write/read round trip");
    context.assertTrue(
        restored.canImmediatelyDespawn(0.0) == flag,
        data.breed().serializedId()
            + " despawn eligibility should follow the restored flag while untamed");
    context.complete();
  }

  @CustomTestProvider
  public List<TestFunction> despawnEligibilityLifecyclePerBreed() {
    return DogTestData.getAllBreeds().stream()
        .map(
            data ->
                new TestFunction(
                    BATCH,
                    "dogspawnertest.despawnlifecycle." + data.breed().serializedId(),
                    ARENA,
                    TICK_LIMIT,
                    0L,
                    true,
                    ctx -> testDespawnEligibilityLifecycle(ctx, data)))
        .toList();
  }

  private void testDespawnEligibilityLifecycle(
      final TestContext context, final DogTestData<? extends UnleashedDogEntity> data) {
    final UnleashedDogEntity dog = DogTestHelper.spawnDog(context, data, REL_ARENA_CENTER);
    dog.setAiDisabled(true);
    final String breedId = data.breed().serializedId();

    dog.setSpawnedByDogSpawner(true);
    context.assertTrue(
        dog.canImmediatelyDespawn(0.0),
        "An untamed spawner-spawned " + breedId + " should be eligible to despawn");

    dog.setTamed(true, true);
    context.assertFalse(
        dog.isSpawnedByDogSpawner(), "Taming should clear the spawner-spawned flag on " + breedId);
    context.assertFalse(
        dog.canImmediatelyDespawn(0.0),
        "A tamed " + breedId + " should never be eligible to despawn");

    dog.setTamed(false, true);
    context.assertFalse(
        dog.isSpawnedByDogSpawner(),
        "Untaming should not restore the spawner-spawned flag on " + breedId);
    context.assertFalse(
        dog.canImmediatelyDespawn(0.0),
        "A previously tamed " + breedId + " should keep permanent persistence");
    context.complete();
  }

  @CustomTestProvider
  public List<TestFunction> chunkGenerationPersistencePerBreed() {
    return DogTestData.getAllBreeds().stream()
        .map(
            data ->
                new TestFunction(
                    BATCH,
                    "dogspawnertest.chunkgenpersistence." + data.breed().serializedId(),
                    ARENA,
                    TICK_LIMIT,
                    0L,
                    true,
                    ctx -> testChunkGenerationPersistence(ctx, data)))
        .toList();
  }

  private void testChunkGenerationPersistence(
      final TestContext context, final DogTestData<? extends UnleashedDogEntity> data) {
    final UnleashedDogEntity dog = DogTestHelper.spawnDog(context, data, REL_ARENA_CENTER);
    dog.setAiDisabled(true);
    final String breedId = data.breed().serializedId();

    context.assertFalse(
        dog.isSpawnedByDogSpawner(),
        "A " + breedId + " not spawned by the DogSpawner should carry no flag");
    context.assertFalse(
        dog.canImmediatelyDespawn(0.0),
        "An unflagged wild " + breedId + " should keep vanilla animal persistence");
    context.complete();
  }

  private static Registry<Biome> biomeRegistry(final TestContext context) {
    return context.getWorld().getRegistryManager().get(RegistryKeys.BIOME);
  }

  private static long countDogSpawners(final ServerWorld world) {
    return ((ServerWorldSpawnersAccessor) world)
        .dogsUnleashed$getSpawners().stream()
            .filter(spawner -> spawner instanceof DogSpawner)
            .count();
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
