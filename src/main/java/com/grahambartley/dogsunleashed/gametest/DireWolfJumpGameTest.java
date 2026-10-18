package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class DireWolfJumpGameTest implements FabricGameTest {

  private static final String RANGE = "dogs-unleashed:dog_range";
  private static final int FLOOR_Y = 2;
  private static final int STEP_START_X = 8;
  private static final int RANGE_LENGTH = 16;
  private static final int RANGE_WIDTH = 9;
  private static final BlockPos REL_START = new BlockPos(3, FLOOR_Y, 4);
  private static final BlockPos REL_GOAL = new BlockPos(12, FLOOR_Y, 4);
  private static final double ARRIVED = 0.01;
  private static final double TELEPORT_STRIDE = 2.0;

  @GameTest(templateName = RANGE, tickLimit = 200)
  public void aPrimeClimbsAStepOneAndAHalfBlocksHigh(final TestContext context) {
    buildStep(context, 1.5);
    climbs(context, DireWolfGrowthStage.PRIME, 1.5);
  }

  @GameTest(templateName = RANGE, tickLimit = 200)
  public void anAlphaClimbsAStepTwoBlocksHigh(final TestContext context) {
    buildStep(context, 2.0);
    climbs(context, DireWolfGrowthStage.ALPHA, 2.0);
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void anAdultFindsNoWayUpAStepOneAndAHalfBlocksHigh(final TestContext context) {
    buildStep(context, 1.5);
    findsNoWayUp(context, DireWolfGrowthStage.ADULT, 1.5);
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void aPrimeFindsNoWayUpAStepTwoBlocksHigh(final TestContext context) {
    buildStep(context, 2.0);
    findsNoWayUp(context, DireWolfGrowthStage.PRIME, 2.0);
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void eachStageJumpsWithItsOwnStrength(final TestContext context) {
    final UnleashedDogEntity wolf = context.spawnEntity(ModEntities.DIRE_WOLF, REL_START);
    wolf.setAiDisabled(true);
    for (final DireWolfGrowthStage stage : DireWolfGrowthStage.values()) {
      wolf.getGrowth().setStage(stage);
      final double strength = wolf.getAttributeValue(EntityAttributes.GENERIC_JUMP_STRENGTH);
      context.assertTrue(
          Math.abs(strength - stage.jumpStrength()) < 0.0001,
          stage + " should jump with strength " + stage.jumpStrength() + " but has " + strength);
    }
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 300)
  public void aPrimeJumpsAFenceToReachItsOwner(final TestContext context) {
    for (int z = 0; z < RANGE_WIDTH; z++) {
      context.setBlockState(new BlockPos(STEP_START_X, FLOOR_Y, z), Blocks.OAK_FENCE);
    }
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final Vec3d ownerPos = context.getAbsolute(Vec3d.ofBottomCenter(REL_GOAL));
    owner.refreshPositionAndAngles(ownerPos.x, ownerPos.y, ownerPos.z, 0.0f, 0.0f);
    final UnleashedDogEntity wolf = context.spawnEntity(ModEntities.DIRE_WOLF, REL_START);
    wolf.setOwner(owner);
    wolf.getGrowth().setStage(DireWolfGrowthStage.PRIME);
    wolf.getCommandController().apply(DogCommand.FOLLOW);
    final double fenceX = context.getAbsolutePos(new BlockPos(STEP_START_X, FLOOR_Y, 0)).getX();
    final Vec3d[] last = {wolf.getPos()};
    context.runAtEveryTick(
        () -> {
          final double stride = wolf.getPos().subtract(last[0]).horizontalLength();
          context.assertTrue(
              stride < TELEPORT_STRIDE,
              "The prime moved " + stride + " blocks in one tick, so it was teleported");
          last[0] = wolf.getPos();
        });
    context.addInstantFinalTask(
        () ->
            context.assertTrue(
                wolf.getX() - wolf.getWidth() / 2.0 > fenceX + 1.0,
                "The prime is still on the near side of the fence at x " + wolf.getX()));
  }

  private static void climbs(
      final TestContext context, final DireWolfGrowthStage stage, final double height) {
    final UnleashedDogEntity wolf = context.spawnEntity(ModEntities.DIRE_WOLF, REL_START);
    wolf.getGrowth().setStage(stage);
    final Vec3d goal = context.getAbsolute(Vec3d.ofBottomCenter(REL_GOAL)).add(0.0, height, 0.0);
    final double floor = context.getAbsolute(Vec3d.ofBottomCenter(REL_START)).y;
    context.runAtEveryTick(
        () -> {
          if (wolf.getNavigation().isIdle()) {
            wolf.getNavigation().startMovingTo(goal.x, goal.y, goal.z, 1.0);
          }
        });
    context.addInstantFinalTask(
        () ->
            context.assertTrue(
                wolf.isOnGround() && wolf.getY() >= floor + height - ARRIVED,
                stage
                    + " has not climbed the "
                    + height
                    + " block step, its feet are at "
                    + (wolf.getY() - floor)));
  }

  private static void findsNoWayUp(
      final TestContext context, final DireWolfGrowthStage stage, final double height) {
    final UnleashedDogEntity wolf = context.spawnEntity(ModEntities.DIRE_WOLF, REL_START);
    wolf.setAiDisabled(true);
    wolf.getGrowth().setStage(stage);
    final BlockPos goal = context.getAbsolutePos(REL_GOAL.up((int) Math.ceil(height)));
    final Path path = wolf.getNavigation().findPathTo(goal, 0);
    context.assertTrue(
        path == null || !path.reachesTarget(),
        stage + " should find no way up a " + height + " block step, but planned " + path);
    context.complete();
  }

  private static void buildStep(final TestContext context, final double height) {
    final int fullBlocks = (int) Math.floor(height);
    final boolean halfBlock = height - fullBlocks > 0.0;
    for (int x = STEP_START_X; x < RANGE_LENGTH; x++) {
      for (int z = 0; z < RANGE_WIDTH; z++) {
        for (int y = 0; y < fullBlocks; y++) {
          context.setBlockState(new BlockPos(x, FLOOR_Y + y, z), Blocks.STONE);
        }
        if (halfBlock) {
          context.setBlockState(
              new BlockPos(x, FLOOR_Y + fullBlocks, z),
              Blocks.STONE_SLAB.getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM));
        }
      }
    }
  }
}
