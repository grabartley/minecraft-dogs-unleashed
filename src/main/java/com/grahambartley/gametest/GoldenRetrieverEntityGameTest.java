package com.grahambartley.gametest;

import com.grahambartley.ModEntities;
import com.grahambartley.entity.GoldenRetrieverEntity;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class GoldenRetrieverEntityGameTest implements FabricGameTest {

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void goldenRetrieverHasCorrectAttributes(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final GoldenRetrieverEntity goldenRetriever =
        new GoldenRetrieverEntity(ModEntities.GOLDEN_RETRIEVER, world);
    goldenRetriever.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(goldenRetriever);

    context.assertTrue(
        goldenRetriever.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) == 24.0,
        "Golden Retriever max health should be 24.0");
    context.assertTrue(
        goldenRetriever.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) == 0.30,
        "Golden Retriever movement speed should be 0.30");
    context.assertTrue(
        goldenRetriever.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) == 4.0,
        "Golden Retriever attack damage should be 4.0");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void goldenRetrieverCreatesGoldenRetrieverBaby(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final GoldenRetrieverEntity parent1 =
        new GoldenRetrieverEntity(ModEntities.GOLDEN_RETRIEVER, world);
    parent1.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    parent1.setTamed(true, true);
    world.spawnEntity(parent1);

    final GoldenRetrieverEntity parent2 =
        new GoldenRetrieverEntity(ModEntities.GOLDEN_RETRIEVER, world);
    parent2.refreshPositionAndAngles(new BlockPos(1, 1, 0), 0.0f, 0.0f);
    parent2.setTamed(true, true);
    world.spawnEntity(parent2);

    context.runAtTick(
        10,
        () -> {
          final GoldenRetrieverEntity baby =
              (GoldenRetrieverEntity) parent1.createChild(world, parent2);

          context.assertTrue(baby != null, "Baby should be created from two golden retrievers");
          context.assertTrue(
              baby instanceof GoldenRetrieverEntity, "Baby should be a GoldenRetrieverEntity");
          context.assertTrue(baby.isBaby(), "Created entity should be a baby");
          context.complete();
        });
  }
}
