package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.block.entity.FrisbeeBlockEntity;
import com.grahambartley.dogsunleashed.gametest.util.CreativeTestPlayer;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

public final class FrisbeeBlockGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String BATCH = "frisbee-block";
  private static final int TICK_LIMIT = 40;
  private static final int SETTLE_TICK = 30;
  private static final BlockPos UNSUPPORTED_POS = new BlockPos(3, 3, 3);
  private static final BlockPos FRISBEE_POS = new BlockPos(3, 2, 3);
  private static final DyeColor COLOR = DyeColor.RED;

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anUnsupportedFrisbeeStaysWhereItIsPut(final TestContext context) {
    final BlockPos below = UNSUPPORTED_POS.down();
    context.assertTrue(
        context.getBlockState(below).isAir(),
        "the arena must leave air under " + UNSUPPORTED_POS + ", or this test proves nothing");
    context.setBlockState(UNSUPPORTED_POS, ModBlocks.FRISBEE.getDefaultState());

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
  public void anUnsupportedSandBlockFallsInTheSameSpot(final TestContext context) {
    context.assertTrue(
        context.getBlockState(UNSUPPORTED_POS.down()).isAir(),
        "the arena must leave air under " + UNSUPPORTED_POS + ", or this control proves nothing");
    context.setBlockState(UNSUPPORTED_POS, Blocks.SAND);

    context.runAtTick(
        SETTLE_TICK,
        () -> {
          context.dontExpectBlock(Blocks.SAND, UNSUPPORTED_POS);
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aFrisbeeBrokenByAPlayerDropsItselfInItsColour(final TestContext context) {
    placeColouredFrisbee(context);
    final ServerPlayerEntity player = CreativeTestPlayer.join(context);
    player.changeGameMode(GameMode.SURVIVAL);
    player.interactionManager.tryBreakBlock(context.getAbsolutePos(FRISBEE_POS));

    context.assertEquals(
        player.getStatHandler().getStat(Stats.MINED, ModBlocks.FRISBEE), 1, "frisbees mined");
    expectOneColouredFrisbeeDropped(context);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aFrisbeeBrokenInCreativeDropsNothing(final TestContext context) {
    placeColouredFrisbee(context);
    final ServerPlayerEntity player = CreativeTestPlayer.join(context);
    player.interactionManager.tryBreakBlock(context.getAbsolutePos(FRISBEE_POS));

    context.runAtTick(
        SETTLE_TICK,
        () -> {
          context.dontExpectBlock(ModBlocks.FRISBEE, FRISBEE_POS);
          context.assertTrue(droppedFrisbees(context).isEmpty(), "creative breaks drop nothing");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aFrisbeeCaughtInAnExplosionDropsItselfInItsColour(final TestContext context) {
    placeColouredFrisbee(context);
    final Vec3d centre = Vec3d.ofCenter(context.getAbsolutePos(FRISBEE_POS));
    context
        .getWorld()
        .createExplosion(null, centre.x, centre.y, centre.z, 1.0f, World.ExplosionSourceType.TNT);

    expectOneColouredFrisbeeDropped(context);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aFrisbeeHitByAPistonDropsItselfInItsColour(final TestContext context) {
    placeColouredFrisbee(context);
    final BlockPos piston = FRISBEE_POS.west();
    context.setBlockState(
        piston, Blocks.PISTON.getDefaultState().with(PistonBlock.FACING, Direction.EAST));
    context.setBlockState(piston.west(), Blocks.REDSTONE_BLOCK);

    expectOneColouredFrisbeeDropped(context);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aFrisbeeWashedAwayByWaterDropsItselfInItsColour(final TestContext context) {
    placeColouredFrisbee(context);
    context.setBlockState(FRISBEE_POS.west(), Blocks.WATER);

    expectOneColouredFrisbeeDropped(context);
  }

  private static void placeColouredFrisbee(final TestContext context) {
    context.setBlockState(FRISBEE_POS, ModBlocks.FRISBEE.getDefaultState());
    final FrisbeeBlockEntity frisbee = context.getBlockEntity(FRISBEE_POS);
    frisbee.setColor(COLOR);
  }

  private static void expectOneColouredFrisbeeDropped(final TestContext context) {
    context.runAtTick(
        SETTLE_TICK,
        () -> {
          context.dontExpectBlock(ModBlocks.FRISBEE, FRISBEE_POS);
          final List<ItemStack> drops = droppedFrisbees(context);
          context.assertEquals(
              drops.stream().mapToInt(ItemStack::getCount).sum(), 1, "frisbees dropped");
          context.assertEquals(
              drops.getFirst().getOrDefault(ModComponents.FRISBEE_COLOR, DyeColor.WHITE),
              COLOR,
              "dropped frisbee colour");
          context.complete();
        });
  }

  private static List<ItemStack> droppedFrisbees(final TestContext context) {
    return context
        .getWorld()
        .getEntitiesByType(
            EntityType.ITEM,
            new Box(context.getAbsolutePos(FRISBEE_POS)).expand(4.0),
            item -> item.getStack().isOf(ModItems.FRISBEE))
        .stream()
        .map(ItemEntity::getStack)
        .toList();
  }
}
