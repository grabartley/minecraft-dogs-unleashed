package com.grahambartley.gametest;

import com.grahambartley.ModSounds;
import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.gametest.util.DogTestData;
import com.grahambartley.gametest.util.DogTestHelper;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class DogEntitySoundTest implements FabricGameTest {

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyBarkSoundIsRegistered(TestContext context) {
    testBarkSoundIsRegistered(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dachshundBarkSoundIsRegistered(TestContext context) {
    testBarkSoundIsRegistered(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void beagleBarkSoundIsRegistered(TestContext context) {
    testBarkSoundIsRegistered(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void goldenRetrieverBarkSoundIsRegistered(TestContext context) {
    testBarkSoundIsRegistered(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void shibaInuBarkSoundIsRegistered(TestContext context) {
    testBarkSoundIsRegistered(context, DogTestData.SHIBA_INU);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyHowlSoundIsRegistered(TestContext context) {
    context.assertTrue(ModSounds.HUSKY_HOWL != null, "Husky howl sound should be registered");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void huskyBarksWhenLowHealth(TestContext context) {
    testDogBarksWhenLowHealth(context, DogTestData.HUSKY);
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
  public void huskyBarksWhenHasTarget(TestContext context) {
    testDogBarksWhenHasTarget(context, DogTestData.HUSKY);
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
  public void huskyBarksWhenTakingDamage(TestContext context) {
    testDogBarksWhenTakingDamage(context, DogTestData.HUSKY);
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
  public void huskyCooldownPreventsBark(TestContext context) {
    testCooldownPreventsBark(context, DogTestData.HUSKY);
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

  private <T extends UnleashedDogEntity> void testBarkSoundIsRegistered(
      TestContext context, DogTestData<T> data) {
    DogTestHelper.spawnDog(context, data);
    context.assertTrue(
        data.expectedBarkSound() != null, "Bark sound should be non-null for " + data.breedId());
    context.complete();
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

  private <T extends UnleashedDogEntity> void testDogBarksWhenHasTarget(
      TestContext context, DogTestData<T> data) {
    ServerWorld world = context.getWorld();
    T dog = DogTestHelper.spawnDog(context, data);

    ZombieEntity zombie = new ZombieEntity(EntityType.ZOMBIE, world);
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

  private <T extends UnleashedDogEntity> void testCooldownPreventsBark(
      TestContext context, DogTestData<T> data) {
    ServerWorld world = context.getWorld();
    T dog = DogTestHelper.spawnDog(context, data);

    ZombieEntity zombie = new ZombieEntity(EntityType.ZOMBIE, world);
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
}
