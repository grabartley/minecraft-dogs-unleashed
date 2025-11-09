package com.grahambartley.gametest;

import com.grahambartley.ModEntities;
import com.grahambartley.entity.DachshundEntity;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class DachshundEntityGameTest implements FabricGameTest {

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dachshundHasCorrectAttributes(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final DachshundEntity dachshund = new DachshundEntity(ModEntities.DACHSHUND, world);
    dachshund.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dachshund);

    context.assertTrue(
        dachshund.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) == 10.0,
        "Dachshund max health should be 10.0");
    context.assertTrue(
        dachshund.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) == 0.25,
        "Dachshund movement speed should be 0.25");
    context.assertTrue(
        dachshund.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) == 2.0,
        "Dachshund attack damage should be 2.0");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dachshundCreatesDachshundBaby(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final DachshundEntity parent1 = new DachshundEntity(ModEntities.DACHSHUND, world);
    parent1.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    parent1.setTamed(true, true);
    world.spawnEntity(parent1);

    final DachshundEntity parent2 = new DachshundEntity(ModEntities.DACHSHUND, world);
    parent2.refreshPositionAndAngles(new BlockPos(1, 1, 0), 0.0f, 0.0f);
    parent2.setTamed(true, true);
    world.spawnEntity(parent2);

    context.runAtTick(
        10,
        () -> {
          final DachshundEntity baby = (DachshundEntity) parent1.createChild(world, parent2);

          context.assertTrue(baby != null, "Baby should be created from two dachshunds");
          context.assertTrue(baby instanceof DachshundEntity, "Baby should be a DachshundEntity");
          context.assertTrue(baby.isBaby(), "Created entity should be a baby");
          context.complete();
        });
  }
}
