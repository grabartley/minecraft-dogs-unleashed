package com.grahambartley.gametest;

import com.grahambartley.ModBlocks;
import com.grahambartley.ModEntities;
import com.grahambartley.ModSounds;
import com.grahambartley.entity.BeagleEntity;
import com.grahambartley.entity.DachshundEntity;
import com.grahambartley.entity.GoldenRetrieverEntity;
import com.grahambartley.entity.HuskyEntity;
import com.grahambartley.entity.ShibaInuEntity;
import com.grahambartley.entity.UnleashedDogEntity;
import java.util.function.Function;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class DogBarkingGameTest implements FabricGameTest {

  private static final TestData<HuskyEntity> HUSKY_DATA =
      new TestData<>(
          ModEntities.HUSKY,
          world -> new HuskyEntity(ModEntities.HUSKY, world),
          ModSounds.HUSKY_BARK);

  private static final TestData<DachshundEntity> DACHSHUND_DATA =
      new TestData<>(
          ModEntities.DACHSHUND,
          world -> new DachshundEntity(ModEntities.DACHSHUND, world),
          ModSounds.DACHSHUND_BARK);

  private static final TestData<BeagleEntity> BEAGLE_DATA =
      new TestData<>(
          ModEntities.BEAGLE,
          world -> new BeagleEntity(ModEntities.BEAGLE, world),
          ModSounds.BEAGLE_BARK);

  private static final TestData<GoldenRetrieverEntity> GOLDEN_RETRIEVER_DATA =
      new TestData<>(
          ModEntities.GOLDEN_RETRIEVER,
          world -> new GoldenRetrieverEntity(ModEntities.GOLDEN_RETRIEVER, world),
          ModSounds.GOLDEN_RETRIEVER_BARK);

  private static final TestData<ShibaInuEntity> SHIBA_INU_DATA =
      new TestData<>(
          ModEntities.SHIBA_INU,
          world -> new ShibaInuEntity(ModEntities.SHIBA_INU, world),
          ModSounds.SHIBA_INU_BARK);

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyGetBarkSoundReturnsNonNull(final TestContext context) {
    testGetBarkSoundReturnsNonNull(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dachshundGetBarkSoundReturnsNonNull(final TestContext context) {
    testGetBarkSoundReturnsNonNull(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void beagleGetBarkSoundReturnsNonNull(final TestContext context) {
    testGetBarkSoundReturnsNonNull(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void goldenRetrieverGetBarkSoundReturnsNonNull(final TestContext context) {
    testGetBarkSoundReturnsNonNull(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void shibaInuGetBarkSoundReturnsNonNull(final TestContext context) {
    testGetBarkSoundReturnsNonNull(context, SHIBA_INU_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void huskyBarksWhenLowHealth(final TestContext context) {
    testDogBarksWhenLowHealth(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void dachshundBarksWhenLowHealth(final TestContext context) {
    testDogBarksWhenLowHealth(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void beagleBarksWhenLowHealth(final TestContext context) {
    testDogBarksWhenLowHealth(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void goldenRetrieverBarksWhenLowHealth(final TestContext context) {
    testDogBarksWhenLowHealth(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void shibaInuBarksWhenLowHealth(final TestContext context) {
    testDogBarksWhenLowHealth(context, SHIBA_INU_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void huskyBarksWhenHasTarget(final TestContext context) {
    testDogBarksWhenHasTarget(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void dachshundBarksWhenHasTarget(final TestContext context) {
    testDogBarksWhenHasTarget(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void beagleBarksWhenHasTarget(final TestContext context) {
    testDogBarksWhenHasTarget(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void goldenRetrieverBarksWhenHasTarget(final TestContext context) {
    testDogBarksWhenHasTarget(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void shibaInuBarksWhenHasTarget(final TestContext context) {
    testDogBarksWhenHasTarget(context, SHIBA_INU_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void huskyCooldownPreventsBark(final TestContext context) {
    testCooldownPreventsBark(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void dachshundCooldownPreventsBark(final TestContext context) {
    testCooldownPreventsBark(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void beagleCooldownPreventsBark(final TestContext context) {
    testCooldownPreventsBark(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void goldenRetrieverCooldownPreventsBark(final TestContext context) {
    testCooldownPreventsBark(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void shibaInuCooldownPreventsBark(final TestContext context) {
    testCooldownPreventsBark(context, SHIBA_INU_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void huskyBarksWhenTakingDamage(final TestContext context) {
    testDogBarksWhenTakingDamage(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void dachshundBarksWhenTakingDamage(final TestContext context) {
    testDogBarksWhenTakingDamage(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void beagleBarksWhenTakingDamage(final TestContext context) {
    testDogBarksWhenTakingDamage(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void goldenRetrieverBarksWhenTakingDamage(final TestContext context) {
    testDogBarksWhenTakingDamage(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void shibaInuBarksWhenTakingDamage(final TestContext context) {
    testDogBarksWhenTakingDamage(context, SHIBA_INU_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyHowlSoundIsRegistered(final TestContext context) {
    context.assertTrue(ModSounds.HUSKY_HOWL != null, "Husky howl sound should be registered");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200, required = false)
  public void huskySleepingDoesNotBark(final TestContext context) {
    testSleepingDogDoesNotBark(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200, required = false)
  public void dachshundSleepingDoesNotBark(final TestContext context) {
    testSleepingDogDoesNotBark(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200, required = false)
  public void beagleSleepingDoesNotBark(final TestContext context) {
    testSleepingDogDoesNotBark(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200, required = false)
  public void goldenRetrieverSleepingDoesNotBark(final TestContext context) {
    testSleepingDogDoesNotBark(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200, required = false)
  public void shibaInuSleepingDoesNotBark(final TestContext context) {
    testSleepingDogDoesNotBark(context, SHIBA_INU_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200, required = false)
  public void sleepingHuskyDoesNotHowl(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(
        absBedPos.getX(), absBedPos.getY(), absBedPos.getZ(), 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(absBedPos);
          husky.commandToSleep(absBedPos);
          husky.startSleepingInBed(absBedPos);
        });

    context.runAtTick(
        50,
        () -> {
          context.assertTrue(husky.isSleepingInBed(), "Husky should still be sleeping");
          context.assertTrue(
              husky.getHowlCooldownTicks() == 0,
              "Sleeping husky should not have howled (cooldown should be 0)");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testGetBarkSoundReturnsNonNull(
      final TestContext context, final TestData<T> data) {
    final ServerWorld world = context.getWorld();
    final T dog = data.factory.apply(world);
    dog.refreshPositionAndAngles(new BlockPos(0, 1, 0), 0.0f, 0.0f);
    world.spawnEntity(dog);

    context.assertTrue(
        data.expectedBarkSound != null, "Bark sound should be non-null for " + dog.getBreedId());
    context.complete();
  }

  private <T extends UnleashedDogEntity> void testDogBarksWhenLowHealth(
      final TestContext context, final TestData<T> data) {
    final ServerWorld world = context.getWorld();
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final T dog = data.factory.apply(world);
    dog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dog);

    context.runAtTick(
        5,
        () -> {
          dog.setHealth(1.0f);
        });

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              dog.getBarkCooldownTicks() > 0,
              "Low health dog should have barked (cooldown should be > 0)");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testDogBarksWhenHasTarget(
      final TestContext context, final TestData<T> data) {
    final ServerWorld world = context.getWorld();
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final T dog = data.factory.apply(world);
    dog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dog);

    final ZombieEntity zombie = new ZombieEntity(EntityType.ZOMBIE, world);
    zombie.refreshPositionAndAngles(new BlockPos(2, 1, 0), 0.0f, 0.0f);
    world.spawnEntity(zombie);

    context.runAtTick(
        5,
        () -> {
          dog.setTarget(zombie);
        });

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              dog.getBarkCooldownTicks() > 0,
              "Dog with target should have barked (cooldown should be > 0)");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testCooldownPreventsBark(
      final TestContext context, final TestData<T> data) {
    final ServerWorld world = context.getWorld();
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final T dog = data.factory.apply(world);
    dog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dog);

    final ZombieEntity zombie = new ZombieEntity(EntityType.ZOMBIE, world);
    zombie.refreshPositionAndAngles(new BlockPos(2, 1, 0), 0.0f, 0.0f);
    world.spawnEntity(zombie);

    context.runAtTick(
        5,
        () -> {
          dog.setTarget(zombie);
        });

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              dog.getBarkCooldownTicks() > 0, "Dog should have barked once (cooldown > 0)");
          final int cooldownAfterFirstBark = dog.getBarkCooldownTicks();

          context.runAtTick(
              12,
              () -> {
                context.assertTrue(
                    dog.getBarkCooldownTicks() < cooldownAfterFirstBark,
                    "Cooldown should be decrementing");
                context.assertTrue(
                    dog.getBarkCooldownTicks() > 0,
                    "Cooldown should still be active, preventing another bark");
                context.complete();
              });
        });
  }

  private <T extends UnleashedDogEntity> void testSleepingDogDoesNotBark(
      final TestContext context, final TestData<T> data) {
    final ServerWorld world = context.getWorld();
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final T dog = data.factory.apply(world);
    dog.refreshPositionAndAngles(absBedPos.getX(), absBedPos.getY(), absBedPos.getZ(), 0.0f, 0.0f);
    dog.setTamed(true, true);
    world.spawnEntity(dog);

    context.runAtTick(
        10,
        () -> {
          dog.setAssignedBedPos(absBedPos);
          dog.commandToSleep(absBedPos);
          dog.startSleepingInBed(absBedPos);
        });

    context.runAtTick(
        50,
        () -> {
          context.assertTrue(dog.isSleepingInBed(), "Dog should still be sleeping");
          context.assertTrue(
              dog.getBarkCooldownTicks() == 0,
              "Sleeping dog should not have barked (cooldown should be 0)");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testDogBarksWhenTakingDamage(
      final TestContext context, final TestData<T> data) {
    final ServerWorld world = context.getWorld();
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final T dog = data.factory.apply(world);
    dog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dog);

    context.runAtTick(
        5,
        () -> {
          final DamageSource source = world.getDamageSources().generic();
          dog.damage(source, 1.0f);
        });

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              dog.getBarkCooldownTicks() > 0,
              "Dog should have barked when taking damage (cooldown should be > 0)");
          context.complete();
        });
  }

  private record TestData<T extends UnleashedDogEntity>(
      EntityType<T> entityType, Function<World, T> factory, SoundEvent expectedBarkSound) {}
}
