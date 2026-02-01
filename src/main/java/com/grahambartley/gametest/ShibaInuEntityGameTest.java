package com.grahambartley.gametest;

import com.grahambartley.ModEntities;
import com.grahambartley.entity.ShibaInuEntity;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class ShibaInuEntityGameTest implements FabricGameTest {

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void shibaInuHasCorrectAttributes(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final ShibaInuEntity shibaInu = new ShibaInuEntity(ModEntities.SHIBA_INU, world);
    shibaInu.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(shibaInu);

    context.assertTrue(
        shibaInu.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) == 18.0,
        "Shiba Inu max health should be 18.0");
    context.assertTrue(
        shibaInu.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) == 0.32,
        "Shiba Inu movement speed should be 0.32");
    context.assertTrue(
        shibaInu.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) == 3.5,
        "Shiba Inu attack damage should be 3.5");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void shibaInuCreatesShibaInuBaby(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final ShibaInuEntity parent1 = new ShibaInuEntity(ModEntities.SHIBA_INU, world);
    parent1.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    parent1.setTamed(true, true);
    world.spawnEntity(parent1);

    final ShibaInuEntity parent2 = new ShibaInuEntity(ModEntities.SHIBA_INU, world);
    parent2.refreshPositionAndAngles(new BlockPos(1, 1, 0), 0.0f, 0.0f);
    parent2.setTamed(true, true);
    world.spawnEntity(parent2);

    context.runAtTick(
        10,
        () -> {
          final ShibaInuEntity baby = (ShibaInuEntity) parent1.createChild(world, parent2);

          context.assertTrue(baby != null, "Baby should be created from two shiba inus");
          context.assertTrue(baby instanceof ShibaInuEntity, "Baby should be a ShibaInuEntity");
          context.assertTrue(baby.isBaby(), "Created entity should be a baby");
          context.complete();
        });
  }
}
