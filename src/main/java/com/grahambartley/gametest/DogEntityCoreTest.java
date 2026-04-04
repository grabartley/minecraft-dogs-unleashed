package com.grahambartley.gametest;

import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.gametest.util.DogTestData;
import com.grahambartley.gametest.util.DogTestHelper;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.DyeColor;

public final class DogEntityCoreTest implements FabricGameTest {

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void huskySpawnsCorrectly(TestContext context) {
    testDogSpawnsCorrectly(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void dachshundSpawnsCorrectly(TestContext context) {
    testDogSpawnsCorrectly(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void beagleSpawnsCorrectly(TestContext context) {
    testDogSpawnsCorrectly(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void goldenRetrieverSpawnsCorrectly(TestContext context) {
    testDogSpawnsCorrectly(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void shibaInuSpawnsCorrectly(TestContext context) {
    testDogSpawnsCorrectly(context, DogTestData.SHIBA_INU);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyIsTameable(TestContext context) {
    testDogIsTameable(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dachshundIsTameable(TestContext context) {
    testDogIsTameable(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void beagleIsTameable(TestContext context) {
    testDogIsTameable(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void goldenRetrieverIsTameable(TestContext context) {
    testDogIsTameable(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void shibaInuIsTameable(TestContext context) {
    testDogIsTameable(context, DogTestData.SHIBA_INU);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void huskyCanBeTamed(TestContext context) {
    testDogCanBeTamed(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dachshundCanBeTamed(TestContext context) {
    testDogCanBeTamed(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void beagleCanBeTamed(TestContext context) {
    testDogCanBeTamed(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void goldenRetrieverCanBeTamed(TestContext context) {
    testDogCanBeTamed(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void shibaInuCanBeTamed(TestContext context) {
    testDogCanBeTamed(context, DogTestData.SHIBA_INU);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void huskyHasCorrectDimensions(TestContext context) {
    testDogHasCorrectDimensions(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dachshundHasCorrectDimensions(TestContext context) {
    testDogHasCorrectDimensions(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void beagleHasCorrectDimensions(TestContext context) {
    testDogHasCorrectDimensions(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void goldenRetrieverHasCorrectDimensions(TestContext context) {
    testDogHasCorrectDimensions(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void shibaInuHasCorrectDimensions(TestContext context) {
    testDogHasCorrectDimensions(context, DogTestData.SHIBA_INU);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyCollarColorPersistsInNbt(TestContext context) {
    testCollarColorPersistsInNbt(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dachshundCollarColorPersistsInNbt(TestContext context) {
    testCollarColorPersistsInNbt(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void beagleCollarColorPersistsInNbt(TestContext context) {
    testCollarColorPersistsInNbt(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void goldenRetrieverCollarColorPersistsInNbt(TestContext context) {
    testCollarColorPersistsInNbt(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void shibaInuCollarColorPersistsInNbt(TestContext context) {
    testCollarColorPersistsInNbt(context, DogTestData.SHIBA_INU);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyHasAnimatableInstanceCache(TestContext context) {
    testDogHasAnimatableInstanceCache(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dachshundHasAnimatableInstanceCache(TestContext context) {
    testDogHasAnimatableInstanceCache(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void beagleHasAnimatableInstanceCache(TestContext context) {
    testDogHasAnimatableInstanceCache(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void goldenRetrieverHasAnimatableInstanceCache(TestContext context) {
    testDogHasAnimatableInstanceCache(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void shibaInuHasAnimatableInstanceCache(TestContext context) {
    testDogHasAnimatableInstanceCache(context, DogTestData.SHIBA_INU);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void untamedHuskyHasDefaultCollarColor(TestContext context) {
    testUntamedDogHasDefaultCollarColor(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void untamedDachshundHasDefaultCollarColor(TestContext context) {
    testUntamedDogHasDefaultCollarColor(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void untamedBeagleHasDefaultCollarColor(TestContext context) {
    testUntamedDogHasDefaultCollarColor(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void untamedGoldenRetrieverHasDefaultCollarColor(TestContext context) {
    testUntamedDogHasDefaultCollarColor(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void untamedShibaInuHasDefaultCollarColor(TestContext context) {
    testUntamedDogHasDefaultCollarColor(context, DogTestData.SHIBA_INU);
  }

  private <T extends UnleashedDogEntity> void testDogSpawnsCorrectly(
      TestContext context, DogTestData<T> data) {
    ServerWorld world = context.getWorld();
    T dog = DogTestHelper.spawnDog(context, data);

    context.runAtTick(
        1,
        () -> {
          context.assertTrue(!dog.isRemoved(), "Dog should be alive and present in the world");
          context.assertTrue(
              world.getEntitiesByType(data.entityType(), entity -> true).contains(dog),
              "Dog should be in the world's entity list");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testDogIsTameable(
      TestContext context, DogTestData<T> data) {
    T dog = DogTestHelper.spawnDog(context, data);
    context.assertTrue(dog instanceof TameableEntity, "Dog must be a TameableEntity");
    context.complete();
  }

  private <T extends UnleashedDogEntity> void testDogCanBeTamed(
      TestContext context, DogTestData<T> data) {
    T dog = DogTestHelper.spawnDog(context, data);

    context.runAtTick(
        10,
        () -> {
          context.assertFalse(dog.isTamed(), "Dog should not be tamed initially");
          dog.setTamed(true, true);

          context.runAtTick(
              20,
              () -> {
                context.assertTrue(dog.isTamed(), "Dog should be tamed after setTamed");
                context.complete();
              });
        });
  }

  private <T extends UnleashedDogEntity> void testDogHasCorrectDimensions(
      TestContext context, DogTestData<T> data) {
    T dog = DogTestHelper.spawnDog(context, data);

    context.assertTrue(
        Math.abs(dog.getWidth() - data.expectedWidth()) < 0.01f,
        "Dog width should be " + data.expectedWidth());
    context.assertTrue(
        Math.abs(dog.getHeight() - data.expectedHeight()) < 0.01f,
        "Dog height should be " + data.expectedHeight());
    context.complete();
  }

  private <T extends UnleashedDogEntity> void testCollarColorPersistsInNbt(
      TestContext context, DogTestData<T> data) {
    ServerWorld world = context.getWorld();
    T dog = DogTestHelper.spawnTamedDog(context, data);

    dog.setCollarColor(DyeColor.LIME);

    NbtCompound nbt = new NbtCompound();
    dog.writeCustomDataToNbt(nbt);

    context.assertTrue(nbt.contains("CollarColor"), "NBT should contain CollarColor data");
    context.assertTrue(
        nbt.getInt("CollarColor") == DyeColor.LIME.getId(), "NBT should store LIME color ID");

    T newDog = data.factory().apply(world);
    newDog.readCustomDataFromNbt(nbt);

    context.assertTrue(
        newDog.getCollarColor() == DyeColor.LIME, "Collar color should persist after NBT load");
    context.complete();
  }

  private <T extends UnleashedDogEntity> void testDogHasAnimatableInstanceCache(
      TestContext context, DogTestData<T> data) {
    T dog = DogTestHelper.spawnDog(context, data);

    context.assertTrue(
        dog.getAnimatableInstanceCache() != null,
        "Dog should have AnimatableInstanceCache for GeckoLib");
    context.complete();
  }

  private <T extends UnleashedDogEntity> void testUntamedDogHasDefaultCollarColor(
      TestContext context, DogTestData<T> data) {
    T dog = DogTestHelper.spawnDog(context, data);

    context.assertFalse(dog.isTamed(), "Dog should not be tamed initially");
    context.assertTrue(
        dog.getCollarColor() == DyeColor.RED, "Untamed dog should have RED collar color");
    context.complete();
  }
}
