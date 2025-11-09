package com.grahambartley.gametest;

import com.grahambartley.ModEntities;
import com.grahambartley.entity.HuskyEntity;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class HuskyEntityGameTest implements FabricGameTest {

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyHasCorrectAttributes(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(husky);

    context.assertTrue(
        husky.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) == 25.0,
        "Husky max health should be 25.0");
    context.assertTrue(
        husky.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) == 0.30,
        "Husky movement speed should be 0.30");
    context.assertTrue(
        husky.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) == 5.0,
        "Husky attack damage should be 5.0");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void huskyCreatesHuskyBaby(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity parent1 = new HuskyEntity(ModEntities.HUSKY, world);
    parent1.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    parent1.setTamed(true, true);
    world.spawnEntity(parent1);

    final HuskyEntity parent2 = new HuskyEntity(ModEntities.HUSKY, world);
    parent2.refreshPositionAndAngles(new BlockPos(1, 1, 0), 0.0f, 0.0f);
    parent2.setTamed(true, true);
    world.spawnEntity(parent2);

    context.runAtTick(
        10,
        () -> {
          final HuskyEntity baby = (HuskyEntity) parent1.createChild(world, parent2);

          context.assertTrue(baby != null, "Baby should be created from two huskies");
          context.assertTrue(baby instanceof HuskyEntity, "Baby should be a HuskyEntity");
          context.assertTrue(baby.isBaby(), "Created entity should be a baby");
          context.complete();
        });
  }
}
