package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.DogBoneBurial;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.burial.BoneCache;
import com.grahambartley.dogsunleashed.entity.burial.BoneCacheRules;
import com.grahambartley.dogsunleashed.entity.burial.BoneCaches;
import com.grahambartley.dogsunleashed.entity.fetch.FetchTypes;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public final class DogBoneBurialGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String BATCH = "dog-bone-burial";
  private static final BlockPos OWNER_SPOT = new BlockPos(1, 2, 3);
  private static final BlockPos DOG_START = new BlockPos(1, 2, 1);
  private static final BlockPos DIG_GROUND = new BlockPos(5, 1, 5);
  private static final int ARENA_SIZE = 7;
  private static final int FLOOR_Y = 1;
  private static final long LEFT_ALONE_TICKS = 250;

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aFreeRoamingDogTakesTheBone(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner, DogCommand.FREE_ROAM);
    dog.setAiDisabled(true);

    giveBone(dog, owner);

    context.assertTrue(burial(dog).isCarryingBone(), "A free-roaming dog should take the bone");
    context.assertTrue(burial(dog).isBuryPending(), "The dog should mean to bury the bone");
    context.assertTrue(owner.getMainHandStack().isEmpty(), "The bone should leave the hand");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aFollowingDogDoesNotTakeTheBone(TestContext context) {
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner, DogCommand.FOLLOW);
    dog.setAiDisabled(true);

    giveBone(dog, owner);

    context.assertFalse(burial(dog).isCarryingBone(), "Only roaming or staying dogs bury bones");
    context.assertTrue(owner.getMainHandStack().isOf(Items.BONE), "The bone should stay in hand");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void theBoneIsNeverDuplicatedOrLostAcrossBuryAndDigUp(TestContext context) {
    dirtFloor(context);
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner, DogCommand.STAY);
    dog.setAiDisabled(true);
    final ServerWorld world = context.getWorld();
    final BlockPos ground = context.getAbsolutePos(DIG_GROUND);

    owner.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BONE));
    assertOneBone(context, owner, dog, "before giving");
    dog.interactMob(owner, Hand.MAIN_HAND);
    assertOneBone(context, owner, dog, "after giving");
    context.assertTrue(burial(dog).bury(world, ground), "The dog should bury the bone");
    assertOneBone(context, owner, dog, "after burying");
    context.assertFalse(burial(dog).bury(world, ground), "A buried bone can't be buried again");
    context.assertTrue(burial(dog).digUp(world, ground), "The dog should dig the bone up");
    assertOneBone(context, owner, dog, "after digging up");
    context.assertFalse(burial(dog).digUp(world, ground), "An empty cache can't be dug again");
    assertOneBone(context, owner, dog, "after digging an empty cache");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 400)
  public void aStayingDogBuriesTheBoneNearby(TestContext context) {
    dirtFloor(context);
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner, DogCommand.STAY);

    giveBone(dog, owner);

    context.runAtEveryTick(
        () -> {
          final boolean buried = !caches(context).buriedBy(dog.getUuid()).isEmpty();
          if (buried && !burial(dog).isCarryingBone()) {
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 400)
  public void aDogDigsUpADueCacheAndCarriesTheBone(TestContext context) {
    dirtFloor(context);
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner, DogCommand.FREE_ROAM);
    final BlockPos ground = context.getAbsolutePos(DIG_GROUND);
    caches(context)
        .add(new BoneCache(ground, owner.getUuid(), dog.getUuid(), dueBuriedAt(context)));

    context.runAtEveryTick(
        () -> {
          if (caches(context).at(ground).isEmpty() && burial(dog).isCarryingBone()) {
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 300)
  public void aCacheNotYetDueStaysBuried(TestContext context) {
    dirtFloor(context);
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner, DogCommand.FREE_ROAM);
    final BlockPos ground = context.getAbsolutePos(DIG_GROUND);
    caches(context)
        .add(
            new BoneCache(
                ground, owner.getUuid(), dog.getUuid(), context.getWorld().getTimeOfDay()));

    assertLeftAlone(context, dog, ground, "A fresh cache");
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 300)
  public void aDogPlayingFetchLeavesADueCacheAlone(TestContext context) {
    dirtFloor(context);
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner, DogCommand.FREE_ROAM);
    final BlockPos ground = context.getAbsolutePos(DIG_GROUND);
    caches(context)
        .add(new BoneCache(ground, owner.getUuid(), dog.getUuid(), dueBuriedAt(context)));
    dog.getPlaySession().startPlayMode(owner, FetchTypes.TENNIS_BALL);

    assertLeftAlone(context, dog, ground, "A cache during fetch");
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 300)
  public void aDogCalledAwayBringsTheBoneBack(TestContext context) {
    dirtFloor(context);
    final ServerPlayerEntity owner = placeOwner(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context, owner, DogCommand.STAY);

    giveBone(dog, owner);
    dog.getCommandController().apply(DogCommand.FOLLOW);

    context.runAtEveryTick(
        () -> {
          assertOneBone(context, owner, dog, "while called away");
          if (!burial(dog).isCarryingBone() && caches(context).buriedBy(dog.getUuid()).isEmpty()) {
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aCacheWithNoDogCanBeDugUpByHand(TestContext context) {
    dirtFloor(context);
    final ServerPlayerEntity player = placeOwner(context);
    final BlockPos ground = context.getAbsolutePos(DIG_GROUND);
    caches(context).add(new BoneCache(ground, player.getUuid(), UUID.randomUUID(), 0L));
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_SHOVEL));

    player.interactionManager.tryBreakBlock(ground);

    context.assertTrue(caches(context).at(ground).isEmpty(), "Digging should empty the cache");
    context.assertEquals(loose(context), 1, "Bones turned up by digging the cache");
    context.complete();
  }

  private static void assertLeftAlone(
      final TestContext context,
      final UnleashedDogEntity dog,
      final BlockPos ground,
      final String what) {
    context.runAtEveryTick(
        () -> {
          context.assertFalse(burial(dog).isDigging(), what + " should not be dug at");
          context.assertFalse(burial(dog).isCarryingBone(), what + " should stay buried");
        });
    context.runAtTick(
        LEFT_ALONE_TICKS,
        () -> {
          context.assertTrue(caches(context).at(ground).isPresent(), what + " stays put");
          context.complete();
        });
  }

  private static void assertOneBone(
      final TestContext context,
      final ServerPlayerEntity owner,
      final UnleashedDogEntity dog,
      final String when) {
    final int inHand = owner.getInventory().count(Items.BONE);
    final int inMouth = burial(dog).isCarryingBone() ? 1 : 0;
    final int buried = caches(context).buriedBy(dog.getUuid()).size();
    context.assertEquals(inHand + inMouth + buried + loose(context), 1, "Bones " + when);
  }

  private static int loose(final TestContext context) {
    return context
        .getWorld()
        .getEntitiesByClass(
            ItemEntity.class,
            context.getTestBox().expand(2),
            item -> item.getStack().isOf(Items.BONE))
        .stream()
        .mapToInt(item -> item.getStack().getCount())
        .sum();
  }

  private static long dueBuriedAt(final TestContext context) {
    return context.getWorld().getTimeOfDay()
        - BoneCacheRules.DAYS_UNTIL_DIG_UP * BoneCacheRules.TICKS_PER_DAY;
  }

  private static void dirtFloor(final TestContext context) {
    for (int x = 0; x < ARENA_SIZE; x++) {
      for (int z = 0; z < ARENA_SIZE; z++) {
        context.setBlockState(new BlockPos(x, FLOOR_Y, z), Blocks.DIRT);
      }
    }
  }

  private static void giveBone(final UnleashedDogEntity dog, final ServerPlayerEntity player) {
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BONE));
    dog.interactMob(player, Hand.MAIN_HAND);
  }

  private static ServerPlayerEntity placeOwner(final TestContext context) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    player.changeGameMode(GameMode.SURVIVAL);
    final BlockPos absolute = context.getAbsolutePos(OWNER_SPOT);
    player.requestTeleport(absolute.getX() + 0.5, absolute.getY(), absolute.getZ() + 0.5);
    return player;
  }

  private static UnleashedDogEntity spawnOwnedDog(
      final TestContext context, final ServerPlayerEntity owner, final DogCommand command) {
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.HUSKY, DOG_START);
    dog.setTamed(true, true);
    dog.setOwnerUuid(owner.getUuid());
    dog.getCommandController().apply(command);
    return dog;
  }

  private static BoneCaches caches(final TestContext context) {
    return BoneCaches.get(context.getWorld());
  }

  private static DogBoneBurial burial(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogBoneBurial.class);
  }
}
