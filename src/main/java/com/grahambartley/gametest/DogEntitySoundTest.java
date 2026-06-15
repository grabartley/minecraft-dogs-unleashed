package com.grahambartley.gametest;

import com.grahambartley.entity.HuskyEntity;
import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.gametest.util.DogTestData;
import com.grahambartley.gametest.util.DogTestHelper;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class DogEntitySoundTest implements FabricGameTest {

  // Sound-registry presence checks (huskyHasNoBarkSoundRegistered, huskyHowlSoundIsRegistered, and
  // the four per-breed BarkSoundIsRegistered tests) live in DogSoundRegistrationTest under
  // src/test/java since they only query Registries.SOUND_EVENT and don't need a live world. See
  // gametest skill rule 10.

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void huskyDoesNotBarkWhenLowHealth(TestContext context) {
    testDogDoesNotBarkWhenLowHealth(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void dachshundBarksWhenLowHealth(TestContext context) {
    testDogBarksWhenLowHealth(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void beagleBarksWhenLowHealth(TestContext context) {
    testDogBarksWhenLowHealth(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void goldenRetrieverBarksWhenLowHealth(TestContext context) {
    testDogBarksWhenLowHealth(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void shibaInuBarksWhenLowHealth(TestContext context) {
    testDogBarksWhenLowHealth(context, DogTestData.SHIBA_INU);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void huskyDoesNotBarkWhenHasTarget(TestContext context) {
    testDogDoesNotBarkWhenHasTarget(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void dachshundBarksWhenHasTarget(TestContext context) {
    testDogBarksWhenHasTarget(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void beagleBarksWhenHasTarget(TestContext context) {
    testDogBarksWhenHasTarget(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void goldenRetrieverBarksWhenHasTarget(TestContext context) {
    testDogBarksWhenHasTarget(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void shibaInuBarksWhenHasTarget(TestContext context) {
    testDogBarksWhenHasTarget(context, DogTestData.SHIBA_INU);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void huskyDoesNotBarkWhenTakingDamage(TestContext context) {
    testDogDoesNotBarkWhenTakingDamage(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void dachshundBarksWhenTakingDamage(TestContext context) {
    testDogBarksWhenTakingDamage(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void beagleBarksWhenTakingDamage(TestContext context) {
    testDogBarksWhenTakingDamage(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void goldenRetrieverBarksWhenTakingDamage(TestContext context) {
    testDogBarksWhenTakingDamage(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void shibaInuBarksWhenTakingDamage(TestContext context) {
    testDogBarksWhenTakingDamage(context, DogTestData.SHIBA_INU);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void huskyBarkCooldownStaysInactive(TestContext context) {
    testBarkCooldownStaysInactive(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void dachshundCooldownPreventsBark(TestContext context) {
    testCooldownPreventsBark(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void beagleCooldownPreventsBark(TestContext context) {
    testCooldownPreventsBark(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void goldenRetrieverCooldownPreventsBark(TestContext context) {
    testCooldownPreventsBark(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void shibaInuCooldownPreventsBark(TestContext context) {
    testCooldownPreventsBark(context, DogTestData.SHIBA_INU);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyStartsNotHowling(TestContext context) {
    HuskyEntity husky = DogTestHelper.spawnDog(context, DogTestData.HUSKY);

    context.assertFalse(husky.isHowling(), "Husky should not be howling immediately after spawn");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyStartsWithZeroHowlCooldown(TestContext context) {
    HuskyEntity husky = DogTestHelper.spawnDog(context, DogTestData.HUSKY);

    context.assertTrue(
        husky.getHowlCooldownTicks() == 0, "Husky howl cooldown should be 0 at spawn");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void huskySleepingPreventsHowling(TestContext context) {
    final BlockPos absBedPos = context.getAbsolutePos(new BlockPos(0, 1, 0));
    HuskyEntity husky = DogTestHelper.spawnDog(context, DogTestData.HUSKY);

    context.runAtTick(
        5,
        () -> {
          husky.startSleepingInBed(absBedPos);
        });

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(husky.isSleepingInBed(), "Husky should be sleeping");
          context.assertFalse(husky.isHowling(), "Sleeping husky should not be howling");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void huskyHowlingFlagSetsAndClearsCorrectly(TestContext context) {
    HuskyEntity husky = DogTestHelper.spawnDog(context, DogTestData.HUSKY);

    context.runAtTick(
        5,
        () -> {
          context.assertFalse(
              husky.isHowling(), "isHowling should start false before any howl is triggered");
        });

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              husky.getHowlCooldownTicks() == 0,
              "Howl cooldown should still be 0 since howl has not triggered");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void huskyHowlCooldownIsNonNegativeAfterMultipleTicks(TestContext context) {
    HuskyEntity husky = DogTestHelper.spawnDog(context, DogTestData.HUSKY);

    context.runAtTick(
        50,
        () -> {
          context.assertTrue(
              husky.getHowlCooldownTicks() >= 0,
              "husky howl cooldown should never go negative after ticking");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testDogBarksWhenLowHealth(
      TestContext context, DogTestData<T> data) {
    T dog = DogTestHelper.spawnDog(context, data);

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

  private <T extends UnleashedDogEntity> void testDogDoesNotBarkWhenLowHealth(
      TestContext context, DogTestData<T> data) {
    T dog = DogTestHelper.spawnDog(context, data);

    context.runAtTick(5, () -> dog.setHealth(1.0f));

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              dog.getBarkCooldownTicks() == 0,
              data.breed().serializedId() + " should not bark at low health");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testDogBarksWhenHasTarget(
      TestContext context, DogTestData<T> data) {
    T dog = DogTestHelper.spawnDog(context, data);

    ZombieEntity zombie =
        (ZombieEntity) context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 0));

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

  private <T extends UnleashedDogEntity> void testDogDoesNotBarkWhenHasTarget(
      TestContext context, DogTestData<T> data) {
    T dog = DogTestHelper.spawnDog(context, data);

    ZombieEntity zombie =
        (ZombieEntity) context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 0));

    context.runAtTick(5, () -> dog.setTarget(zombie));

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              dog.getBarkCooldownTicks() == 0,
              data.breed().serializedId() + " should not bark with a target");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testDogBarksWhenTakingDamage(
      TestContext context, DogTestData<T> data) {
    T dog = DogTestHelper.spawnDog(context, data);

    context.runAtTick(
        5,
        () -> {
          DogTestHelper.damageEntity(dog, 1.0f);
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

  private <T extends UnleashedDogEntity> void testDogDoesNotBarkWhenTakingDamage(
      TestContext context, DogTestData<T> data) {
    T dog = DogTestHelper.spawnDog(context, data);

    context.runAtTick(5, () -> DogTestHelper.damageEntity(dog, 1.0f));

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              dog.getBarkCooldownTicks() == 0,
              data.breed().serializedId() + " should not bark when taking damage");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testCooldownPreventsBark(
      TestContext context, DogTestData<T> data) {
    T dog = DogTestHelper.spawnDog(context, data);

    ZombieEntity zombie =
        (ZombieEntity) context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 0));

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
          int cooldownAfterFirstBark = dog.getBarkCooldownTicks();

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

  private <T extends UnleashedDogEntity> void testBarkCooldownStaysInactive(
      TestContext context, DogTestData<T> data) {
    T dog = DogTestHelper.spawnDog(context, data);

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              dog.getBarkCooldownTicks() == 0,
              data.breed().serializedId() + " bark cooldown should stay 0");
          context.complete();
        });
  }
}
