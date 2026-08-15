package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;

public final class DogLeashGameTest implements FabricGameTest {

  private static final String INTERACT_BATCH = "leash-interact";
  private static final String SLEEP_BATCH = "leash-sleep";
  private static final BlockPos DOG_POS = new BlockPos(3, 2, 3);
  private static final BlockPos BED_POS = new BlockPos(2, 2, 3);
  private static final BlockPos OWNER_POS = new BlockPos(4, 2, 3);
  private static final long NIGHT_TICK = 15000;

  @BeforeBatch(batchId = INTERACT_BATCH)
  public void clearSessionsBefore(final ServerWorld world) {
    UnleashedDogEntity.clearActivePlaySessions();
  }

  @AfterBatch(batchId = INTERACT_BATCH)
  public void clearSessionsAfter(final ServerWorld world) {
    UnleashedDogEntity.clearActivePlaySessions();
  }

  @BeforeBatch(batchId = SLEEP_BATCH)
  public void freezeDaylight(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, world.getServer());
  }

  @AfterBatch(batchId = SLEEP_BATCH)
  public void restoreDaylight(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, world.getServer());
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = SLEEP_BATCH, tickLimit = 200)
  public void leashedDogDoesNotAutoSleepAtNight(final TestContext context) {
    context.setBlockState(BED_POS, ModBlocks.DOG_BED.getDefaultState());
    final BlockPos absBedPos = context.getAbsolutePos(BED_POS);
    final ServerPlayerEntity owner = spawnOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS, owner.getUuid());
    dog.setAssignedBedPos(absBedPos);
    dog.attachLeash(owner, true);

    context.runAtTick(10, () -> context.setTime((int) NIGHT_TICK));

    context.runAtTick(
        150,
        () -> {
          context.assertTrue(dog.isLeashed(), "The leash should still be attached mid-test");
          context.assertFalse(
              dog.isSleepingInBed(), "A leashed dog must not auto-sleep, even at night");
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = INTERACT_BATCH, tickLimit = 40)
  public void attachingLeashWakesDogSleepingInBed(final TestContext context) {
    final ServerPlayerEntity owner = spawnOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS);
    dog.setAiDisabled(true);
    dog.startSleepingInBed(context.getAbsolutePos(BED_POS));
    context.assertTrue(dog.isSleepingInBed(), "The dog should be asleep before the leash attaches");

    dog.attachLeash(owner, true);

    context.waitAndRun(
        5,
        () -> {
          context.assertFalse(
              dog.isSleepingInBed(), "Attaching a leash should wake a dog sleeping in its bed");
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = INTERACT_BATCH, tickLimit = 40)
  public void attachingLeashCancelsCommandedSleep(final TestContext context) {
    final ServerPlayerEntity owner = spawnOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog = DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS);
    dog.setAiDisabled(true);
    dog.commandToSleep(context.getAbsolutePos(BED_POS));
    context.assertTrue(
        dog.isCommandedToSleep(), "The dog should be en route to bed before the leash attaches");

    dog.attachLeash(owner, true);

    context.waitAndRun(
        5,
        () -> {
          context.assertFalse(
              dog.isCommandedToSleep(), "Attaching a leash should cancel a commanded sleep");
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = INTERACT_BATCH, tickLimit = 20)
  public void sneakFetchInteractDropsLeashAndStartsPlayMode(final TestContext context) {
    final ServerPlayerEntity owner = spawnOwnerAt(context, OWNER_POS);
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS, owner.getUuid());
    dog.setAiDisabled(true);
    dog.attachLeash(owner, true);
    owner.setSneaking(true);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.STICK));

    dog.interactMob(owner, Hand.MAIN_HAND);

    context.assertTrue(
        dog.isInPlayMode(), "Sneak-right-click with a fetch item should start play mode");
    context.assertFalse(
        dog.isLeashed(), "Starting play mode should drop the leash when the config is on");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = INTERACT_BATCH, tickLimit = 20)
  public void sneakFetchInteractKeepsLeashWhenDropDisabled(final TestContext context) {
    final DogsUnleashedConfig original = DogsUnleashed.SERVER_CONFIG;
    DogsUnleashed.SERVER_CONFIG = original.withDropLeashOnPlayMode(false);
    try {
      final ServerPlayerEntity owner = spawnOwnerAt(context, OWNER_POS);
      final UnleashedDogEntity dog =
          DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS, owner.getUuid());
      dog.setAiDisabled(true);
      dog.attachLeash(owner, true);
      owner.setSneaking(true);
      owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.STICK));

      dog.interactMob(owner, Hand.MAIN_HAND);

      context.assertTrue(
          dog.isInPlayMode(), "Play mode should still start while the drop-leash config is off");
      context.assertTrue(
          dog.isLeashed(), "The leash should stay attached while the drop-leash config is off");
    } finally {
      DogsUnleashed.SERVER_CONFIG = original;
    }
    context.complete();
  }

  private ServerPlayerEntity spawnOwnerAt(final TestContext context, final BlockPos relativePos) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final BlockPos abs = context.getAbsolutePos(relativePos);
    owner.refreshPositionAndAngles(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5, 0f, 0f);
    return owner;
  }
}
