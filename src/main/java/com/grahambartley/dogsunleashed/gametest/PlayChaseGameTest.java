package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogPlayChase;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.goal.PlayChaseGoal;
import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules;
import com.grahambartley.dogsunleashed.gametest.util.CreativeTestPlayer;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.gametest.util.SleepBatchFixture;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class PlayChaseGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String DAY_BATCH = "play-chase-day";
  private static final String NIGHT_BATCH = "play-chase-night";
  private static final long NOON = 6000;
  private static final BlockPos REL_A = new BlockPos(2, 2, 3);
  private static final BlockPos REL_B = new BlockPos(4, 2, 3);
  private static final BlockPos REL_C = new BlockPos(3, 2, 5);
  private static final int START_WINDOW_TICKS = 40;

  @BeforeBatch(batchId = DAY_BATCH)
  public void beforeDayBatch(final ServerWorld world) {
    SleepBatchFixture.prepare(world);
    world.setTimeOfDay(NOON);
  }

  @AfterBatch(batchId = DAY_BATCH)
  public void afterDayBatch(final ServerWorld world) {
    SleepBatchFixture.teardown(world);
  }

  @BeforeBatch(batchId = NIGHT_BATCH)
  public void beforeNightBatch(final ServerWorld world) {
    SleepBatchFixture.prepare(world);
    SleepBatchFixture.pinNight(world);
  }

  @AfterBatch(batchId = NIGHT_BATCH)
  public void afterNightBatch(final ServerWorld world) {
    SleepBatchFixture.teardown(world);
  }

  @GameTest(templateName = ARENA, batchId = DAY_BATCH, tickLimit = 20)
  public void aFreeRoamingDogPicksAnotherFreeRoamingDogAsItsPlaymate(final TestContext context) {
    final ServerPlayerEntity owner = CreativeTestPlayer.join(context);
    final UnleashedDogEntity dog = still(pet(context, owner, REL_A, DogCommand.FREE_ROAM));
    final UnleashedDogEntity roamer = still(pet(context, owner, REL_B, DogCommand.FREE_ROAM));
    still(pet(context, owner, REL_C, DogCommand.FOLLOW));

    context.assertTrue(
        new PlayChaseGoal(dog).findPlaymate() == roamer,
        "A free-roaming dog should pick the other free-roaming dog, not the one on Follow");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = DAY_BATCH, tickLimit = 20)
  public void aDogOnAnotherCommandFindsNoPlaymate(final TestContext context) {
    final ServerPlayerEntity owner = CreativeTestPlayer.join(context);
    final UnleashedDogEntity following = still(pet(context, owner, REL_A, DogCommand.FOLLOW));
    still(pet(context, owner, REL_B, DogCommand.FREE_ROAM));

    context.assertTrue(
        new PlayChaseGoal(following).findPlaymate() == null,
        "A dog on Follow should not start a game");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = DAY_BATCH, tickLimit = 20)
  public void wildPupsPlayOnlyWithPupsFromTheirOwnPack(final TestContext context) {
    final List<UnleashedDogEntity> litter =
        wildPack(context, DireWolfGrowthStage.PUP, REL_A, REL_B);
    wildPack(context, DireWolfGrowthStage.PUP, REL_C);
    final UnleashedDogEntity pup = litter.get(0);
    final UnleashedDogEntity sibling = litter.get(1);

    context.assertTrue(
        new PlayChaseGoal(pup).findPlaymate() == sibling,
        "A wild pup should play with its own packmate, never a stranger's pup");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = DAY_BATCH, tickLimit = 20)
  public void grownWildWolvesDoNotPlay(final TestContext context) {
    final UnleashedDogEntity adult =
        wildPack(context, DireWolfGrowthStage.ADULT, REL_A, REL_B).get(0);

    context.assertTrue(
        new PlayChaseGoal(adult).findPlaymate() == null, "Grown wild wolves should not play");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = DAY_BATCH, tickLimit = START_WINDOW_TICKS)
  public void twoFreeRoamingDogsRunAPlayChaseGoalOnceAGameStarts(final TestContext context) {
    final ServerPlayerEntity owner = CreativeTestPlayer.join(context);
    final UnleashedDogEntity dog = pet(context, owner, REL_A, DogCommand.FREE_ROAM);
    final UnleashedDogEntity partner = pet(context, owner, REL_B, DogCommand.FREE_ROAM);

    play(dog).startWith(partner);

    context.runAtEveryTick(
        () -> {
          if (isChasing(dog) && isChasing(partner)) {
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = DAY_BATCH, tickLimit = START_WINDOW_TICKS * 2)
  public void aCommandStopsTheGameForBothDogs(final TestContext context) {
    final ServerPlayerEntity owner = CreativeTestPlayer.join(context);
    final UnleashedDogEntity dog = pet(context, owner, REL_A, DogCommand.FREE_ROAM);
    final UnleashedDogEntity partner = pet(context, owner, REL_B, DogCommand.FREE_ROAM);
    final boolean[] commanded = {false};

    play(dog).startWith(partner);

    context.runAtEveryTick(
        () -> {
          if (!commanded[0]) {
            if (isChasing(dog) && isChasing(partner)) {
              dog.getCommandController().apply(DogCommand.FREE_ROAM);
              commanded[0] = true;
            }
            return;
          }
          if (!isChasing(dog)
              && !isChasing(partner)
              && !play(dog).isPlaying()
              && !play(partner).isPlaying()) {
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = DAY_BATCH, tickLimit = START_WINDOW_TICKS * 3)
  public void aDogWhosePartnerNeverJoinsGivesUpAndBothAreFreeAgain(final TestContext context) {
    final ServerPlayerEntity owner = CreativeTestPlayer.join(context);
    final UnleashedDogEntity dog = pet(context, owner, REL_A, DogCommand.FREE_ROAM);
    final UnleashedDogEntity busy = still(pet(context, owner, REL_B, DogCommand.FREE_ROAM));

    play(dog).startWith(busy);

    context.runAtEveryTick(
        () -> {
          if (context.getTick() > PlayChaseRules.BOW_TICKS
              && !play(dog).isPlaying()
              && !play(busy).isPlaying()) {
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = DAY_BATCH, tickLimit = 20)
  public void aHostileNearbyKeepsADogFromPlaying(final TestContext context) {
    final ServerPlayerEntity owner = CreativeTestPlayer.join(context);
    final UnleashedDogEntity dog = still(pet(context, owner, REL_A, DogCommand.FREE_ROAM));

    context.assertTrue(new PlayChaseGoal(dog).mayPlay(), "An idle free-roaming dog may play");
    context.spawnEntity(EntityType.ZOMBIE, REL_C).setAiDisabled(true);
    context.assertFalse(
        new PlayChaseGoal(dog).mayPlay(), "A dog with a zombie close by should not play");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 20)
  public void dogsDoNotPlayDuringSleepHours(final TestContext context) {
    final ServerPlayerEntity owner = CreativeTestPlayer.join(context);
    final UnleashedDogEntity dog = still(pet(context, owner, REL_A, DogCommand.FREE_ROAM));
    still(pet(context, owner, REL_B, DogCommand.FREE_ROAM));
    final PlayChaseGoal goal = new PlayChaseGoal(dog);

    context.assertFalse(goal.mayPlay(), "A dog should not start a game at night");
    context.assertTrue(goal.findPlaymate() == null, "No dog should be free to play at night");
    context.complete();
  }

  private static UnleashedDogEntity pet(
      final TestContext context,
      final ServerPlayerEntity owner,
      final BlockPos pos,
      final DogCommand command) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.BEAGLE, pos, owner.getUuid());
    dog.getCommandController().apply(command);
    dog.setOnGround(true);
    return dog;
  }

  private static List<UnleashedDogEntity> wildPack(
      final TestContext context, final DireWolfGrowthStage stage, final BlockPos... positions) {
    final List<UnleashedDogEntity> pack = new ArrayList<>();
    EntityData data = null;
    for (final BlockPos pos : positions) {
      final UnleashedDogEntity wolf = context.spawnEntity(ModEntities.DIRE_WOLF, pos);
      data =
          wolf.initialize(
              context.getWorld(),
              context.getWorld().getLocalDifficulty(wolf.getBlockPos()),
              SpawnReason.NATURAL,
              data);
      wolf.getGrowth().setStage(stage);
      wolf.setOnGround(true);
      pack.add(still(wolf));
    }
    return pack;
  }

  private static UnleashedDogEntity still(final UnleashedDogEntity dog) {
    dog.setAiDisabled(true);
    return dog;
  }

  private static DogPlayChase play(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogPlayChase.class);
  }

  private static boolean isChasing(final UnleashedDogEntity dog) {
    return dog.goalSelector.getGoals().stream()
        .filter(PrioritizedGoal::isRunning)
        .anyMatch(goal -> goal.getGoal() instanceof PlayChaseGoal);
  }
}
