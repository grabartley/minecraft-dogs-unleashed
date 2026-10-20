package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.config.AgingSettings;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.DogAging;
import com.grahambartley.dogsunleashed.entity.DogAgingRules;
import com.grahambartley.dogsunleashed.entity.DogCommand;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.SleepBatchFixture;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class DogAgingGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String AGING_ON = "dog-aging-on";
  private static final String AGING_OFF = "dog-aging-off";
  private static final int SENIOR_AFTER_DAYS = 1;
  private static final long SENIOR_AGE = DogAgingRules.seniorAgeTicks(SENIOR_AFTER_DAYS);
  private static final long MORNING_NAP_TIME = 4000;
  private static final BlockPos REL_DOG = new BlockPos(3, 2, 3);
  private static final BlockPos REL_MATE = new BlockPos(1, 2, 3);
  private static final int SETTLE_TICKS = 40;
  private static final double SPEED_TOLERANCE = 1.0e-6;

  private static DogsUnleashedConfig original = DogsUnleashedConfig.defaults();

  @BeforeBatch(batchId = AGING_ON)
  public void beforeAgingOn(final ServerWorld world) {
    original = DogsUnleashed.SERVER_CONFIG;
    DogsUnleashed.SERVER_CONFIG = original.withAging(new AgingSettings(true, SENIOR_AFTER_DAYS));
    SleepBatchFixture.prepare(world);
    world.setTimeOfDay(MORNING_NAP_TIME);
  }

  @AfterBatch(batchId = AGING_ON)
  public void afterAgingOn(final ServerWorld world) {
    DogsUnleashed.SERVER_CONFIG = original;
    SleepBatchFixture.teardown(world);
  }

  @BeforeBatch(batchId = AGING_OFF)
  public void beforeAgingOff(final ServerWorld world) {
    original = DogsUnleashed.SERVER_CONFIG;
    DogsUnleashed.SERVER_CONFIG = original.withAging(AgingSettings.DEFAULTS);
  }

  @AfterBatch(batchId = AGING_OFF)
  public void afterAgingOff(final ServerWorld world) {
    DogsUnleashed.SERVER_CONFIG = original;
  }

  @GameTest(templateName = ARENA, batchId = AGING_ON, tickLimit = 40)
  public void anAdultPastTheSeniorAgeTurnsSeniorAndWalksSlower(final TestContext context) {
    final UnleashedDogEntity dog = still(context, ModEntities.BEAGLE, REL_DOG);
    final double adultSpeed = speedOf(dog);
    aging(dog).setAdultAgeTicks(SENIOR_AGE);

    context.runAtTick(
        5,
        () -> {
          context.assertTrue(aging(dog).isSenior(), "An adult past the senior age is senior");
          context.assertTrue(
              Math.abs(speedOf(dog) - adultSpeed * (1.0 + DogAgingRules.SENIOR_SPEED_FRACTION))
                  < SPEED_TOLERANCE,
              "A senior walks a little slower, was " + speedOf(dog) + " from " + adultSpeed);
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = AGING_ON, tickLimit = 60)
  public void anAdultAgesWithWorldTime(final TestContext context) {
    final UnleashedDogEntity dog = still(context, ModEntities.HUSKY, REL_DOG);
    aging(dog).setAdultAgeTicks(SENIOR_AGE - 10);

    context.runAtTick(
        3, () -> context.assertFalse(aging(dog).isSenior(), "Ten ticks short is not yet senior"));
    context.runAtTick(
        30,
        () -> {
          context.assertTrue(aging(dog).isSenior(), "Passing world time made the adult senior");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = AGING_ON, tickLimit = 60)
  public void aPuppyNeverAges(final TestContext context) {
    final UnleashedDogEntity puppy = still(context, ModEntities.BEAGLE, REL_DOG);
    puppy.setBaby(true);
    aging(puppy).setAdultAgeTicks(SENIOR_AGE - 10);

    context.runAtTick(
        30,
        () -> {
          context.assertFalse(aging(puppy).isSenior(), "Puppies do not age");
          context.assertTrue(
              aging(puppy).adultAgeTicks() == SENIOR_AGE - 10, "A puppy's adult age holds");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = AGING_ON, tickLimit = 60)
  public void aYearlingDireWolfDoesNotAgeYet(final TestContext context) {
    final UnleashedDogEntity wolf = still(context, ModEntities.DIRE_WOLF, REL_DOG);
    wolf.getGrowth().setStage(DireWolfGrowthStage.YEARLING);
    aging(wolf).setAdultAgeTicks(SENIOR_AGE - 10);

    context.runAtTick(
        30,
        () -> {
          context.assertFalse(aging(wolf).isSenior(), "Dire wolves only age once adult");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = AGING_ON, tickLimit = 40)
  public void aSeniorDireWolfKeepsItsSize(final TestContext context) {
    final UnleashedDogEntity wolf = still(context, ModEntities.DIRE_WOLF, REL_DOG);
    wolf.getGrowth().setStage(DireWolfGrowthStage.ADULT);
    final float width = wolf.getWidth();
    final float height = wolf.getHeight();
    aging(wolf).setAdultAgeTicks(SENIOR_AGE);

    context.runAtTick(
        5,
        () -> {
          context.assertTrue(aging(wolf).isSenior(), "An old adult dire wolf is senior");
          context.assertTrue(
              wolf.getGrowth().stage() == DireWolfGrowthStage.ADULT, "Aging keeps the stage");
          context.assertTrue(
              wolf.getWidth() == width && wolf.getHeight() == height, "Aging keeps the size");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = AGING_ON, tickLimit = 40)
  public void aPupWithASeniorParentCarriesTheLegacy(final TestContext context) {
    final UnleashedDogEntity senior = still(context, ModEntities.BEAGLE, REL_DOG);
    final UnleashedDogEntity mate = still(context, ModEntities.BEAGLE, REL_MATE);
    aging(senior).setAdultAgeTicks(SENIOR_AGE);

    context.runAtTick(
        5,
        () -> {
          final UnleashedDogEntity legacyPup =
              (UnleashedDogEntity) senior.createChild(context.getWorld(), mate);
          final UnleashedDogEntity plainPup =
              (UnleashedDogEntity) mate.createChild(context.getWorld(), mate);
          context.assertTrue(
              aging(legacyPup).hasSeniorParent(), "A pup of a senior carries the legacy");
          context.assertFalse(
              aging(plainPup).hasSeniorParent(), "A pup of two adults has no legacy");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = AGING_ON, tickLimit = 100)
  public void aSeniorDogThatSleepsInBedsNapsOnTheGround(final TestContext context) {
    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, REL_DOG);
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    husky.setTamed(true, true);
    husky.setOwnerUuid(owner.getUuid());
    husky.getCommandController().apply(DogCommand.STAY);
    aging(husky).setAdultAgeTicks(SENIOR_AGE);

    context.runAtTick(
        SETTLE_TICKS,
        () -> {
          context.assertTrue(aging(husky).isSenior(), "The old husky is senior");
          context.assertTrue(
              husky.getSleepController().isNapping(), "A senior husky naps on the ground");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = AGING_ON, tickLimit = 20)
  public void ageSurvivesASaveAndLoad(final TestContext context) {
    final UnleashedDogEntity dog = still(context, ModEntities.BEAGLE, REL_DOG);
    aging(dog).setAdultAgeTicks(SENIOR_AGE + 123);
    final NbtCompound nbt = new NbtCompound();
    dog.writeNbt(nbt);
    final UnleashedDogEntity loaded = still(context, ModEntities.BEAGLE, REL_MATE);
    loaded.readNbt(nbt);
    context.assertTrue(
        aging(loaded).adultAgeTicks() == SENIOR_AGE + 123, "The adult age was lost on reload");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = AGING_ON, tickLimit = 20)
  public void timeWhileUnloadedCountsOnReload(final TestContext context) {
    final UnleashedDogEntity dog = still(context, ModEntities.BEAGLE, REL_DOG);
    final NbtCompound nbt = new NbtCompound();
    dog.writeNbt(nbt);
    nbt.putLong(ModNbtKeys.ADULT_AGE_TICKS, 0L);
    nbt.putLong(ModNbtKeys.AGE_LAST_SEEN_TIME, context.getWorld().getTime() - SENIOR_AGE);
    final UnleashedDogEntity loaded = still(context, ModEntities.BEAGLE, REL_MATE);
    loaded.readNbt(nbt);

    context.runAtTick(
        5,
        () -> {
          context.assertTrue(
              aging(loaded).adultAgeTicks() >= SENIOR_AGE,
              "A day away should count, but the age is " + aging(loaded).adultAgeTicks());
          context.assertTrue(aging(loaded).isSenior(), "A day away made the adult senior");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = AGING_OFF, tickLimit = 40)
  public void withAgingOffNothingAges(final TestContext context) {
    final UnleashedDogEntity dog = still(context, ModEntities.BEAGLE, REL_DOG);
    final double adultSpeed = speedOf(dog);
    aging(dog).setAdultAgeTicks(DogAgingRules.seniorAgeTicks(AgingSettings.DAYS_MAX) * 2);

    context.runAtTick(
        20,
        () -> {
          context.assertFalse(aging(dog).isSenior(), "With aging off no dog is senior");
          context.assertTrue(
              aging(dog).adultAgeTicks()
                  == DogAgingRules.seniorAgeTicks(AgingSettings.DAYS_MAX) * 2,
              "With aging off the age does not move");
          context.assertTrue(speedOf(dog) == adultSpeed, "With aging off the speed holds");
          context.complete();
        });
  }

  private static UnleashedDogEntity still(
      final TestContext context, final EntityType<UnleashedDogEntity> type, final BlockPos pos) {
    final UnleashedDogEntity dog = context.spawnEntity(type, pos);
    dog.setAiDisabled(true);
    return dog;
  }

  private static DogAging aging(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogAging.class);
  }

  private static double speedOf(final UnleashedDogEntity dog) {
    return dog.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
  }
}
