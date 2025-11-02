package com.grahambartley.gametest;

import com.grahambartley.ModEntities;
import com.grahambartley.entity.HuskyEntity;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class HuskyEntityGameTest implements FabricGameTest {

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void huskySpawnsCorrectly(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);

    husky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(husky);

    context.runAtTick(
        1,
        () -> {
          context.assertTrue(!husky.isRemoved(), "Husky should be alive and present in the world");
          context.assertTrue(
              world.getEntitiesByType(ModEntities.HUSKY, entity -> true).contains(husky),
              "Husky should be in the world's entity list");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyIsTameable(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);

    husky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(husky);

    context.assertTrue(husky instanceof TameableEntity, "Husky must be a TameableEntity");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void huskyCanBeTamed(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);

    husky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          context.assertFalse(husky.isTamed(), "Husky should not be tamed initially");

          husky.setTamed(true, true);

          context.runAtTick(
              20,
              () -> {
                context.assertTrue(husky.isTamed(), "Husky should be tamed after setTamed");
                context.complete();
              });
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void huskyHasCorrectDimensions(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);

    husky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(husky);

    final float expectedWidth = 0.8f;
    final float expectedHeight = 1.1f;

    context.assertTrue(
        Math.abs(husky.getWidth() - expectedWidth) < 0.01f,
        "Husky width should be " + expectedWidth);
    context.assertTrue(
        Math.abs(husky.getHeight() - expectedHeight) < 0.01f,
        "Husky height should be " + expectedHeight);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyHasAnimatableInstanceCache(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);

    husky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(husky);

    context.assertTrue(
        husky.getAnimatableInstanceCache() != null,
        "Husky should have AnimatableInstanceCache for GeckoLib");
    context.complete();
  }
}
