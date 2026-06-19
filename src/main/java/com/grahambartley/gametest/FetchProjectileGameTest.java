package com.grahambartley.gametest;

import com.grahambartley.ModBlocks;
import com.grahambartley.ModEntities;
import com.grahambartley.block.entity.FrisbeeBlockEntity;
import com.grahambartley.entity.FrisbeeProjectileEntity;
import java.util.Collection;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

/**
 * Real-behavior coverage of the shared {@link
 * com.grahambartley.entity.fetch.AbstractFetchProjectileEntity} lifecycle: each thrown fetch
 * projectile, when it lands in open air against a surface, replaces that air with its own {@code
 * FetchItemType.landedBlock()} so a dog has something to retrieve. The frisbee additionally
 * imprints its thrown color onto the placed block via the {@code enrichLandedBlockEntity} hook.
 *
 * <p>These need a live {@code ServerWorld} (entity construction, registry-backed item/block
 * lookups, and projectile physics all touch class init that the JUnit classloader cannot complete),
 * so they live here rather than under {@code src/test/java}.
 */
public final class FetchProjectileGameTest implements FabricGameTest {

  private static final BlockPos SURFACE = new BlockPos(1, 0, 1);
  private static final BlockPos SPAWN = new BlockPos(1, 5, 1);
  private static final BlockPos LANDED = new BlockPos(1, 1, 1);

  /** One land-as-block case per fetch projectile, sharing the same throw-and-settle body. */
  @CustomTestProvider
  public Collection<TestFunction> landsAsItsOwnBlock() {
    record FetchCase(String id, EntityType<? extends ThrownEntity> projectile, Block landedBlock) {}
    final List<FetchCase> cases =
        List.of(
            new FetchCase("tennis_ball", ModEntities.TENNIS_BALL_PROJECTILE, ModBlocks.TENNIS_BALL),
            new FetchCase("stick", ModEntities.STICK_PROJECTILE, ModBlocks.STICK),
            new FetchCase("frisbee", ModEntities.FRISBEE_PROJECTILE, ModBlocks.FRISBEE));

    return cases.stream()
        .map(
            fetchCase ->
                new TestFunction(
                    "defaultBatch",
                    "fetchprojectilegametest.landsasitsownblock." + fetchCase.id(),
                    FabricGameTest.EMPTY_STRUCTURE,
                    /* tickLimit */ 80,
                    /* setupTicks */ 0L,
                    /* required */ true,
                    context ->
                        dropProjectileAndExpectBlock(
                            context, fetchCase.projectile(), fetchCase.landedBlock())))
        .toList();
  }

  private void dropProjectileAndExpectBlock(
      final TestContext context,
      final EntityType<? extends ThrownEntity> projectile,
      final Block landedBlock) {
    context.setBlockState(SURFACE, Blocks.STONE);

    final Entity thrown = context.spawnEntity(projectile, SPAWN);
    thrown.setVelocity(0, -0.5, 0);

    context.expectBlockAtEnd(landedBlock, LANDED);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 80)
  public void frisbeeLandedBlockKeepsThrownColor(final TestContext context) {
    context.setBlockState(SURFACE, Blocks.STONE);

    final FrisbeeProjectileEntity frisbee =
        context.spawnEntity(ModEntities.FRISBEE_PROJECTILE, SPAWN);
    frisbee.setFrisbeeColor(DyeColor.LIME);
    frisbee.setVelocity(0, -0.5, 0);

    context.addInstantFinalTask(
        () ->
            context.checkBlockEntity(
                LANDED,
                blockEntity ->
                    blockEntity instanceof FrisbeeBlockEntity frisbeeBlock
                        && frisbeeBlock.getColor() == DyeColor.LIME,
                () -> "Landed frisbee block should carry the thrown frisbee's LIME color"));
  }
}
