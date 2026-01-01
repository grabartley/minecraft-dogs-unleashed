package com.grahambartley.gametest;

import com.grahambartley.ModEntities;
import com.grahambartley.entity.BeagleEntity;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class BeagleEntityGameTest implements FabricGameTest {

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void beagleHasCorrectAttributes(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final BeagleEntity beagle = new BeagleEntity(ModEntities.BEAGLE, world);
    beagle.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(beagle);

    context.assertTrue(
        beagle.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) == 17.0,
        "Beagle max health should be 17.0");
    context.assertTrue(
        beagle.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) == 0.29,
        "Beagle movement speed should be 0.29");
    context.assertTrue(
        beagle.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) == 3.0,
        "Beagle attack damage should be 3.0");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void beagleCreatesBeagleBaby(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final BeagleEntity parent1 = new BeagleEntity(ModEntities.BEAGLE, world);
    parent1.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    parent1.setTamed(true, true);
    world.spawnEntity(parent1);

    final BeagleEntity parent2 = new BeagleEntity(ModEntities.BEAGLE, world);
    parent2.refreshPositionAndAngles(new BlockPos(1, 1, 0), 0.0f, 0.0f);
    parent2.setTamed(true, true);
    world.spawnEntity(parent2);

    context.runAtTick(
        10,
        () -> {
          final BeagleEntity baby = (BeagleEntity) parent1.createChild(world, parent2);

          context.assertTrue(baby != null, "Baby should be created from two beagles");
          context.assertTrue(baby instanceof BeagleEntity, "Baby should be a BeagleEntity");
          context.assertTrue(baby.isBaby(), "Created entity should be a baby");
          context.complete();
        });
  }
}
