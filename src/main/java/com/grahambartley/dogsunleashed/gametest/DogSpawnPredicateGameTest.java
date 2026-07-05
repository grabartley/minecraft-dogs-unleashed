package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlockTags;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.registry.Registries;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.math.BlockPos;

/**
 * Verifies the {@code dogs-unleashed:dogs_spawnable_on} spawn restriction wiring: the tag resolves
 * at runtime with the wolf-tag surfaces inherited via its {@code #minecraft:wolves_spawnable_on}
 * reference, and the registered predicate accepts spawn positions above snowy and grassy surfaces
 * while still rejecting bare stone. Predicate tests go through {@link SpawnRestriction#canSpawn} so
 * they exercise the actual registration from {@code ModSpawns.initialize()}, fanned out across
 * every breed via {@link CustomTestProvider}.
 */
public final class DogSpawnPredicateGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final int TICK_LIMIT = 20;
  private static final BlockPos REL_SURFACE_POS = new BlockPos(2, 1, 2);

  private record SurfaceCase(Block surface, boolean spawnable) {}

  private static final List<SurfaceCase> TAG_CASES =
      List.of(
          new SurfaceCase(Blocks.GRASS_BLOCK, true),
          new SurfaceCase(Blocks.SNOW, true),
          new SurfaceCase(Blocks.SNOW_BLOCK, true),
          new SurfaceCase(Blocks.COARSE_DIRT, true),
          new SurfaceCase(Blocks.PODZOL, true),
          new SurfaceCase(Blocks.STONE, false));

  @CustomTestProvider
  public List<TestFunction> dogsSpawnableOnTagMembershipPerSurface() {
    return TAG_CASES.stream()
        .map(
            surfaceCase ->
                new TestFunction(
                    "defaultBatch",
                    "dogspawnpredicatetest.tagmembership." + surfaceId(surfaceCase),
                    FabricGameTest.EMPTY_STRUCTURE,
                    TICK_LIMIT,
                    0L,
                    true,
                    ctx -> testTagMembership(ctx, surfaceCase)))
        .toList();
  }

  @CustomTestProvider
  public List<TestFunction> spawnPredicateAcceptsGrassBlockPerBreed() {
    return generatePerBreed("acceptsgrassblock", new SurfaceCase(Blocks.GRASS_BLOCK, true));
  }

  @CustomTestProvider
  public List<TestFunction> spawnPredicateAcceptsSnowBlockPerBreed() {
    return generatePerBreed("acceptssnowblock", new SurfaceCase(Blocks.SNOW_BLOCK, true));
  }

  @CustomTestProvider
  public List<TestFunction> spawnPredicateRejectsStonePerBreed() {
    return generatePerBreed("rejectsstone", new SurfaceCase(Blocks.STONE, false));
  }

  private List<TestFunction> generatePerBreed(
      final String behavior, final SurfaceCase surfaceCase) {
    return DogTestData.getAllBreeds().stream()
        .map(
            data ->
                new TestFunction(
                    "defaultBatch",
                    "dogspawnpredicatetest." + behavior + "." + data.breed().serializedId(),
                    ARENA,
                    TICK_LIMIT,
                    0L,
                    true,
                    ctx -> testSpawnPredicate(ctx, data, surfaceCase)))
        .toList();
  }

  private void testTagMembership(final TestContext context, final SurfaceCase surfaceCase) {
    final boolean inTag =
        surfaceCase.surface().getDefaultState().isIn(ModBlockTags.DOGS_SPAWNABLE_ON);

    context.assertTrue(
        inTag == surfaceCase.spawnable(),
        "dogs_spawnable_on membership for "
            + surfaceId(surfaceCase)
            + " should be "
            + surfaceCase.spawnable()
            + " but was "
            + inTag);
    context.complete();
  }

  private void testSpawnPredicate(
      final TestContext context,
      final DogTestData<? extends UnleashedDogEntity> data,
      final SurfaceCase surfaceCase) {
    context.setBlockState(REL_SURFACE_POS, surfaceCase.surface().getDefaultState());

    final BlockPos absSpawnPos = context.getAbsolutePos(REL_SURFACE_POS.up());
    final boolean allowed =
        SpawnRestriction.canSpawn(
            data.entityType(),
            context.getWorld(),
            SpawnReason.NATURAL,
            absSpawnPos,
            context.getWorld().getRandom());

    context.assertTrue(
        allowed == surfaceCase.spawnable(),
        "Spawn above "
            + surfaceId(surfaceCase)
            + " should be "
            + (surfaceCase.spawnable() ? "allowed" : "rejected")
            + " for "
            + data.breed().serializedId()
            + " but was not (base light level "
            + context.getWorld().getBaseLightLevel(absSpawnPos, 0)
            + ")");
    context.complete();
  }

  private static String surfaceId(final SurfaceCase surfaceCase) {
    return Registries.BLOCK.getId(surfaceCase.surface()).getPath();
  }
}
