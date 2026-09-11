package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlocks;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class FrisbeeBlockGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String BATCH = "frisbee-block";
  private static final int TICK_LIMIT = 40;
  private static final int SETTLE_TICK = 30;
  private static final BlockPos UNSUPPORTED_POS = new BlockPos(3, 3, 3);

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anUnsupportedFrisbeeStaysWhereItIsPut(TestContext context) {
    final BlockPos below = UNSUPPORTED_POS.down();
    context.setBlockState(below, Blocks.AIR);
    context.setBlockState(UNSUPPORTED_POS, ModBlocks.FRISBEE.getDefaultState());
    context.assertTrue(
        context.getBlockState(below).isAir(),
        "nothing may hold the frisbee up, or this test proves nothing");

    context.runAtTick(
        SETTLE_TICK,
        () -> {
          context.expectBlock(ModBlocks.FRISBEE, UNSUPPORTED_POS);
          context.assertTrue(
              context.getBlockState(below).isAir(),
              "the frisbee must not have dropped into the space below it");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anUnsupportedSandBlockFallsInTheSameSpot(TestContext context) {
    final BlockPos below = UNSUPPORTED_POS.down();
    context.setBlockState(below, Blocks.AIR);
    context.setBlockState(UNSUPPORTED_POS, Blocks.SAND);

    context.runAtTick(
        SETTLE_TICK,
        () -> {
          context.dontExpectBlock(Blocks.SAND, UNSUPPORTED_POS);
          context.complete();
        });
  }
}
