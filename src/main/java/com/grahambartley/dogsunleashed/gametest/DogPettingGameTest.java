package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.block.DogSleepSpotAssignment;
import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.DogBedWheelAction;
import com.grahambartley.dogsunleashed.entity.DogBond;
import com.grahambartley.dogsunleashed.entity.DogBondRules;
import com.grahambartley.dogsunleashed.entity.DogPetting;
import com.grahambartley.dogsunleashed.entity.DogWheelSubActions;
import com.grahambartley.dogsunleashed.entity.PetSpot;
import com.grahambartley.dogsunleashed.entity.PettingRules;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.goal.PettedGoal;
import com.grahambartley.dogsunleashed.gametest.util.CreativeTestPlayer;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.GeneratedGameTest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class DogPettingGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String BATCH = "petting";
  private static final int TICK_LIMIT = 20;
  private static final int HOLD_TICK_LIMIT = 80;
  private static final BlockPos REL_DOG = new BlockPos(3, 2, 3);
  private static final Vec3d REL_PLAYER = new Vec3d(3.5, 2, 2.9);
  private static final Vec3d REL_OTHER_PLAYER = new Vec3d(2.9, 2, 3.5);
  private static final Vec3d REL_FAR_PLAYER = new Vec3d(3.5, 2, 0.5);
  private static final Vec3d REL_FAR_CORNER = new Vec3d(0.5, 2, 5.5);

  @BeforeBatch(batchId = BATCH)
  public void clearPendingBeds(final ServerWorld world) {
    DogSleepSpotAssignment.clearPendingAssignments();
  }

  @AfterBatch(batchId = BATCH)
  public void clearPendingBedsAfter(final ServerWorld world) {
    DogSleepSpotAssignment.clearPendingAssignments();
  }

  @CustomTestProvider
  public Collection<TestFunction> everyDogCanBePetted() {
    final List<TestFunction> tests = new ArrayList<>();
    for (final DogTestData data : DogTestData.getAllBreeds()) {
      if (data.breed() == UnleashedDogBreed.DIRE_WOLF) {
        continue;
      }
      for (final boolean puppy : List.of(false, true)) {
        tests.add(
            GeneratedGameTest.of(
                BATCH,
                "dogpettinggametest.pets."
                    + data.breed().serializedId()
                    + (puppy ? ".puppy" : ".adult"),
                ARENA,
                TICK_LIMIT,
                context -> assertAWildDogTakesAPet(context, data.entityType(), puppy)));
      }
    }
    for (final EntityType<UnleashedDogEntity> type :
        List.of(ModEntities.CROSS_BREED, ModEntities.ZOMBIE_HUSKY, ModEntities.ZOMBIE_DIRE_WOLF)) {
      tests.add(
          GeneratedGameTest.of(
              BATCH,
              "dogpettinggametest.pets." + EntityType.getId(type).getPath(),
              ARENA,
              TICK_LIMIT,
              context -> assertAWildDogTakesAPet(context, type, false)));
    }
    for (final DireWolfGrowthStage stage : DireWolfGrowthStage.values()) {
      tests.add(
          GeneratedGameTest.of(
              BATCH,
              "dogpettinggametest.pets.direwolf." + stage.name().toLowerCase(Locale.ROOT),
              ARENA,
              TICK_LIMIT,
              context -> assertADireWolfTakesAPetAt(context, stage)));
    }
    return tests;
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anOwnersPetGrowsTheBond(final TestContext context) {
    final ServerPlayerEntity owner = sneakingPlayerAt(context, REL_PLAYER);
    final UnleashedDogEntity dog = tamedDog(context, owner);

    context.assertTrue(
        dog.interactMob(owner, Hand.MAIN_HAND) == ActionResult.CONSUME, "The owner's pat is taken");
    afterTheKneel(
        context,
        dog,
        () ->
            context.assertTrue(
                bond(dog).points() == DogBondRules.Source.PET.points(),
                "A pat from the owner grows the bond, got " + bond(dog).points()));
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aStrangersPetLeavesTheBondAlone(final TestContext context) {
    final ServerPlayerEntity owner = sneakingPlayerAt(context, REL_OTHER_PLAYER);
    final ServerPlayerEntity stranger = sneakingPlayerAt(context, REL_PLAYER);
    final UnleashedDogEntity dog = tamedDog(context, owner);

    context.assertTrue(
        dog.interactMob(stranger, Hand.MAIN_HAND) == ActionResult.CONSUME,
        "A stranger may pet someone else's dog");
    afterTheKneel(
        context,
        dog,
        () -> {
          context.assertTrue(wags(dog), "The stranger's pat lands");
          context.assertTrue(bond(dog).points() == 0, "A stranger's pat must not grow the bond");
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void twoPlayersPettingInOneTickGetOneReaction(final TestContext context) {
    final ServerPlayerEntity first = sneakingPlayerAt(context, REL_PLAYER);
    final ServerPlayerEntity second = sneakingPlayerAt(context, REL_OTHER_PLAYER);
    final UnleashedDogEntity dog = wildDog(context, ModEntities.GOLDEN_RETRIEVER);

    final ActionResult firstPat = dog.interactMob(first, Hand.MAIN_HAND);
    final ActionResult secondPat = dog.interactMob(second, Hand.MAIN_HAND);

    context.assertTrue(firstPat == ActionResult.CONSUME, "The first pat is taken");
    context.assertTrue(
        secondPat != ActionResult.CONSUME, "A second pat in the same tick is ignored");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void thePatWaitsForThePlayerToKneel(final TestContext context) {
    final ServerPlayerEntity player = sneakingPlayerAt(context, REL_PLAYER);
    final UnleashedDogEntity dog = wildDog(context, ModEntities.BEAGLE);
    final PettedGoal hold = new PettedGoal(dog);

    dog.interactMob(player, Hand.MAIN_HAND);
    hold.start();

    context.assertFalse(wags(dog), "The dog waits while the player kneels");
    afterTheKneel(context, dog, () -> context.assertTrue(wags(dog), "The pat lands once knelt"));
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aPettedDogHoldsStillFacingThePetter(final TestContext context) {
    final ServerPlayerEntity player = sneakingPlayerAt(context, REL_PLAYER);
    final UnleashedDogEntity dog = wildDog(context, ModEntities.HUSKY);
    final PettedGoal hold = new PettedGoal(dog);
    dog.interactMob(player, Hand.MAIN_HAND);

    context.runAtTick(
        PettingRules.SETTLE_TICKS + 1L,
        () -> {
          dog.setOnGround(true);
          final Vec3d farCorner = context.getAbsolute(REL_FAR_CORNER);
          dog.getNavigation().startMovingTo(farCorner.x, farCorner.y, farCorner.z, 1.0);
          context.assertFalse(dog.getNavigation().isIdle(), "The dog is walking before the pat");

          hold.start();
          hold.tick();

          final PetSpot.Place spot = petting(dog).spotBeside(player);
          context.assertTrue(dog.getNavigation().isIdle(), "The petted dog stops walking");
          context.assertTrue(
              dog.getLookControl().isLookingAtSpecificPosition()
                  && dog.getLookControl().getLookX() == spot.faceX()
                  && dog.getLookControl().getLookZ() == spot.faceZ(),
              "The petted dog turns to stand alongside the player petting it");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aDogOutOfReachWalksToTheHand(final TestContext context) {
    final ServerPlayerEntity player = sneakingPlayerAt(context, REL_FAR_PLAYER);
    final UnleashedDogEntity dog = wildDog(context, ModEntities.BEAGLE);
    final PettedGoal walkOver = new PettedGoal(dog);
    dog.setOnGround(true);

    context.assertTrue(
        dog.interactMob(player, Hand.MAIN_HAND) == ActionResult.CONSUME,
        "A pat out of reach is still taken");
    context.assertTrue(walkOver.canStart(), "The dog sets off to the petter");
    walkOver.start();
    context.assertFalse(dog.getNavigation().isIdle(), "The dog walks over to the petter");

    context.runAtTick(
        PettingRules.SETTLE_TICKS + 1L,
        () -> {
          context.assertFalse(wags(dog), "The pat waits until the dog reaches the hand");
          arriveBeside(dog, player);
          walkOver.tick();
        });
    context.runAtTick(
        PettingRules.SETTLE_TICKS + 3L,
        () -> {
          context.assertTrue(wags(dog), "The pat lands once the dog arrives");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = HOLD_TICK_LIMIT)
  public void aDogThatNeverArrivesGivesUp(final TestContext context) {
    final ServerPlayerEntity player = sneakingPlayerAt(context, REL_FAR_PLAYER);
    final UnleashedDogEntity dog = wildDog(context, ModEntities.BEAGLE);
    final PettedGoal walkOver = new PettedGoal(dog);

    dog.interactMob(player, Hand.MAIN_HAND);

    context.runAtTick(
        PettingRules.SETTLE_TICKS + PettingRules.APPROACH_TICKS + 1L,
        () -> {
          context.assertFalse(walkOver.canStart(), "The dog stops walking over after a while");
          context.assertTrue(
              dog.interactMob(player, Hand.MAIN_HAND) == ActionResult.CONSUME,
              "A new pat can be asked for once the dog gives up");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void walkingAwayCallsThePatOff(final TestContext context) {
    final ServerPlayerEntity player = sneakingPlayerAt(context, REL_FAR_PLAYER);
    final UnleashedDogEntity dog = wildDog(context, ModEntities.BEAGLE);
    final PettedGoal walkOver = new PettedGoal(dog);

    dog.interactMob(player, Hand.MAIN_HAND);
    context.runAtTick(
        2L,
        () -> {
          final Vec3d away = context.getAbsolute(REL_FAR_PLAYER.add(2.0, 0.0, 0.0));
          player.refreshPositionAndAngles(away.x, away.y, away.z, 0.0f, 0.0f);
        });
    context.runAtTick(
        4L,
        () -> {
          context.assertFalse(walkOver.canStart(), "The dog stops when the petter walks off");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = HOLD_TICK_LIMIT)
  public void theHoldEndsWithThePat(final TestContext context) {
    final ServerPlayerEntity player = sneakingPlayerAt(context, REL_PLAYER);
    final UnleashedDogEntity dog = wildDog(context, ModEntities.HUSKY);
    final PettedGoal hold = new PettedGoal(dog);

    dog.interactMob(player, Hand.MAIN_HAND);
    context.runAtTick(PettingRules.SETTLE_TICKS + 1L, hold::start);
    context.runAtTick(
        PettingRules.SETTLE_TICKS + PettingRules.COOLDOWN_TICKS + 2L,
        () -> {
          context.assertFalse(hold.shouldContinue(), "The dog is free once the pat ends");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aNappingDogIsLeftToSleep(final TestContext context) {
    final ServerPlayerEntity player = sneakingPlayerAt(context, REL_PLAYER);
    final UnleashedDogEntity dog = wildDog(context, ModEntities.BEAGLE);
    dog.getSleepController().startNapping();

    context.assertTrue(
        dog.interactMob(player, Hand.MAIN_HAND) != ActionResult.CONSUME,
        "A napping dog is not woken by a pat");
    context.assertTrue(
        new PettedGoal(dog).canStart() == false, "A refused pat starts no walk over or hold");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aFullHandIsNotAPat(final TestContext context) {
    final ServerPlayerEntity owner = sneakingPlayerAt(context, REL_PLAYER);
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.STICK));
    final UnleashedDogEntity dog = tamedDog(context, owner);

    dog.interactMob(owner, Hand.MAIN_HAND);

    context.assertFalse(
        new PettedGoal(dog).canStart(), "Sneaking with an item in hand is not a pat");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theWheelPicksABedForTheDog(final TestContext context) {
    final ServerPlayerEntity owner = sneakingPlayerAt(context, REL_PLAYER);
    final UnleashedDogEntity dog = tamedDog(context, owner);

    DogWheelSubActions.REGISTERED
        .byId(DogBedWheelAction.WHEEL_ACTION_ID)
        .orElseThrow()
        .runner()
        .run(dog, owner);

    context.assertTrue(
        dog.getUuid().equals(DogSleepSpotAssignment.consumePendingAssignment(owner.getUuid())),
        "Assign bed waits for the owner to pick a bed for this dog");
    context.complete();
  }

  private static void assertAWildDogTakesAPet(
      final TestContext context, final EntityType<UnleashedDogEntity> type, final boolean puppy) {
    final ServerPlayerEntity player = sneakingPlayerAt(context, REL_PLAYER);
    final UnleashedDogEntity dog = wildDog(context, type);
    dog.setBaby(puppy);

    context.assertTrue(
        dog.interactMob(player, Hand.MAIN_HAND) == ActionResult.CONSUME,
        "A sneaking player with an empty hand pets the dog");
    afterTheKneel(
        context, dog, () -> context.assertTrue(wags(dog), "The petted dog wags its tail"));
  }

  private static void assertADireWolfTakesAPetAt(
      final TestContext context, final DireWolfGrowthStage stage) {
    final ServerPlayerEntity player = sneakingPlayerAt(context, REL_PLAYER);
    final UnleashedDogEntity wolf = wildDog(context, ModEntities.DIRE_WOLF);
    wolf.getGrowth().setStage(stage);

    context.assertTrue(
        wolf.interactMob(player, Hand.MAIN_HAND) == ActionResult.CONSUME,
        "A dire wolf takes a pat at stage " + stage);
    afterTheKneel(
        context, wolf, () -> context.assertTrue(wags(wolf), "The petted dire wolf wags its tail"));
  }

  private static void afterTheKneel(
      final TestContext context, final UnleashedDogEntity dog, final Runnable check) {
    petting(dog).approachingPetter().ifPresent(petter -> arriveBeside(dog, petter));
    context.runAtTick(
        PettingRules.SETTLE_TICKS + 1L,
        () -> {
          new PettedGoal(dog).start();
          check.run();
          context.complete();
        });
  }

  private static void arriveBeside(final UnleashedDogEntity dog, final PlayerEntity petter) {
    final PetSpot.Place spot = petting(dog).spotBeside(petter);
    final float yaw =
        (float)
                (MathHelper.atan2(spot.faceZ() - spot.z(), spot.faceX() - spot.x())
                    * MathHelper.DEGREES_PER_RADIAN)
            - 90.0f;
    dog.refreshPositionAndAngles(spot.x(), petter.getY(), spot.z(), yaw, 0.0f);
    dog.setBodyYaw(yaw);
    dog.setHeadYaw(yaw);
  }

  private static DogPetting petting(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogPetting.class);
  }

  private static boolean wags(final UnleashedDogEntity dog) {
    return dog.getAnimationState().getTailWagTimerTicks() > 0;
  }

  private static ServerPlayerEntity sneakingPlayerAt(
      final TestContext context, final Vec3d relative) {
    final ServerPlayerEntity player = CreativeTestPlayer.join(context);
    player.refreshPositionAndAngles(context.getAbsolute(relative), 0.0f, 0.0f);
    player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
    player.setSneaking(true);
    player.setOnGround(true);
    return player;
  }

  private static UnleashedDogEntity wildDog(
      final TestContext context, final EntityType<UnleashedDogEntity> type) {
    final UnleashedDogEntity dog = context.spawnEntity(type, REL_DOG);
    dog.setAiDisabled(true);
    return dog;
  }

  private static UnleashedDogEntity tamedDog(
      final TestContext context, final ServerPlayerEntity owner) {
    final UnleashedDogEntity dog = wildDog(context, ModEntities.BEAGLE);
    dog.setTamed(true, true);
    dog.setOwnerUuid(owner.getUuid());
    return dog;
  }

  private static DogBond bond(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogBond.class);
  }
}
