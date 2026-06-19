package com.grahambartley.gametest;

import com.grahambartley.ModEntities;
import com.grahambartley.entity.StickProjectileEntity;
import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.entity.fetch.FetchTypes;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

/**
 * Real-behavior coverage of the {@code StickThrowHandler} play-mode gate (#179). The handler is the
 * mod's only {@link UseItemCallback}; before #179 every non-sneaking stick right-click launched a
 * {@link StickProjectileEntity}. Now it only throws when the player is the play-mode partner of a
 * tamed dog ({@code UnleashedDogEntity.isAnyDogInPlayModeFor}).
 *
 * <p>Tests fire the real {@code UseItemCallback.EVENT} invoker rather than calling the handler
 * method directly, so they also assert the callback stays registered: deleting the {@code
 * register()} wiring would flip the throw case from CONSUME to PASS and fail here.
 *
 * <p>These need a live {@code ServerWorld}: {@code Items.STICK}, {@code
 * ModEntities.STICK_PROJECTILE} construction, {@code world.spawnEntity}, and the JVM-global
 * play-session map all touch class init the JUnit classloader cannot complete, so this lives here
 * rather than under {@code src/test/java}.
 *
 * <p>The play-session map is JVM-global, so the batch resets it before and after to avoid leaking a
 * session into a sibling test (gametest skill rule 5).
 */
public final class StickThrowHandlerGameTest implements FabricGameTest {

  private static final String BATCH = "stick-throw";
  private static final BlockPos PLAYER_POS = new BlockPos(0, 1, 0);

  @BeforeBatch(batchId = BATCH)
  public void clearSessionsBefore(final ServerWorld world) {
    UnleashedDogEntity.clearActivePlaySessions();
  }

  @AfterBatch(batchId = BATCH)
  public void clearSessionsAfter(final ServerWorld world) {
    UnleashedDogEntity.clearActivePlaySessions();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 5)
  public void rightClickWithoutPlayModeDoesNotThrow(final TestContext context) {
    final PlayerEntity player = spawnStickHolder(context);

    final TypedActionResult<ItemStack> result = fireUse(context, player);

    context.assertTrue(
        result.getResult() == ActionResult.PASS,
        "Right-click with a stick outside play mode should pass through, was "
            + result.getResult());
    context.assertTrue(
        stickProjectileCount(context) == 0,
        "No stick projectile should spawn when the player is not in play mode");
    context.assertTrue(
        player.getStackInHand(Hand.MAIN_HAND).getCount() == 1,
        "The stick should not be consumed when no throw happens");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 5)
  public void rightClickInPlayModeThrowsStick(final TestContext context) {
    final PlayerEntity player = spawnStickHolder(context);
    startPlayModeWithDog(context, player);

    final TypedActionResult<ItemStack> result = fireUse(context, player);

    // Server-side success carries swingHand=false (world.isClient), which maps to CONSUME, not
    // SUCCESS; SUCCESS is the client-only swing variant.
    context.assertTrue(
        result.getResult() == ActionResult.CONSUME,
        "Right-click with a stick in play mode should consume the throw, was "
            + result.getResult());
    context.assertTrue(
        stickProjectileCount(context) == 1,
        "Exactly one stick projectile should spawn when the player is in play mode");
    context.assertTrue(
        player.getStackInHand(Hand.MAIN_HAND).getCount() == 0,
        "The thrown stick should be consumed from a survival player's hand");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 5)
  public void sneakingBypassesThrowEvenInPlayMode(final TestContext context) {
    final PlayerEntity player = spawnStickHolder(context);
    startPlayModeWithDog(context, player);
    player.setSneaking(true);

    final TypedActionResult<ItemStack> result = fireUse(context, player);

    context.assertTrue(
        result.getResult() == ActionResult.PASS,
        "Sneaking should bypass the throw so play-mode entry/exit stays usable, was "
            + result.getResult());
    context.assertTrue(
        stickProjectileCount(context) == 0,
        "No stick projectile should spawn while the player is sneaking");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 5)
  public void nonStickItemInPlayModePassesThrough(final TestContext context) {
    final PlayerEntity player = spawnStickHolder(context);
    startPlayModeWithDog(context, player);
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.WOODEN_PICKAXE));

    final TypedActionResult<ItemStack> result = fireUse(context, player);

    context.assertTrue(
        result.getResult() == ActionResult.PASS,
        "A non-stick item in play mode should pass through untouched, was " + result.getResult());
    context.assertTrue(
        stickProjectileCount(context) == 0,
        "No stick projectile should spawn for a non-stick item even in play mode");
    context.complete();
  }

  /** Spawns a tamed-dog play partner for the player so {@code isAnyDogInPlayModeFor} is true. */
  private void startPlayModeWithDog(final TestContext context, final PlayerEntity player) {
    context.spawnEntity(ModEntities.HUSKY, PLAYER_POS).startPlayMode(player, FetchTypes.STICK);
  }

  /**
   * Runs the real {@code UseItemCallback} chain, the same entry point production right-clicks hit.
   */
  private TypedActionResult<ItemStack> fireUse(
      final TestContext context, final PlayerEntity player) {
    return UseItemCallback.EVENT.invoker().interact(player, context.getWorld(), Hand.MAIN_HAND);
  }

  /**
   * Survival mock player positioned inside the structure (so a thrown projectile lands in the test
   * box) with a single stick in the main hand.
   */
  private PlayerEntity spawnStickHolder(final TestContext context) {
    final PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
    final BlockPos abs = context.getAbsolutePos(PLAYER_POS);
    player.refreshPositionAndAngles(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5, 0f, 0f);
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.STICK));
    return player;
  }

  private int stickProjectileCount(final TestContext context) {
    return context
        .getWorld()
        .getEntitiesByClass(
            StickProjectileEntity.class, context.getTestBox().expand(4), entity -> true)
        .size();
  }
}
