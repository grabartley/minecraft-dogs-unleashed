package com.grahambartley.gametest;

import com.grahambartley.ModBlocks;
import com.grahambartley.ModComponents;
import com.grahambartley.ModEntities;
import com.grahambartley.block.entity.FrisbeeBlockEntity;
import com.grahambartley.entity.FrisbeeProjectileEntity;
import com.grahambartley.entity.fetch.FetchTypes;
import java.util.Collection;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.item.Item;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

/**
 * Real-behavior coverage of the shared {@link
 * com.grahambartley.entity.fetch.AbstractFetchProjectileEntity} {@code onBlockHit} lifecycle, run
 * through every concrete projectile. Two branches are exercised:
 *
 * <ul>
 *   <li>Lands in open air against a surface: the air is replaced with the projectile's own {@code
 *       FetchItemType.landedBlock()} so a dog has something to retrieve.
 *   <li>Lands where the spot is obstructed (the point-blank-throw case, reproduced here by spawning
 *       the projectile inside a block): no block is placed and the fetch item drops as a
 *       retrievable {@link ItemEntity} via the {@code buildDropStack} hook.
 * </ul>
 *
 * <p>The frisbee additionally imprints its thrown color, onto the placed block via {@code
 * enrichLandedBlockEntity} and onto the dropped stack's {@code FRISBEE_COLOR} component via {@code
 * buildDropStack}.
 *
 * <p>These need a live {@code ServerWorld} (entity construction, registry-backed item/block
 * lookups, and projectile physics all touch class init that the JUnit classloader cannot complete),
 * so they live here rather than under {@code src/test/java}.
 */
public final class FetchProjectileGameTest implements FabricGameTest {

  private static final BlockPos SURFACE = new BlockPos(1, 0, 1);
  // Land case: drop from 4 blocks up at -0.5/tick (plus drag) settles onto SURFACE well within the
  // 80-tick limit, so the placed block lands at LANDED. Tweaking a subclass getGravity() that slows
  // the fall this much would need this geometry revisited.
  private static final BlockPos LAND_SPAWN = new BlockPos(1, 5, 1);
  private static final BlockPos LANDED = new BlockPos(1, 1, 1);
  // Drop case: the projectile spawns inside this (stone-filled) cell so its landing position is
  // obstructed, forcing the drop-as-item branch instead of placing a block inside solid geometry.
  private static final BlockPos BLOCKED_SPAWN = new BlockPos(1, 1, 1);

  private record FetchCase(
      String id, EntityType<? extends ThrownEntity> projectile, Block landedBlock) {}

  private static final List<FetchCase> FETCH_CASES =
      List.of(
          new FetchCase("tennis_ball", ModEntities.TENNIS_BALL_PROJECTILE, ModBlocks.TENNIS_BALL),
          new FetchCase("stick", ModEntities.STICK_PROJECTILE, ModBlocks.STICK),
          new FetchCase("frisbee", ModEntities.FRISBEE_PROJECTILE, ModBlocks.FRISBEE));

  /** One land-as-block case per fetch projectile, sharing the same throw-and-settle body. */
  @CustomTestProvider
  public Collection<TestFunction> landsAsItsOwnBlock() {
    return generatePerProjectile("landsasitsownblock", 80, this::landsAsItsOwnBlockBody);
  }

  /** One drop-as-item case per fetch projectile, sharing the same blocked-landing body. */
  @CustomTestProvider
  public Collection<TestFunction> dropsItemWhenBlocked() {
    return generatePerProjectile("dropsitemwhenblocked", 40, this::dropsItemWhenBlockedBody);
  }

  private void landsAsItsOwnBlockBody(final TestContext context, final FetchCase fetchCase) {
    context.setBlockState(SURFACE, Blocks.STONE);

    final Entity thrown = context.spawnEntity(fetchCase.projectile(), LAND_SPAWN);
    thrown.setVelocity(0, -0.5, 0);

    context.expectBlockAtEnd(fetchCase.landedBlock(), LANDED);
  }

  private void dropsItemWhenBlockedBody(final TestContext context, final FetchCase fetchCase) {
    context.setBlockState(SURFACE, Blocks.STONE);
    context.setBlockState(BLOCKED_SPAWN, Blocks.STONE);

    final Entity thrown = context.spawnEntity(fetchCase.projectile(), BLOCKED_SPAWN);
    thrown.setVelocity(0, -0.3, 0);

    final Item expectedItem = FetchTypes.forEntityType(fetchCase.projectile()).item();
    context.runAtTick(
        20,
        () -> {
          context.dontExpectBlock(fetchCase.landedBlock(), LANDED);
          context.assertTrue(
              droppedItemCount(context, expectedItem) >= 1,
              "Blocked "
                  + fetchCase.id()
                  + " throw should drop a retrievable item, not place a block");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 80)
  public void frisbeeLandedBlockKeepsThrownColor(final TestContext context) {
    context.setBlockState(SURFACE, Blocks.STONE);

    final FrisbeeProjectileEntity frisbee =
        context.spawnEntity(ModEntities.FRISBEE_PROJECTILE, LAND_SPAWN);
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

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 40)
  public void frisbeeDroppedItemKeepsThrownColor(final TestContext context) {
    context.setBlockState(SURFACE, Blocks.STONE);
    context.setBlockState(BLOCKED_SPAWN, Blocks.STONE);

    final FrisbeeProjectileEntity frisbee =
        context.spawnEntity(ModEntities.FRISBEE_PROJECTILE, BLOCKED_SPAWN);
    frisbee.setFrisbeeColor(DyeColor.LIME);
    frisbee.setVelocity(0, -0.3, 0);

    context.runAtTick(
        20,
        () -> {
          final ItemEntity dropped = firstDroppedItem(context, ModEntities.FRISBEE_PROJECTILE);
          context.assertTrue(dropped != null, "Blocked frisbee throw should drop a frisbee item");
          context.assertTrue(
              dropped.getStack().get(ModComponents.FRISBEE_COLOR) == DyeColor.LIME,
              "Dropped frisbee item should carry the thrown frisbee's LIME color component");
          context.complete();
        });
  }

  private static int droppedItemCount(final TestContext context, final Item expectedItem) {
    return context
        .getWorld()
        .getEntitiesByClass(
            ItemEntity.class,
            context.getTestBox().expand(6),
            item -> item.getStack().isOf(expectedItem))
        .size();
  }

  private static ItemEntity firstDroppedItem(
      final TestContext context, final EntityType<? extends ThrownEntity> projectile) {
    final Item expectedItem = FetchTypes.forEntityType(projectile).item();
    return context
        .getWorld()
        .getEntitiesByClass(
            ItemEntity.class,
            context.getTestBox().expand(6),
            item -> item.getStack().isOf(expectedItem))
        .stream()
        .findFirst()
        .orElse(null);
  }

  private Collection<TestFunction> generatePerProjectile(
      final String behavior, final int tickLimit, final PerProjectileBody body) {
    return FETCH_CASES.stream()
        .map(
            fetchCase ->
                new TestFunction(
                    "defaultBatch",
                    "fetchprojectilegametest." + behavior + "." + fetchCase.id(),
                    FabricGameTest.EMPTY_STRUCTURE,
                    tickLimit,
                    /* setupTicks */ 0L,
                    /* required */ true,
                    context -> body.run(context, fetchCase)))
        .toList();
  }

  @FunctionalInterface
  private interface PerProjectileBody {
    void run(TestContext context, FetchCase fetchCase);
  }
}
