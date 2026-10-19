package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.server.ServerConfigService;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class DireWolfPupGrowthGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String STOPPED_BATCH = "dire-wolf-pup-growth-stopped";
  private static final String DOUBLED_BATCH = "dire-wolf-pup-growth-doubled";
  private static final BlockPos REL_WOLF = new BlockPos(3, 2, 3);
  private static final int STOPPED_PERCENT = 0;
  private static final int DOUBLED_PERCENT = 200;
  private static final int TICKS_FROM_GROWN = 20;

  private static DogsUnleashedConfig configBefore;

  @BeforeBatch(batchId = STOPPED_BATCH)
  public void stopGrowth(final ServerWorld world) {
    useGrowthRate(world, STOPPED_PERCENT);
  }

  @AfterBatch(batchId = STOPPED_BATCH)
  public void restoreAfterStopped(final ServerWorld world) {
    ServerConfigService.update(world.getServer(), configBefore);
  }

  @BeforeBatch(batchId = DOUBLED_BATCH)
  public void doubleGrowth(final ServerWorld world) {
    useGrowthRate(world, DOUBLED_PERCENT);
  }

  @AfterBatch(batchId = DOUBLED_BATCH)
  public void restoreAfterDoubled(final ServerWorld world) {
    ServerConfigService.update(world.getServer(), configBefore);
  }

  @GameTest(templateName = ARENA, batchId = STOPPED_BATCH, tickLimit = 60)
  public void aPupAboutToGrowStaysAPupAtZeroGrowthSpeed(final TestContext context) {
    final UnleashedDogEntity pup = spawnPupTicksFromGrown(context, 2);

    context.runAtTick(
        40,
        () -> {
          context.assertTrue(
              pup.isBaby() && pup.getGrowth().stage() == DireWolfGrowthStage.PUP,
              "At 0% growth speed the pup should stay a pup, but it is " + pup.getGrowth().stage());
          context.assertTrue(
              pup.getBreedingAge() == -2,
              "At 0% growth speed the pup's age should not move from -2, but it is "
                  + pup.getBreedingAge());
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = DOUBLED_BATCH, tickLimit = 40)
  public void aPupGrowsTwiceAsFastAtDoubleGrowthSpeed(final TestContext context) {
    final UnleashedDogEntity pup = spawnPupTicksFromGrown(context, TICKS_FROM_GROWN);

    context.runAtTick(
        TICKS_FROM_GROWN * 3 / 4,
        () -> {
          context.assertTrue(
              !pup.isBaby() && pup.getGrowth().stage() == DireWolfGrowthStage.YEARLING,
              "At 200% growth speed the pup should have grown into a yearling in half the time,"
                  + " but it is "
                  + pup.getGrowth().stage());
          context.complete();
        });
  }

  private static void useGrowthRate(final ServerWorld world, final int percent) {
    configBefore = DogsUnleashed.SERVER_CONFIG;
    ServerConfigService.update(
        world.getServer(),
        configBefore.withDireWolf(
            configBefore.direWolf().withGrowthRateMultiplierPercent(percent)));
  }

  private static UnleashedDogEntity spawnPupTicksFromGrown(
      final TestContext context, final int ticksFromGrown) {
    final UnleashedDogEntity pup = context.spawnEntity(ModEntities.DIRE_WOLF, REL_WOLF);
    pup.setAiDisabled(true);
    pup.getGrowth().setStage(DireWolfGrowthStage.PUP);
    pup.setBreedingAge(-ticksFromGrown);
    return pup;
  }
}
