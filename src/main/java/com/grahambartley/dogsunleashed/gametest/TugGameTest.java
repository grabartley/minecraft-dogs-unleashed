package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.entity.DogBond;
import com.grahambartley.dogsunleashed.entity.DogBondRules;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogTug;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.gametest.util.SleepBatchFixture;
import com.grahambartley.dogsunleashed.gametest.util.SurvivalTestPlayer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class TugGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String BATCH = "tug";
  private static final long NOON = 6000;
  private static final BlockPos REL_DOG = new BlockPos(3, 2, 3);
  private static final Vec3d REL_OWNER = new Vec3d(3.5, 2, 1.5);
  private static final Vec3d REL_STRANGER = new Vec3d(5.5, 2, 3.5);
  private static final int RHYTHM_GAP_TICKS = 6;

  @BeforeBatch(batchId = BATCH)
  public void beforeBatch(final ServerWorld world) {
    SleepBatchFixture.prepare(world);
    world.setTimeOfDay(NOON);
  }

  @AfterBatch(batchId = BATCH)
  public void afterBatch(final ServerWorld world) {
    SleepBatchFixture.teardown(world);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void startingATugMovesTheRopeFromTheHandIntoTheDogsMouth(final TestContext context) {
    final ServerPlayerEntity owner = ownerWithRope(context);
    final UnleashedDogEntity dog = pet(context, owner);

    dog.interact(owner, Hand.MAIN_HAND);

    context.assertTrue(tug(dog).isTugging(), "Using a rope toy on your dog should start a tug");
    context.assertTrue(tug(dog).isCarryingRope(), "The dog should hold the rope in its mouth");
    context.assertTrue(
        tug(dog).tuggingPlayerId() == owner.getId(),
        "Clients should see which player holds the other end");
    assertOneRope(context, owner, dog);
    SurvivalTestPlayer.leave(context, owner);
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 300)
  public void pullingInRhythmWinsAndTheDogLetsGo(final TestContext context) {
    final ServerPlayerEntity owner = ownerWithRope(context);
    final UnleashedDogEntity dog = pet(context, owner);
    dog.interact(owner, Hand.MAIN_HAND);
    final long started = context.getTick();

    context.runAtEveryTick(
        () -> {
          if (tug(dog).state() == null) {
            return;
          }
          if ((context.getTick() - started) % RHYTHM_GAP_TICKS == 0) {
            dog.interact(owner, Hand.MAIN_HAND);
          }
        });
    context.runAtEveryTick(
        () -> {
          if (tug(dog).state() == null && owner.getInventory().count(ModItems.ROPE_TOY) == 1) {
            context.assertTrue(!tug(dog).isCarryingRope(), "The dog should have let go");
            context.assertTrue(
                dog.getComponents().get(DogBond.class).earnedToday(DogBondRules.Source.PLAY) > 0,
                "Tugging should count as play for the bond");
            assertOneRope(context, owner, dog);
            SurvivalTestPlayer.leave(context, owner);
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 400)
  public void aDogThatOutPullsYouRunsOffAndHandsTheRopeBackWhenCaught(final TestContext context) {
    final ServerPlayerEntity owner = ownerWithRope(context);
    final UnleashedDogEntity dog = pet(context, owner);
    dog.interact(owner, Hand.MAIN_HAND);
    final boolean[] caught = {false};

    context.runAtEveryTick(
        () -> {
          if (caught[0] || !tug(dog).isKeepingAway()) {
            return;
          }
          caught[0] = true;
          context.assertTrue(
              tug(dog).isCarryingRope(), "A dog that wins should run off with the rope");
          context.assertTrue(
              tug(dog).tuggingPlayerId() == DogTug.NO_PLAYER,
              "The meter should go away once the dog has won");
          assertOneRope(context, owner, dog);
          owner.refreshPositionAndAngles(dog.getX(), dog.getY(), dog.getZ() - 1.0, 0.0f, 0.0f);
          dog.interact(owner, Hand.MAIN_HAND);
          context.assertTrue(
              owner.getInventory().count(ModItems.ROPE_TOY) == 1,
              "Catching the dog should give the rope back");
          context.assertTrue(tug(dog).state() == null, "Catching the dog should end the game");
          assertOneRope(context, owner, dog);
          SurvivalTestPlayer.leave(context, owner);
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 600)
  public void aDogThatKeepsTheRopeAwayStillHoldsItWhenTheGameEnds(final TestContext context) {
    final ServerPlayerEntity owner = ownerWithRope(context);
    final UnleashedDogEntity dog = pet(context, owner);
    dog.interact(owner, Hand.MAIN_HAND);
    final boolean[] ranOff = {false};

    context.runAtEveryTick(
        () -> {
          ranOff[0] |= tug(dog).isKeepingAway();
          if (ranOff[0] && tug(dog).state() == null) {
            context.assertTrue(
                tug(dog).isCarryingRope()
                    || owner.getInventory().count(ModItems.ROPE_TOY) == 1
                    || ropesOnTheGround(context) == 1,
                "The rope should still be with the dog or back with its owner");
            assertOneRope(context, owner, dog);
            SurvivalTestPlayer.leave(context, owner);
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aCommandStopsTheTugAndHandsTheRopeBack(final TestContext context) {
    final ServerPlayerEntity owner = ownerWithRope(context);
    final UnleashedDogEntity dog = pet(context, owner);
    dog.interact(owner, Hand.MAIN_HAND);

    dog.getCommandController().apply(DogCommand.SIT);

    context.assertTrue(tug(dog).state() == null, "A command should stop the tug");
    context.assertTrue(!tug(dog).isCarryingRope(), "The dog should let go of the rope");
    context.assertTrue(
        owner.getInventory().count(ModItems.ROPE_TOY) == 1, "The owner should get the rope back");
    assertOneRope(context, owner, dog);
    SurvivalTestPlayer.leave(context, owner);
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void walkingAwayStopsTheTugAndHandsTheRopeBack(final TestContext context) {
    final ServerPlayerEntity owner = ownerWithRope(context);
    final UnleashedDogEntity dog = pet(context, owner);
    dog.interact(owner, Hand.MAIN_HAND);
    owner.refreshPositionAndAngles(dog.getX() + 20.0, dog.getY(), dog.getZ(), 0.0f, 0.0f);

    context.waitAndRun(
        2,
        () -> {
          context.assertTrue(tug(dog).state() == null, "Walking off should stop the tug");
          context.assertTrue(
              owner.getInventory().count(ModItems.ROPE_TOY) == 1,
              "The owner should get the rope back");
          SurvivalTestPlayer.leave(context, owner);
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void anotherPlayerCannotPullOrStartASecondTug(final TestContext context) {
    final ServerPlayerEntity owner = ownerWithRope(context);
    final ServerPlayerEntity stranger = SurvivalTestPlayer.joinAt(context, REL_STRANGER);
    stranger.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModItems.ROPE_TOY));
    final UnleashedDogEntity dog = pet(context, owner);
    dog.interact(owner, Hand.MAIN_HAND);
    final double before = tug(dog).state().balance();

    dog.interact(stranger, Hand.MAIN_HAND);

    context.assertTrue(
        tug(dog).state() != null && tug(dog).state().isPlayer(owner.getUuid()),
        "The tug should stay with the owner");
    context.assertTrue(
        tug(dog).state().balance() == before, "Another player's click should not pull the rope");
    context.assertTrue(
        stranger.getInventory().count(ModItems.ROPE_TOY) == 1,
        "Another player's rope should stay in their hand");
    assertOneRope(context, owner, dog);
    SurvivalTestPlayer.leave(context, stranger);
    SurvivalTestPlayer.leave(context, owner);
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aDogKilledMidTugDropsTheRope(final TestContext context) {
    final ServerPlayerEntity owner = ownerWithRope(context);
    final UnleashedDogEntity dog = pet(context, owner);
    dog.interact(owner, Hand.MAIN_HAND);

    dog.kill();

    context.waitAndRun(
        1,
        () -> {
          context.assertTrue(
              ropesOnTheGround(context) + owner.getInventory().count(ModItems.ROPE_TOY) == 1,
              "Exactly one rope should be left after the dog dies");
          SurvivalTestPlayer.leave(context, owner);
          context.complete();
        });
  }

  private static ServerPlayerEntity ownerWithRope(final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_OWNER);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModItems.ROPE_TOY));
    return owner;
  }

  private static UnleashedDogEntity pet(final TestContext context, final ServerPlayerEntity owner) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.BEAGLE, REL_DOG, owner.getUuid());
    dog.getCommandController().apply(DogCommand.FOLLOW);
    dog.setOnGround(true);
    return dog;
  }

  private static DogTug tug(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogTug.class);
  }

  private static int ropesOnTheGround(final TestContext context) {
    return context
        .getWorld()
        .getEntitiesByClass(
            ItemEntity.class,
            context.getTestBox().expand(2.0),
            item -> item.getStack().isOf(ModItems.ROPE_TOY))
        .stream()
        .mapToInt(item -> item.getStack().getCount())
        .sum();
  }

  private static void assertOneRope(
      final TestContext context, final ServerPlayerEntity owner, final UnleashedDogEntity dog) {
    final int total =
        owner.getInventory().count(ModItems.ROPE_TOY)
            + (tug(dog).isCarryingRope() ? dog.getFetchCarry().carriedStack().getCount() : 0)
            + ropesOnTheGround(context);
    context.assertTrue(total == 1, "There should be exactly one rope in the world, found " + total);
  }
}
