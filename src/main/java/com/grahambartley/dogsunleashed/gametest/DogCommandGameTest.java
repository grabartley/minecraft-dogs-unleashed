package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public final class DogCommandGameTest implements FabricGameTest {

  private static final String BATCH = "dog-command";

  /**
   * A free-roaming dog wanders, so a single distance reading at the end of the window is decided as
   * much by which way it happened to amble as by whether it was following. What separates the two
   * is persistence: a following dog closes on its owner and parks there, while a wanderer only
   * drifts past. Counting the ticks spent near the owner measures that, and does not flip when the
   * shared world's random stream shifts under an unrelated change.
   */
  private static final double FREE_ROAM_NEAR_OWNER_DISTANCE = 6.0;

  private static final int FREE_ROAM_NEAR_OWNER_TICK_ALLOWANCE = 25;
  private static final int TARGET_OBSERVATION_TICK = 100;
  private static final int TARGET_TICK_LIMIT = 120;

  private static UnleashedDogEntity spawnOwnedDog(
      final TestContext context, final BlockPos relativePos) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    return DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, relativePos, owner.getUuid());
  }

  private static ServerPlayerEntity placeOwnerAt(
      final TestContext context, final UnleashedDogEntity dog, final BlockPos relativePos) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    dog.setOwnerUuid(owner.getUuid());
    final BlockPos absPos = context.getAbsolutePos(relativePos);
    owner.refreshPositionAndAngles(absPos.getX() + 0.5, absPos.getY(), absPos.getZ() + 0.5, 0f, 0f);
    return owner;
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void applyCommandSitSetsSittingPose(final TestContext context) {
    final UnleashedDogEntity dog = spawnOwnedDog(context, new BlockPos(0, 1, 0));
    dog.setAiDisabled(true);

    dog.getCommandController().apply(DogCommand.SIT);

    context.assertTrue(dog.getCommand() == DogCommand.SIT, "Command should be SIT");
    context.assertTrue(dog.isSitting(), "Sit command should set the sitting flag");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void applyCommandStayAnchorsAtCurrentPosition(final TestContext context) {
    final UnleashedDogEntity dog = spawnOwnedDog(context, new BlockPos(0, 1, 0));
    dog.setAiDisabled(true);

    dog.getCommandController().apply(DogCommand.STAY);

    context.assertTrue(dog.getCommand() == DogCommand.STAY, "Command should be STAY");
    context.assertTrue(
        dog.getBlockPos().equals(dog.getCommandController().getAnchorPos()),
        "Stay should anchor at the dog's position, anchor="
            + dog.getCommandController().getAnchorPos());
    context.assertFalse(dog.isSitting(), "Stay must not sit the dog");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void applyCommandFollowClearsAnchorAndSitting(final TestContext context) {
    final UnleashedDogEntity dog = spawnOwnedDog(context, new BlockPos(0, 1, 0));
    dog.setAiDisabled(true);

    dog.getCommandController().apply(DogCommand.STAY);
    dog.getCommandController().apply(DogCommand.SIT);
    dog.getCommandController().apply(DogCommand.FOLLOW);

    context.assertTrue(dog.getCommand() == DogCommand.FOLLOW, "Command should be FOLLOW");
    context.assertTrue(
        dog.getCommandController().getAnchorPos() == null, "Follow should clear the anchor");
    context.assertFalse(dog.isSitting(), "Follow should clear the sitting flag");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void applyCommandWakesSleepingDog(final TestContext context) {
    final BlockPos relPos = new BlockPos(0, 1, 0);
    final UnleashedDogEntity dog = spawnOwnedDog(context, relPos);
    dog.setAiDisabled(true);
    final BlockPos absPos = context.getAbsolutePos(relPos);
    dog.setAssignedBedPos(absPos);
    dog.getSleepController().startSleepingInBed(absPos);

    dog.getCommandController().apply(DogCommand.HEEL);

    context.assertFalse(dog.isSleepingInBed(), "Issuing a command should wake a sleeping dog");
    context.assertFalse(dog.isCommandedToSleep(), "Issuing a command should clear commanded sleep");
    context.assertTrue(dog.getCommand() == DogCommand.HEEL, "Command should be HEEL");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void damageDemotesSitToFollow(final TestContext context) {
    final UnleashedDogEntity dog = spawnOwnedDog(context, new BlockPos(0, 1, 0));
    dog.setAiDisabled(true);
    dog.getCommandController().apply(DogCommand.SIT);

    DogTestHelper.damageEntity(dog, 1.0f);

    context.assertTrue(
        dog.getCommand() == DogCommand.FOLLOW, "Damage should demote a sitting dog to FOLLOW");
    context.assertFalse(dog.isSitting(), "Damage should clear the sitting flag");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void commandAndAnchorPersistThroughNbtRoundTrip(final TestContext context) {
    final UnleashedDogEntity source = spawnOwnedDog(context, new BlockPos(0, 1, 0));
    source.setAiDisabled(true);
    source.getCommandController().apply(DogCommand.GUARD);
    final BlockPos anchor = source.getCommandController().getAnchorPos();

    final NbtCompound nbt = new NbtCompound();
    source.writeCustomDataToNbt(nbt);
    final UnleashedDogEntity restored = DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY);
    restored.setAiDisabled(true);
    restored.readCustomDataFromNbt(nbt);

    context.assertTrue(
        restored.getCommand() == DogCommand.GUARD, "Command should survive an NBT round trip");
    context.assertTrue(
        anchor != null && anchor.equals(restored.getCommandController().getAnchorPos()),
        "Anchor should survive an NBT round trip, anchor="
            + restored.getCommandController().getAnchorPos());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void legacySittingDogWithoutCommandNbtMapsToSit(final TestContext context) {
    final UnleashedDogEntity source = spawnOwnedDog(context, new BlockPos(0, 1, 0));
    source.setAiDisabled(true);
    source.setSitting(true);
    final NbtCompound nbt = new NbtCompound();
    source.writeCustomDataToNbt(nbt);
    nbt.remove(ModNbtKeys.COMMAND_MODE);

    final UnleashedDogEntity restored = DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY);
    restored.setAiDisabled(true);
    restored.readCustomDataFromNbt(nbt);

    context.assertTrue(
        restored.getCommand() == DogCommand.SIT,
        "A pre-command sitting dog should load as SIT, was " + restored.getCommand());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void legacyStandingDogWithoutCommandNbtMapsToFollow(final TestContext context) {
    final UnleashedDogEntity source = spawnOwnedDog(context, new BlockPos(0, 1, 0));
    source.setAiDisabled(true);
    final NbtCompound nbt = new NbtCompound();
    source.writeCustomDataToNbt(nbt);
    nbt.remove(ModNbtKeys.COMMAND_MODE);

    final UnleashedDogEntity restored = DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY);
    restored.setAiDisabled(true);
    restored.readCustomDataFromNbt(nbt);

    context.assertTrue(
        restored.getCommand() == DogCommand.FOLLOW,
        "A pre-command standing dog should load as FOLLOW, was " + restored.getCommand());
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void ownerRightClickNoLongerTogglesSitting(final TestContext context) {
    final UnleashedDogEntity dog = DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY);
    dog.setAiDisabled(true);
    final PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
    dog.setOwnerUuid(player.getUuid());

    dog.interactMob(player, Hand.MAIN_HAND);

    context.assertFalse(dog.isSitting(), "Right-click must no longer toggle sitting");
    context.assertTrue(
        dog.getCommand() == DogCommand.FOLLOW,
        "Right-click must not change the command, was " + dog.getCommand());
    context.complete();
  }

  @GameTest(
      templateName = "dogs-unleashed:teleport_arena",
      batchId = BATCH,
      tickLimit = 120,
      maxAttempts = 3,
      requiredSuccesses = 1)
  public void stayDogHoldsPositionWhenOwnerIsFar(final TestContext context) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, new BlockPos(2, 2, 2));
    placeOwnerAt(context, dog, new BlockPos(20, 2, 2));
    dog.getCommandController().apply(DogCommand.STAY);
    final BlockPos anchor = dog.getCommandController().getAnchorPos();

    context.runAtTick(
        100,
        () -> {
          final double distance = Math.sqrt(dog.getBlockPos().getSquaredDistance(anchor));
          context.assertTrue(
              distance <= 9.0,
              "A staying dog must remain near its anchor even with a distant owner, distance="
                  + distance);
          context.complete();
        });
  }

  @GameTest(
      templateName = "dogs-unleashed:teleport_arena",
      batchId = BATCH,
      tickLimit = 160,
      maxAttempts = 3,
      requiredSuccesses = 1)
  public void heelDogClosesToWithinHeelDistance(final TestContext context) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, new BlockPos(2, 2, 2));
    final ServerPlayerEntity owner = placeOwnerAt(context, dog, new BlockPos(10, 2, 2));
    dog.getCommandController().apply(DogCommand.HEEL);

    context.runAtTick(
        140,
        () -> {
          final double distance = dog.distanceTo(owner);
          context.assertTrue(
              distance <= 4.0,
              "A heeling dog should close to within heel distance of its owner, distance="
                  + distance);
          context.complete();
        });
  }

  @GameTest(
      templateName = "dogs-unleashed:teleport_arena",
      batchId = BATCH,
      tickLimit = 120,
      maxAttempts = 3,
      requiredSuccesses = 1)
  public void freeRoamDogDoesNotFollowOwner(final TestContext context) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, new BlockPos(2, 2, 2));
    final ServerPlayerEntity owner = placeOwnerAt(context, dog, new BlockPos(20, 2, 2));
    dog.getCommandController().apply(DogCommand.FREE_ROAM);

    final int[] ticksNearOwner = {0};
    context.runAtEveryTick(
        () -> {
          if (dog.distanceTo(owner) < FREE_ROAM_NEAR_OWNER_DISTANCE) {
            ticksNearOwner[0]++;
          }
        });

    context.runAtTick(
        100,
        () -> {
          context.assertTrue(
              ticksNearOwner[0] <= FREE_ROAM_NEAR_OWNER_TICK_ALLOWANCE,
              "A free-roaming dog must not seek out its owner, but it spent "
                  + ticksNearOwner[0]
                  + " of 100 ticks within "
                  + FREE_ROAM_NEAR_OWNER_DISTANCE
                  + " blocks of them");
          context.complete();
        });
  }

  @GameTest(
      templateName = "dogs-unleashed:dog_arena",
      batchId = BATCH,
      tickLimit = TARGET_TICK_LIMIT,
      maxAttempts = 3,
      requiredSuccesses = 1)
  public void huntDogTargetsNearbyHostile(final TestContext context) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, new BlockPos(1, 2, 1));
    placeOwnerAt(context, dog, new BlockPos(1, 2, 3));
    final HuskEntity husk = context.spawnEntity(EntityType.HUSK, new BlockPos(5, 2, 5));
    husk.setAiDisabled(true);
    dog.getCommandController().apply(DogCommand.HUNT);

    final LivingEntity[] firstTarget = new LivingEntity[1];
    context.runAtEveryTick(
        () -> {
          if (firstTarget[0] == null && dog.getTarget() != null) {
            firstTarget[0] = dog.getTarget();
          }
        });
    context.runAtTick(
        TARGET_OBSERVATION_TICK,
        () -> {
          context.assertTrue(
              firstTarget[0] == husk,
              "A hunting dog should target a nearby hostile, target=" + firstTarget[0]);
          context.complete();
        });
  }

  @GameTest(
      templateName = "dogs-unleashed:dog_arena",
      batchId = BATCH,
      tickLimit = TARGET_TICK_LIMIT,
      maxAttempts = 3,
      requiredSuccesses = 1)
  public void huntDogTargetsUnnamedAnimal(final TestContext context) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, new BlockPos(1, 2, 1));
    placeOwnerAt(context, dog, new BlockPos(1, 2, 3));
    final CowEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(5, 2, 5));
    cow.setAiDisabled(true);
    dog.getCommandController().apply(DogCommand.HUNT);

    final LivingEntity[] firstTarget = new LivingEntity[1];
    context.runAtEveryTick(
        () -> {
          if (firstTarget[0] == null && dog.getTarget() != null) {
            firstTarget[0] = dog.getTarget();
          }
        });
    context.runAtTick(
        TARGET_OBSERVATION_TICK,
        () -> {
          context.assertTrue(
              firstTarget[0] == cow,
              "A hunting dog should target an unnamed animal, target=" + firstTarget[0]);
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = BATCH, tickLimit = 100)
  public void huntDogIgnoresProtectedTargets(final TestContext context) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, new BlockPos(1, 2, 1));
    placeOwnerAt(context, dog, new BlockPos(1, 2, 3));
    final CowEntity namedCow = context.spawnEntity(EntityType.COW, new BlockPos(5, 2, 1));
    namedCow.setAiDisabled(true);
    namedCow.setCustomName(Text.literal("Bessie"));
    final VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, new BlockPos(5, 2, 5));
    villager.setAiDisabled(true);
    final UnleashedDogEntity otherDog =
        DogTestHelper.spawnDog(context, DogTestData.HUSKY, new BlockPos(1, 2, 5));
    otherDog.setAiDisabled(true);
    dog.getCommandController().apply(DogCommand.HUNT);

    final LivingEntity[] firstTarget = new LivingEntity[1];
    context.runAtEveryTick(
        () -> {
          if (firstTarget[0] == null && dog.getTarget() != null) {
            firstTarget[0] = dog.getTarget();
          }
        });
    context.runAtTick(
        80,
        () -> {
          context.assertTrue(
              firstTarget[0] == null,
              "A hunting dog must never target named animals, villagers, or dogs, target="
                  + firstTarget[0]);
          context.complete();
        });
  }

  @GameTest(
      templateName = "dogs-unleashed:dog_arena",
      batchId = BATCH,
      tickLimit = TARGET_TICK_LIMIT,
      maxAttempts = 3,
      requiredSuccesses = 1)
  public void guardDogTargetsHostileNearAnchor(final TestContext context) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, new BlockPos(1, 2, 1));
    placeOwnerAt(context, dog, new BlockPos(1, 2, 3));
    dog.getCommandController().apply(DogCommand.GUARD);
    final HuskEntity husk = context.spawnEntity(EntityType.HUSK, new BlockPos(5, 2, 5));
    husk.setAiDisabled(true);

    final LivingEntity[] firstTarget = new LivingEntity[1];
    context.runAtEveryTick(
        () -> {
          if (firstTarget[0] == null && dog.getTarget() != null) {
            firstTarget[0] = dog.getTarget();
          }
        });
    context.runAtTick(
        TARGET_OBSERVATION_TICK,
        () -> {
          context.assertTrue(
              firstTarget[0] == husk,
              "A guarding dog should target a hostile near its anchor, target=" + firstTarget[0]);
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:teleport_arena", batchId = BATCH, tickLimit = 100)
  public void guardDogIgnoresHostileFarFromAnchor(final TestContext context) {
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, new BlockPos(2, 2, 2));
    placeOwnerAt(context, dog, new BlockPos(4, 2, 2));
    dog.getCommandController().apply(DogCommand.GUARD);
    final HuskEntity husk = context.spawnEntity(EntityType.HUSK, new BlockPos(20, 2, 2));
    husk.setAiDisabled(true);

    final LivingEntity[] firstTarget = new LivingEntity[1];
    context.runAtEveryTick(
        () -> {
          if (firstTarget[0] == null && dog.getTarget() != null) {
            firstTarget[0] = dog.getTarget();
          }
        });
    context.runAtTick(
        80,
        () -> {
          context.assertTrue(
              firstTarget[0] == null,
              "A guarding dog must ignore hostiles far from its anchor, target=" + firstTarget[0]);
          context.complete();
        });
  }
}
