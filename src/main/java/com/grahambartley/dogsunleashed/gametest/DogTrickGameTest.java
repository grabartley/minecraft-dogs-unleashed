package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.block.entity.DogGraveBlockEntity;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.DogResurrection;
import com.grahambartley.dogsunleashed.entity.DogSledHarness;
import com.grahambartley.dogsunleashed.entity.DogTrickPerformance;
import com.grahambartley.dogsunleashed.entity.DogTricks;
import com.grahambartley.dogsunleashed.entity.DogWheelSubActions;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.sled.SledEntity;
import com.grahambartley.dogsunleashed.entity.trick.DogTrick;
import com.grahambartley.dogsunleashed.entity.trick.DogTrickWheelActions;
import com.grahambartley.dogsunleashed.entity.trick.TrickLearningRules;
import com.grahambartley.dogsunleashed.entity.trick.TrickProgress;
import com.grahambartley.dogsunleashed.gametest.util.CreativeTestPlayer;
import com.grahambartley.dogsunleashed.gametest.util.SurvivalTestPlayer;
import com.grahambartley.dogsunleashed.network.payload.WheelSubActionState;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.grahambartley.dogsunleashed.pet.PetLifeState;
import com.grahambartley.dogsunleashed.pet.PetManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class DogTrickGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos REL_DOG = new BlockPos(3, 2, 3);
  private static final Vec3d REL_OWNER = new Vec3d(3.5, 2, 1.5);
  private static final String RANGE = "dogs-unleashed:dog_range";
  private static final Vec3d REL_SLED = new Vec3d(1.5, 2, 4.5);
  private static final Vec3d REL_SLED_DRIVER = new Vec3d(1.5, 2, 6.5);
  private static final BlockPos REL_TEAM_DOG = new BlockPos(4, 2, 3);
  private static final BlockPos REL_FAR_CORNER = new BlockPos(0, 2, 6);
  private static final int TRICK_START = 5;

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void fiveRewardedTricksAreLearned(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context);
    final UnleashedDogEntity dog = dog(context, owner);

    for (int reward = 1; reward <= TrickLearningRules.REWARDS_TO_LEARN; reward++) {
      context.assertFalse(tricks(dog).progress().isLearned(DogTrick.BEG), "Not learned yet");
      tricks(dog).offer(DogTrick.BEG);
      feedTreat(dog, owner);
      context.assertEquals(
          reward, tricks(dog).progress().rewardsFor(DogTrick.BEG), "Each reward counts once");
    }

    context.assertTrue(tricks(dog).progress().isLearned(DogTrick.BEG), "Five rewards teach it");
    context.assertTrue(advancementDone(context, owner, "good_dog"), "First trick unlocks good_dog");
    context.assertFalse(advancementDone(context, owner, "show_dog"), "One trick is not all");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void treatWithNoTrickDoesNotCount(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context);
    final UnleashedDogEntity dog = dog(context, owner);

    feedTreat(dog, owner);

    context.assertEquals(
        TrickProgress.NONE, tricks(dog).progress(), "A treat with no trick shown teaches nothing");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TrickLearningRules.REWARD_WINDOW_TICKS + 40)
  public void treatAfterTheWindowDoesNotCount(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context);
    final UnleashedDogEntity dog = dog(context, owner);
    tricks(dog).offer(DogTrick.ROLL_OVER);

    context.runAtTick(
        TrickLearningRules.REWARD_WINDOW_TICKS + 10,
        () -> {
          context.assertTrue(tricks(dog).offered().isEmpty(), "The window has closed");
          feedTreat(dog, owner);
          context.assertEquals(
              0, tricks(dog).progress().rewardsFor(DogTrick.ROLL_OVER), "A late treat is ignored");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void oneTreatClosesTheWindow(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context);
    final UnleashedDogEntity dog = dog(context, owner);
    tricks(dog).offer(DogTrick.PLAY_DEAD);

    feedTreat(dog, owner);
    feedTreat(dog, owner);

    context.assertEquals(
        1, tricks(dog).progress().rewardsFor(DogTrick.PLAY_DEAD), "Only the first treat counts");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void everyTrickLearnedUnlocksShowDog(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context);
    final UnleashedDogEntity dog = dog(context, owner);

    for (final DogTrick trick : DogTrick.values()) {
      for (int reward = 0; reward < TrickLearningRules.REWARDS_TO_LEARN; reward++) {
        tricks(dog).offer(trick);
        feedTreat(dog, owner);
      }
    }

    context.assertEquals(
        DogTrick.values().length, tricks(dog).progress().learned().size(), "All tricks learned");
    context.assertTrue(advancementDone(context, owner, "show_dog"), "All three unlock show_dog");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void onlyLearnedTricksShowOnTheWheel(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context);
    final UnleashedDogEntity dog = dog(context, owner);
    context.assertFalse(onWheel(dog, owner, DogTrick.BEG), "An unlearned trick is hidden");

    tricks(dog).restore(TrickProgress.of(Map.of(DogTrick.BEG, 5)));

    context.assertTrue(onWheel(dog, owner, DogTrick.BEG), "A learned trick shows on the wheel");
    context.assertFalse(onWheel(dog, owner, DogTrick.PLAY_DEAD), "Other tricks stay hidden");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void learnedTricksSurviveSaveAndLoad(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context);
    final UnleashedDogEntity dog = dog(context, owner);
    final TrickProgress progress = TrickProgress.of(Map.of(DogTrick.BEG, 5, DogTrick.ROLL_OVER, 2));
    tricks(dog).restore(progress);
    final NbtCompound nbt = new NbtCompound();
    dog.writeCustomDataToNbt(nbt);

    final UnleashedDogEntity reloaded = context.spawnEntity(ModEntities.BEAGLE, REL_DOG);
    reloaded.setAiDisabled(true);
    reloaded.readCustomDataFromNbt(nbt);

    context.assertEquals(progress, tricks(reloaded).progress(), "Tricks reload as saved");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void learnedTricksAreKeptInPetData(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context);
    final UnleashedDogEntity dog = dog(context, owner);
    final TrickProgress progress = TrickProgress.of(Map.of(DogTrick.PLAY_DEAD, 5));
    tricks(dog).restore(progress);
    final PetData pet = petOf(context, dog, owner);

    context.assertEquals(
        progress,
        PetData.fromNbt(pet.toNbt()).getTricks(),
        "Pet data keeps learned tricks after death");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aSleepingDogRefusesItsTricks(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context);
    final UnleashedDogEntity dog = dog(context, owner);
    tricks(dog).restore(TrickProgress.of(Map.of(DogTrick.BEG, 5)));
    context.assertEquals("", wheelRefusal(dog, owner, DogTrick.BEG), "An awake dog performs");

    dog.getSleepController().startSleepingInBed(context.getAbsolutePos(REL_DOG));

    context.assertEquals(
        DogTrickWheelActions.ASLEEP_REFUSAL_KEY,
        wheelRefusal(dog, owner, DogTrick.BEG),
        "A sleeping dog refuses");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aDogAlreadyDoingATrickRefusesAnother(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context);
    final UnleashedDogEntity dog = dog(context, owner);
    tricks(dog).restore(TrickProgress.of(Map.of(DogTrick.BEG, 5)));

    performance(dog).perform(DogTrick.BEG);

    context.assertEquals(
        DogTrickWheelActions.BUSY_REFUSAL_KEY,
        wheelRefusal(dog, owner, DogTrick.BEG),
        "A dog mid trick is too busy for another");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void gettingHurtStopsATrick(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context);
    final UnleashedDogEntity dog = dog(context, owner);
    performance(dog).perform(DogTrick.PLAY_DEAD);

    dog.damage(context.getWorld().getDamageSources().generic(), 1.0f);

    context.runAtTick(
        3,
        () -> {
          context.assertFalse(performance(dog).isPerforming(), "A hurt dog drops its trick");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TRICK_START + 120)
  public void aDogHoldsStillThroughItsTrick(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context);
    final UnleashedDogEntity dog = standingDog(context, owner);
    final Vec3d[] start = new Vec3d[1];

    context.runAtTick(TRICK_START, () -> performance(dog).perform(DogTrick.PLAY_DEAD));
    context.runAtTick(TRICK_START + 3, () -> start[0] = dog.getPos());
    context.runAtEveryTick(
        () -> {
          if (context.getTick() > TRICK_START + 3 && performance(dog).isPerforming()) {
            walkToFarCorner(context, dog);
          }
        });
    context.runAtTick(
        TRICK_START + DogTrick.PLAY_DEAD.durationTicks() - 4,
        () -> {
          context.assertTrue(performance(dog).isPerforming(), "The trick is still running");
          final double moved = dog.getPos().subtract(start[0]).horizontalLength();
          context.assertTrue(moved < 0.05, "A dog mid trick moved " + moved + " blocks");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TRICK_START + 120)
  public void aDogMovesAgainOnceItsTrickEnds(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context);
    final UnleashedDogEntity dog = standingDog(context, owner);
    final int trickEnd = TRICK_START + DogTrick.BEG.durationTicks();
    final Vec3d[] start = new Vec3d[1];

    context.runAtTick(TRICK_START, () -> performance(dog).perform(DogTrick.BEG));
    context.runAtTick(
        trickEnd + 2,
        () -> {
          context.assertFalse(performance(dog).isPerforming(), "The trick has ended");
          start[0] = dog.getPos();
        });
    context.runAtEveryTick(
        () -> {
          if (context.getTick() > trickEnd + 2) {
            walkToFarCorner(context, dog);
          }
        });
    context.runAtTick(
        trickEnd + 60,
        () -> {
          final double moved = dog.getPos().subtract(start[0]).horizontalLength();
          context.assertTrue(moved > 1.0, "A dog done with its trick only moved " + moved);
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TRICK_START + 40)
  public void aSitOrderDoesNotWaitForTheTrick(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context);
    final UnleashedDogEntity dog = standingDog(context, owner);

    context.runAtTick(TRICK_START, () -> performance(dog).perform(DogTrick.PLAY_DEAD));
    context.runAtTick(TRICK_START + 4, () -> dog.getCommandController().apply(DogCommand.SIT));
    context.runAtTick(
        TRICK_START + 20,
        () -> {
          context.assertTrue(performance(dog).isPerforming(), "The trick is still running");
          context.assertTrue(dog.isInSittingPose(), "A dog told to sit sits straight away");
          context.complete();
        });
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void aDogPullingASledIsTooBusyForTricks(final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_SLED_DRIVER);
    final SledEntity sled = sled(context);
    final UnleashedDogEntity husky = leashedHusky(context, owner);
    tricks(husky).restore(TrickProgress.of(Map.of(DogTrick.ROLL_OVER, 5)));

    sled.interact(owner, Hand.MAIN_HAND);

    context.assertTrue(
        husky.getComponents().get(DogSledHarness.class).isHitched(), "The husky is hitched");
    context.assertFalse(
        performance(husky).readiness().mayStart(), "A hitched dog offers no tricks");
    context.assertEquals(
        DogTrickWheelActions.BUSY_REFUSAL_KEY,
        wheelRefusal(husky, owner, DogTrick.ROLL_OVER),
        "A hitched dog refuses tricks from the wheel");
    SurvivalTestPlayer.leave(context, owner);
    context.complete();
  }

  @GameTest(templateName = RANGE, tickLimit = 20)
  public void hitchingADogMidTrickStopsIt(final TestContext context) {
    final ServerPlayerEntity owner = SurvivalTestPlayer.joinAt(context, REL_SLED_DRIVER);
    final SledEntity sled = sled(context);
    final UnleashedDogEntity husky = leashedHusky(context, owner);
    performance(husky).perform(DogTrick.PLAY_DEAD);

    sled.interact(owner, Hand.MAIN_HAND);

    context.runAtTick(
        3,
        () -> {
          context.assertFalse(performance(husky).isPerforming(), "Hitching ends the trick");
          SurvivalTestPlayer.leave(context, owner);
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void learnedTricksComeBackFromTheGrave(final TestContext context) {
    final ServerPlayerEntity owner = ownerAt(context);
    final UnleashedDogEntity dog = dog(context, owner);
    final TrickProgress progress = TrickProgress.of(Map.of(DogTrick.ROLL_OVER, 5));
    tricks(dog).restore(progress);
    registerPet(context, dog, owner);
    final UUID dogId = dog.getUuid();

    dog.kill();

    context.runAtTick(
        5,
        () -> {
          final List<BlockPos> graves = new ArrayList<>();
          context.forEachRelativePos(
              pos -> {
                if (context.getBlockState(pos).isOf(ModBlocks.DOG_GRAVE)) {
                  graves.add(context.getAbsolutePos(pos));
                }
              });
          context.assertTrue(graves.size() == 1, "A dead pet leaves a grave");
          final BlockPos gravePos = graves.get(0);
          final DogGraveBlockEntity grave =
              (DogGraveBlockEntity) context.getWorld().getBlockEntity(gravePos);
          grave.installTotem(UUID.randomUUID());

          context.assertTrue(
              DogResurrection.resurrect(context.getWorld(), gravePos, grave), "The pet rises");
          final UnleashedDogEntity risen = (UnleashedDogEntity) context.getWorld().getEntity(dogId);
          context.assertEquals(progress, tricks(risen).progress(), "It still knows its tricks");
          context.complete();
        });
  }

  private static void registerPet(
      final TestContext context, final UnleashedDogEntity dog, final ServerPlayerEntity owner) {
    PetManager.get(context.getWorld().getServer()).registerPet(petOf(context, dog, owner));
  }

  private static PetData petOf(
      final TestContext context, final UnleashedDogEntity dog, final ServerPlayerEntity owner) {
    final PetData pet =
        new PetData(
            dog.getUuid(),
            owner.getUuid(),
            dog.getBreed(),
            "Biscuit",
            dog.getHealth(),
            dog.getMaxHealth(),
            dog.getBlockPos(),
            context.getWorld().getRegistryKey().getValue().toString(),
            PetLifeState.LIVING);
    pet.syncFrom(dog);
    return pet;
  }

  private static String wheelRefusal(
      final UnleashedDogEntity dog, final ServerPlayerEntity owner, final DogTrick trick) {
    return DogWheelSubActions.REGISTERED.statesFor(dog, owner).stream()
        .filter(state -> state.id() == trick.wheelActionId())
        .findFirst()
        .orElseThrow()
        .refusalKey();
  }

  private static boolean onWheel(
      final UnleashedDogEntity dog, final ServerPlayerEntity owner, final DogTrick trick) {
    return DogWheelSubActions.REGISTERED.statesFor(dog, owner).stream()
        .map(WheelSubActionState::id)
        .anyMatch(id -> id == trick.wheelActionId());
  }

  private static void feedTreat(final UnleashedDogEntity dog, final ServerPlayerEntity owner) {
    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModItems.DOG_TREAT));
    dog.interactMob(owner, Hand.MAIN_HAND);
  }

  private static boolean advancementDone(
      final TestContext context, final ServerPlayerEntity player, final String id) {
    final AdvancementEntry advancement =
        context
            .getWorld()
            .getServer()
            .getAdvancementLoader()
            .get(Identifier.of(DogsUnleashed.MOD_ID, id));
    return player.getAdvancementTracker().getProgress(advancement).isDone();
  }

  private static ServerPlayerEntity ownerAt(final TestContext context) {
    final ServerPlayerEntity owner = CreativeTestPlayer.join(context);
    owner.refreshPositionAndAngles(context.getAbsolute(REL_OWNER), 0.0f, 0.0f);
    return owner;
  }

  private static UnleashedDogEntity dog(final TestContext context, final ServerPlayerEntity owner) {
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.BEAGLE, REL_DOG);
    dog.setAiDisabled(true);
    dog.setTamed(true, true);
    dog.setOwnerUuid(owner.getUuid());
    dog.setInSittingPose(true);
    return dog;
  }

  private static UnleashedDogEntity standingDog(
      final TestContext context, final ServerPlayerEntity owner) {
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.BEAGLE, REL_DOG);
    dog.setTamed(true, true);
    dog.setOwnerUuid(owner.getUuid());
    dog.getCommandController().apply(DogCommand.FREE_ROAM);
    return dog;
  }

  private static void walkToFarCorner(final TestContext context, final UnleashedDogEntity dog) {
    final BlockPos corner = context.getAbsolutePos(REL_FAR_CORNER);
    dog.getNavigation().startMovingTo(corner.getX() + 0.5, corner.getY(), corner.getZ() + 0.5, 1.0);
  }

  private static SledEntity sled(final TestContext context) {
    final SledEntity sled = context.spawnEntity(ModEntities.SLED, REL_SLED);
    sled.placeFacing(-90.0f);
    return sled;
  }

  private static UnleashedDogEntity leashedHusky(
      final TestContext context, final ServerPlayerEntity owner) {
    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, REL_TEAM_DOG);
    husky.setOwner(owner);
    husky.getCommandController().apply(DogCommand.FOLLOW);
    husky.attachLeash(owner, true);
    return husky;
  }

  private static DogTrickPerformance performance(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogTrickPerformance.class);
  }

  private static DogTricks tricks(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogTricks.class);
  }
}
