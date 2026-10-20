package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.burial.BoneCache;
import com.grahambartley.dogsunleashed.entity.burial.BoneCacheRules;
import com.grahambartley.dogsunleashed.entity.burial.BoneCaches;
import com.grahambartley.dogsunleashed.entity.burial.BoneThieves;
import com.grahambartley.dogsunleashed.entity.goal.ChaseBoneThiefGoal;
import com.grahambartley.dogsunleashed.entity.goal.FoxRaidCacheGoal;
import com.grahambartley.dogsunleashed.gametest.util.CreativeTestPlayer;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

public final class FoxBoneRaidGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String NIGHT_BATCH = "fox-bone-raid-night";
  private static final String DAY_BATCH = "fox-bone-raid-day";
  private static final long MIDNIGHT = 18000L;
  private static final long MIDDAY = 6000L;
  private static final int SETTLE_TICKS = 5;
  private static final BlockPos OWNER_SPOT = new BlockPos(1, 2, 3);
  private static final BlockPos DOG_START = new BlockPos(1, 2, 1);
  private static final BlockPos FOX_START = new BlockPos(5, 2, 1);
  private static final BlockPos CACHE_GROUND = new BlockPos(5, 1, 5);
  private static final BlockPos BESIDE_CACHE = new BlockPos(5, 2, 4);
  private static final int ARENA_SIZE = 7;
  private static final int FLOOR_Y = 1;

  @BeforeBatch(batchId = NIGHT_BATCH)
  public void freezeMidnight(final ServerWorld world) {
    freezeAt(world, MIDNIGHT);
  }

  @AfterBatch(batchId = NIGHT_BATCH)
  public void restoreDaylightAfterNight(final ServerWorld world) {
    restoreDaylight(world);
  }

  @BeforeBatch(batchId = DAY_BATCH)
  public void freezeMidday(final ServerWorld world) {
    freezeAt(world, MIDDAY);
  }

  @AfterBatch(batchId = DAY_BATCH)
  public void restoreDaylightAfterDay(final ServerWorld world) {
    restoreDaylight(world);
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 20)
  public void aWildFoxPicksANearbyCacheAtNight(final TestContext context) {
    final BlockPos ground = buryCache(context);
    final FoxEntity fox = spawnFox(context);

    context.runAtTick(
        SETTLE_TICKS,
        () -> {
          context.assertEquals(
              BoneThieves.pickCache(context.getWorld(), fox),
              Optional.of(ground),
              "A wild fox should go for the cache at night");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 20)
  public void aTrustingFoxLeavesCachesAlone(final TestContext context) {
    buryCache(context);
    final FoxEntity fox = spawnFox(context);
    trust(fox, UUID.randomUUID());

    context.runAtTick(
        SETTLE_TICKS,
        () -> {
          context.assertTrue(
              BoneThieves.pickCache(context.getWorld(), fox).isEmpty(),
              "A fox that trusts a player is not wild and should not raid");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = DAY_BATCH, tickLimit = 20)
  public void foxesLeaveCachesAloneByDay(final TestContext context) {
    buryCache(context);
    final FoxEntity fox = spawnFox(context);

    context.runAtTick(
        SETTLE_TICKS,
        () -> {
          context.assertTrue(
              BoneThieves.pickCache(context.getWorld(), fox).isEmpty(), "Foxes only raid at night");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 20)
  public void aDogChasesAFoxGoingForACache(final TestContext context) {
    final BlockPos ground = buryCache(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context);
    final FoxEntity fox = spawnFox(context);
    BoneThieves.setTarget(fox, ground);
    final ChaseBoneThiefGoal chase = new ChaseBoneThiefGoal(dog);

    context.assertTrue(chase.canStart(), "A nearby dog should start chasing the fox");
    context.assertTrue(chase.thief() == fox, "The dog should chase the raiding fox");
    chase.start();
    context.assertTrue(chase.shouldContinue(), "The chase should go on once it starts");
    context.assertTrue(dog.getTarget() == null, "The dog chases the fox off, never attacks it");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 20)
  public void aDogIgnoresAFoxThatIsNotRaiding(final TestContext context) {
    buryCache(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context);
    spawnFox(context);

    context.assertFalse(
        new ChaseBoneThiefGoal(dog).canStart(), "A dog should leave an innocent fox alone");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 20)
  public void aDogChasesAFoxRunningOffWithTheBone(final TestContext context) {
    final BlockPos ground = buryCache(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context);
    final FoxEntity fox = spawnFox(context);
    BoneThieves.steal(context.getWorld(), fox, ground);

    final ChaseBoneThiefGoal chase = new ChaseBoneThiefGoal(dog);
    context.assertTrue(chase.canStart(), "A dog should chase a fox carrying a stolen bone");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 20)
  public void theStolenBoneEndsUpInExactlyOnePlace(final TestContext context) {
    final BlockPos ground = buryCache(context);
    final FoxEntity fox = spawnFox(context);
    final ServerWorld world = context.getWorld();

    assertOneBone(context, fox, ground, "while buried");
    context.assertTrue(BoneThieves.steal(world, fox, ground), "The fox should dig up the bone");
    context.assertTrue(
        fox.getEquippedStack(EquipmentSlot.MAINHAND).isOf(Items.BONE),
        "The fox should hold the bone in its mouth");
    assertOneBone(context, fox, ground, "after the theft");
    context.assertFalse(BoneThieves.steal(world, fox, ground), "An empty cache can't be robbed");
    assertOneBone(context, fox, ground, "after robbing an empty cache");
    fox.damage(world.getDamageSources().generic(), 1.0F);
    context.assertTrue(
        fox.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty(), "A hurt fox should drop the bone");
    assertOneBone(context, fox, ground, "after the fox is hurt");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 20)
  public void aFoxWithSomethingInItsMouthCannotSteal(final TestContext context) {
    final BlockPos ground = buryCache(context);
    final FoxEntity fox = spawnFox(context);
    fox.equipStack(EquipmentSlot.MAINHAND, Items.FEATHER.getDefaultStack());

    context.assertFalse(
        BoneThieves.steal(context.getWorld(), fox, ground), "A full mouth can't take the bone");
    context.assertTrue(
        BoneCaches.get(context.getWorld()).at(ground).isPresent(), "The bone should stay buried");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 20)
  public void everyFoxGetsOneRaidGoal(final TestContext context) {
    final FoxEntity fox = spawnFox(context);

    context.assertEquals(
        fox.goalSelector.getGoals().stream()
            .filter(goal -> goal.getGoal() instanceof FoxRaidCacheGoal)
            .count(),
        1L,
        "Each fox should carry exactly one raid goal");
    context.assertEquals(
        fox.goalSelector.getGoals().stream()
            .filter(goal -> goal.getGoal() instanceof FleeEntityGoal<?>)
            .count(),
        4L,
        "A fox should flee players, wolves, polar bears and dogs, once each");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 40)
  public void aRaidingFoxDigsUpTheBoneAndRunsOff(final TestContext context) {
    final BlockPos ground = buryCache(context);
    final FoxEntity fox = context.spawnEntity(EntityType.FOX, BESIDE_CACHE);
    fox.setAiDisabled(true);

    context.runAtTick(
        SETTLE_TICKS,
        () -> {
          final FoxRaidCacheGoal raid = new FoxRaidCacheGoal(fox, () -> true);
          fox.setOnGround(true);
          context.assertEquals(
              BoneThieves.pickCache(context.getWorld(), fox),
              Optional.of(ground),
              "The fox should pick the cache beside it");
          context.assertTrue(raid.canStart(), "A wild fox beside a cache at night should raid it");
          raid.start();
          context.assertEquals(
              BoneThieves.target(fox), Optional.of(ground), "The fox should mark its target");
          for (int tick = 0; tick <= BoneCacheRules.DIG_TICKS && raid.shouldContinue(); tick++) {
            raid.tick();
          }
          context.assertTrue(
              fox.getEquippedStack(EquipmentSlot.MAINHAND).isOf(Items.BONE),
              "The fox should end up with the bone in its mouth");
          assertOneBone(context, fox, ground, "after the raid");
          context.assertFalse(fox.isInSneakingPose(), "The fox should stand up to run off");
          raid.stop();
          context.assertTrue(BoneThieves.target(fox).isEmpty(), "The target should clear");
          context.assertTrue(BoneThieves.isThief(fox), "The fox still carries a stolen bone");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 20)
  public void aDogAtHeelIgnoresTheThief(final TestContext context) {
    final BlockPos ground = buryCache(context);
    final UnleashedDogEntity dog = spawnOwnedDog(context);
    dog.getCommandController().apply(DogCommand.HEEL);
    final FoxEntity fox = spawnFox(context);
    BoneThieves.setTarget(fox, ground);

    context.assertFalse(
        new ChaseBoneThiefGoal(dog).canStart(), "A dog at heel should keep to its orders");
    context.complete();
  }

  private static void freezeAt(final ServerWorld world, final long time) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, world.getServer());
    world.setTimeOfDay(time);
  }

  private static void restoreDaylight(final ServerWorld world) {
    world.setTimeOfDay(MIDDAY);
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, world.getServer());
  }

  private static void assertOneBone(
      final TestContext context, final FoxEntity fox, final BlockPos ground, final String when) {
    final int buried = BoneCaches.get(context.getWorld()).at(ground).isPresent() ? 1 : 0;
    final int inMouth = fox.getEquippedStack(EquipmentSlot.MAINHAND).isOf(Items.BONE) ? 1 : 0;
    context.assertEquals(buried + inMouth + loose(context), 1, "Bones " + when);
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

  private static BlockPos buryCache(final TestContext context) {
    for (int x = 0; x < ARENA_SIZE; x++) {
      for (int z = 0; z < ARENA_SIZE; z++) {
        context.setBlockState(new BlockPos(x, FLOOR_Y, z), Blocks.DIRT);
      }
    }
    final BlockPos ground = context.getAbsolutePos(CACHE_GROUND);
    BoneCaches.get(context.getWorld())
        .add(
            new BoneCache(
                ground, UUID.randomUUID(), UUID.randomUUID(), context.getWorld().getTimeOfDay()));
    return ground;
  }

  private static FoxEntity spawnFox(final TestContext context) {
    final FoxEntity fox = context.spawnEntity(EntityType.FOX, FOX_START);
    fox.setAiDisabled(true);
    return fox;
  }

  private static void trust(final FoxEntity fox, final UUID player) {
    final NbtCompound nbt = new NbtCompound();
    fox.writeCustomDataToNbt(nbt);
    final NbtList trusted = new NbtList();
    trusted.add(NbtHelper.fromUuid(player));
    nbt.put("Trusted", trusted);
    fox.readCustomDataFromNbt(nbt);
  }

  private static UnleashedDogEntity spawnOwnedDog(final TestContext context) {
    final ServerPlayerEntity owner = CreativeTestPlayer.join(context);
    owner.changeGameMode(GameMode.SURVIVAL);
    final BlockPos spot = context.getAbsolutePos(OWNER_SPOT);
    owner.requestTeleport(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5);
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.HUSKY, DOG_START);
    dog.setAiDisabled(true);
    dog.setTamed(true, true);
    dog.setOwnerUuid(owner.getUuid());
    dog.getCommandController().apply(DogCommand.FREE_ROAM);
    return dog;
  }
}
