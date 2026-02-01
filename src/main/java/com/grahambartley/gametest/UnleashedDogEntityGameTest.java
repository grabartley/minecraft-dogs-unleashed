package com.grahambartley.gametest;

import com.grahambartley.ModEntities;
import com.grahambartley.entity.BeagleEntity;
import com.grahambartley.entity.DachshundEntity;
import com.grahambartley.entity.GoldenRetrieverEntity;
import com.grahambartley.entity.HuskyEntity;
import com.grahambartley.entity.UnleashedDogEntity;
import java.util.function.BiFunction;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class UnleashedDogEntityGameTest implements FabricGameTest {

  private static final TestData<HuskyEntity> HUSKY_DATA =
      new TestData<>(
          ModEntities.HUSKY,
          (type, world) -> new HuskyEntity((EntityType<? extends UnleashedDogEntity>) type, world),
          0.8f,
          1.1f);

  private static final TestData<DachshundEntity> DACHSHUND_DATA =
      new TestData<>(
          ModEntities.DACHSHUND,
          (type, world) ->
              new DachshundEntity((EntityType<? extends UnleashedDogEntity>) type, world),
          0.8f,
          1.1f);

  private static final TestData<BeagleEntity> BEAGLE_DATA =
      new TestData<>(
          ModEntities.BEAGLE,
          (type, world) -> new BeagleEntity((EntityType<? extends UnleashedDogEntity>) type, world),
          0.8f,
          1.1f);

  private static final TestData<GoldenRetrieverEntity> GOLDEN_RETRIEVER_DATA =
      new TestData<>(
          ModEntities.GOLDEN_RETRIEVER,
          (type, world) ->
              new GoldenRetrieverEntity((EntityType<? extends UnleashedDogEntity>) type, world),
          0.8f,
          1.1f);

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void huskySpawnsCorrectly(final TestContext context) {
    testDogSpawnsCorrectly(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void dachshundSpawnsCorrectly(final TestContext context) {
    testDogSpawnsCorrectly(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void beagleSpawnsCorrectly(final TestContext context) {
    testDogSpawnsCorrectly(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void goldenRetrieverSpawnsCorrectly(final TestContext context) {
    testDogSpawnsCorrectly(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyIsTameable(final TestContext context) {
    testDogIsTameable(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dachshundIsTameable(final TestContext context) {
    testDogIsTameable(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void beagleIsTameable(final TestContext context) {
    testDogIsTameable(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void goldenRetrieverIsTameable(final TestContext context) {
    testDogIsTameable(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void huskyCanBeTamed(final TestContext context) {
    testDogCanBeTamed(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dachshundCanBeTamed(final TestContext context) {
    testDogCanBeTamed(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void beagleCanBeTamed(final TestContext context) {
    testDogCanBeTamed(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void goldenRetrieverCanBeTamed(final TestContext context) {
    testDogCanBeTamed(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void huskyHasCorrectDimensions(final TestContext context) {
    testDogHasCorrectDimensions(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dachshundHasCorrectDimensions(final TestContext context) {
    testDogHasCorrectDimensions(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void beagleHasCorrectDimensions(final TestContext context) {
    testDogHasCorrectDimensions(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void goldenRetrieverHasCorrectDimensions(final TestContext context) {
    testDogHasCorrectDimensions(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyHasAnimatableInstanceCache(final TestContext context) {
    testDogHasAnimatableInstanceCache(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dachshundHasAnimatableInstanceCache(final TestContext context) {
    testDogHasAnimatableInstanceCache(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void beagleHasAnimatableInstanceCache(final TestContext context) {
    testDogHasAnimatableInstanceCache(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void goldenRetrieverHasAnimatableInstanceCache(final TestContext context) {
    testDogHasAnimatableInstanceCache(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void untamedHuskyHasDefaultCollarColor(final TestContext context) {
    testUntamedDogHasDefaultCollarColor(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void untamedDachshundHasDefaultCollarColor(final TestContext context) {
    testUntamedDogHasDefaultCollarColor(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void untamedBeagleHasDefaultCollarColor(final TestContext context) {
    testUntamedDogHasDefaultCollarColor(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void untamedGoldenRetrieverHasDefaultCollarColor(final TestContext context) {
    testUntamedDogHasDefaultCollarColor(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void tamedHuskyCollarColorCanBeChanged(final TestContext context) {
    testTamedDogCollarColorCanBeChanged(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void tamedDachshundCollarColorCanBeChanged(final TestContext context) {
    testTamedDogCollarColorCanBeChanged(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void tamedBeagleCollarColorCanBeChanged(final TestContext context) {
    testTamedDogCollarColorCanBeChanged(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void tamedGoldenRetrieverCollarColorCanBeChanged(final TestContext context) {
    testTamedDogCollarColorCanBeChanged(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void huskyCollarColorPersistsInNbt(final TestContext context) {
    testCollarColorPersistsInNbt(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dachshundCollarColorPersistsInNbt(final TestContext context) {
    testCollarColorPersistsInNbt(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void beagleCollarColorPersistsInNbt(final TestContext context) {
    testCollarColorPersistsInNbt(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void goldenRetrieverCollarColorPersistsInNbt(final TestContext context) {
    testCollarColorPersistsInNbt(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void babyHuskyHasCollarWhenTamed(final TestContext context) {
    testBabyDogHasCollarWhenTamed(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void babyDachshundHasCollarWhenTamed(final TestContext context) {
    testBabyDogHasCollarWhenTamed(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void babyBeagleHasCollarWhenTamed(final TestContext context) {
    testBabyDogHasCollarWhenTamed(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void babyGoldenRetrieverHasCollarWhenTamed(final TestContext context) {
    testBabyDogHasCollarWhenTamed(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void huskyAllDyeColorsWorkOnCollar(final TestContext context) {
    testAllDyeColorsWorkOnCollar(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dachshundAllDyeColorsWorkOnCollar(final TestContext context) {
    testAllDyeColorsWorkOnCollar(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void beagleAllDyeColorsWorkOnCollar(final TestContext context) {
    testAllDyeColorsWorkOnCollar(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void goldenRetrieverAllDyeColorsWorkOnCollar(final TestContext context) {
    testAllDyeColorsWorkOnCollar(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void huskyBredBabyInheritsParentTamedStatus(final TestContext context) {
    testBredBabyInheritsParentTamedStatus(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void dachshundBredBabyInheritsParentTamedStatus(final TestContext context) {
    testBredBabyInheritsParentTamedStatus(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void beagleBredBabyInheritsParentTamedStatus(final TestContext context) {
    testBredBabyInheritsParentTamedStatus(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void goldenRetrieverBredBabyInheritsParentTamedStatus(final TestContext context) {
    testBredBabyInheritsParentTamedStatus(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyBoneIsTamingItem(final TestContext context) {
    testBoneIsTamingItem(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dachshundBoneIsTamingItem(final TestContext context) {
    testBoneIsTamingItem(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void beagleBoneIsTamingItem(final TestContext context) {
    testBoneIsTamingItem(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void goldenRetrieverBoneIsTamingItem(final TestContext context) {
    testBoneIsTamingItem(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyMeatItemsAreBothTamingAndBreeding(final TestContext context) {
    testMeatItemsAreBothTamingAndBreeding(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dachshundMeatItemsAreBothTamingAndBreeding(final TestContext context) {
    testMeatItemsAreBothTamingAndBreeding(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void beagleMeatItemsAreBothTamingAndBreeding(final TestContext context) {
    testMeatItemsAreBothTamingAndBreeding(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void goldenRetrieverMeatItemsAreBothTamingAndBreeding(final TestContext context) {
    testMeatItemsAreBothTamingAndBreeding(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void huskyShakesOnceWhenLeavingWater(final TestContext context) {
    testDogShakesOnceWhenLeavingWater(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dachshundShakesOnceWhenLeavingWater(final TestContext context) {
    testDogShakesOnceWhenLeavingWater(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void beagleShakesOnceWhenLeavingWater(final TestContext context) {
    testDogShakesOnceWhenLeavingWater(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void goldenRetrieverShakesOnceWhenLeavingWater(final TestContext context) {
    testDogShakesOnceWhenLeavingWater(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void huskyShakeProgressDecrementsEachTick(final TestContext context) {
    testShakeProgressDecrementsEachTick(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dachshundShakeProgressDecrementsEachTick(final TestContext context) {
    testShakeProgressDecrementsEachTick(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void beagleShakeProgressDecrementsEachTick(final TestContext context) {
    testShakeProgressDecrementsEachTick(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void goldenRetrieverShakeProgressDecrementsEachTick(final TestContext context) {
    testShakeProgressDecrementsEachTick(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void huskyShakeProgressPersistsInNbt(final TestContext context) {
    testShakeProgressPersistsInNbt(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dachshundShakeProgressPersistsInNbt(final TestContext context) {
    testShakeProgressPersistsInNbt(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void beagleShakeProgressPersistsInNbt(final TestContext context) {
    testShakeProgressPersistsInNbt(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void goldenRetrieverShakeProgressPersistsInNbt(final TestContext context) {
    testShakeProgressPersistsInNbt(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void huskyHeadTiltingStateCanBeTracked(final TestContext context) {
    testHeadTiltingStateCanBeTracked(context, HUSKY_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dachshundHeadTiltingStateCanBeTracked(final TestContext context) {
    testHeadTiltingStateCanBeTracked(context, DACHSHUND_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void beagleHeadTiltingStateCanBeTracked(final TestContext context) {
    testHeadTiltingStateCanBeTracked(context, BEAGLE_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void goldenRetrieverHeadTiltingStateCanBeTracked(final TestContext context) {
    testHeadTiltingStateCanBeTracked(context, GOLDEN_RETRIEVER_DATA);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void huskyOnlyBreedsSameSpecies(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    final DachshundEntity dachshund = new DachshundEntity(ModEntities.DACHSHUND, world);
    dachshund.refreshPositionAndAngles(new BlockPos(1, 1, 0), 0.0f, 0.0f);
    dachshund.setTamed(true, true);
    world.spawnEntity(dachshund);

    context.runAtTick(
        10,
        () -> {
          context.assertFalse(
              husky.canBreedWith(dachshund), "Husky should not breed with Dachshund");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dachshundOnlyBreedsSameSpecies(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final DachshundEntity dachshund = new DachshundEntity(ModEntities.DACHSHUND, world);
    dachshund.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    dachshund.setTamed(true, true);
    world.spawnEntity(dachshund);

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(new BlockPos(1, 1, 0), 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          context.assertFalse(
              dachshund.canBreedWith(husky), "Dachshund should not breed with Husky");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void beagleOnlyBreedsSameSpecies(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final BeagleEntity beagle = new BeagleEntity(ModEntities.BEAGLE, world);
    beagle.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    beagle.setTamed(true, true);
    world.spawnEntity(beagle);

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(new BlockPos(1, 1, 0), 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          context.assertFalse(beagle.canBreedWith(husky), "Beagle should not breed with Husky");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void goldenRetrieverOnlyBreedsSameSpecies(final TestContext context) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final GoldenRetrieverEntity goldenRetriever =
        new GoldenRetrieverEntity(ModEntities.GOLDEN_RETRIEVER, world);
    goldenRetriever.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    goldenRetriever.setTamed(true, true);
    world.spawnEntity(goldenRetriever);

    final HuskyEntity husky = new HuskyEntity(ModEntities.HUSKY, world);
    husky.refreshPositionAndAngles(new BlockPos(1, 1, 0), 0.0f, 0.0f);
    husky.setTamed(true, true);
    world.spawnEntity(husky);

    context.runAtTick(
        10,
        () -> {
          context.assertFalse(
              goldenRetriever.canBreedWith(husky), "Golden Retriever should not breed with Husky");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testDogSpawnsCorrectly(
      final TestContext context, final TestData<T> data) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final T dog = data.factory.apply(data.entityType, world);

    dog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dog);

    context.runAtTick(
        1,
        () -> {
          context.assertTrue(!dog.isRemoved(), "Dog should be alive and present in the world");
          context.assertTrue(
              world.getEntitiesByType(data.entityType, entity -> true).contains(dog),
              "Dog should be in the world's entity list");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testDogIsTameable(
      final TestContext context, final TestData<T> data) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final T dog = data.factory.apply(data.entityType, world);

    dog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dog);

    context.assertTrue(dog instanceof TameableEntity, "Dog must be a TameableEntity");
    context.complete();
  }

  private <T extends UnleashedDogEntity> void testDogCanBeTamed(
      final TestContext context, final TestData<T> data) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final T dog = data.factory.apply(data.entityType, world);

    dog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dog);

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
      final TestContext context, final TestData<T> data) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final T dog = data.factory.apply(data.entityType, world);

    dog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dog);

    context.assertTrue(
        Math.abs(dog.getWidth() - data.expectedWidth) < 0.01f,
        "Dog width should be " + data.expectedWidth);
    context.assertTrue(
        Math.abs(dog.getHeight() - data.expectedHeight) < 0.01f,
        "Dog height should be " + data.expectedHeight);
    context.complete();
  }

  private <T extends UnleashedDogEntity> void testDogHasAnimatableInstanceCache(
      final TestContext context, final TestData<T> data) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final T dog = data.factory.apply(data.entityType, world);

    dog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dog);

    context.assertTrue(
        dog.getAnimatableInstanceCache() != null,
        "Dog should have AnimatableInstanceCache for GeckoLib");
    context.complete();
  }

  private <T extends UnleashedDogEntity> void testUntamedDogHasDefaultCollarColor(
      final TestContext context, final TestData<T> data) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final T dog = data.factory.apply(data.entityType, world);
    dog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dog);

    context.assertFalse(dog.isTamed(), "Dog should not be tamed initially");
    context.assertTrue(
        dog.getCollarColor() == DyeColor.RED, "Untamed dog should have RED collar color");
    context.complete();
  }

  private <T extends UnleashedDogEntity> void testTamedDogCollarColorCanBeChanged(
      final TestContext context, final TestData<T> data) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final T dog = data.factory.apply(data.entityType, world);
    dog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dog);

    context.runAtTick(
        10,
        () -> {
          dog.setTamed(true, true);
          context.assertTrue(dog.isTamed(), "Dog should be tamed");
          context.assertTrue(
              dog.getCollarColor() == DyeColor.RED, "Tamed dog should start with RED collar");

          dog.setCollarColor(DyeColor.BLUE);

          context.runAtTick(
              20,
              () -> {
                context.assertTrue(
                    dog.getCollarColor() == DyeColor.BLUE, "Collar color should change to BLUE");
                context.complete();
              });
        });
  }

  private <T extends UnleashedDogEntity> void testCollarColorPersistsInNbt(
      final TestContext context, final TestData<T> data) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final T dog = data.factory.apply(data.entityType, world);
    dog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dog);

    context.runAtTick(
        10,
        () -> {
          dog.setTamed(true, true);
          dog.setCollarColor(DyeColor.LIME);

          final NbtCompound nbt = new NbtCompound();
          dog.writeCustomDataToNbt(nbt);

          context.assertTrue(nbt.contains("CollarColor"), "NBT should contain CollarColor data");
          context.assertTrue(
              nbt.getInt("CollarColor") == DyeColor.LIME.getId(), "NBT should store LIME color ID");

          final T newDog = data.factory.apply(data.entityType, world);
          newDog.readCustomDataFromNbt(nbt);

          context.runAtTick(
              20,
              () -> {
                context.assertTrue(
                    newDog.getCollarColor() == DyeColor.LIME,
                    "Collar color should persist after NBT load");
                context.complete();
              });
        });
  }

  private <T extends UnleashedDogEntity> void testBabyDogHasCollarWhenTamed(
      final TestContext context, final TestData<T> data) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final T babyDog = data.factory.apply(data.entityType, world);
    babyDog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    babyDog.setBaby(true);
    world.spawnEntity(babyDog);

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(babyDog.isBaby(), "Dog should be a baby");
          context.assertFalse(babyDog.isTamed(), "Baby should not be tamed initially");

          babyDog.setTamed(true, true);
          babyDog.setCollarColor(DyeColor.PINK);

          context.runAtTick(
              20,
              () -> {
                context.assertTrue(babyDog.isTamed(), "Baby should be tamed");
                context.assertTrue(
                    babyDog.getCollarColor() == DyeColor.PINK, "Baby should have PINK collar");
                context.complete();
              });
        });
  }

  private <T extends UnleashedDogEntity> void testAllDyeColorsWorkOnCollar(
      final TestContext context, final TestData<T> data) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final T dog = data.factory.apply(data.entityType, world);
    dog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dog);

    context.runAtTick(
        10,
        () -> {
          dog.setTamed(true, true);

          final DyeColor[] testColors = {
            DyeColor.RED, DyeColor.BLUE, DyeColor.GREEN, DyeColor.YELLOW,
            DyeColor.ORANGE, DyeColor.PURPLE, DyeColor.CYAN, DyeColor.MAGENTA,
            DyeColor.WHITE, DyeColor.BLACK, DyeColor.GRAY, DyeColor.LIGHT_GRAY,
            DyeColor.LIME, DyeColor.PINK, DyeColor.LIGHT_BLUE, DyeColor.BROWN
          };

          for (DyeColor color : testColors) {
            dog.setCollarColor(color);
            context.assertTrue(
                dog.getCollarColor() == color,
                "Collar should accept " + color.getName() + " color");
          }

          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testBredBabyInheritsParentTamedStatus(
      final TestContext context, final TestData<T> data) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final T parent = data.factory.apply(data.entityType, world);
    parent.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(parent);

    context.runAtTick(
        10,
        () -> {
          parent.setTamed(true, true);
          parent.setCollarColor(DyeColor.YELLOW);

          final T otherParent = data.factory.apply(data.entityType, world);
          otherParent.refreshPositionAndAngles(new BlockPos(1, 1, 0), 0.0f, 0.0f);
          otherParent.setTamed(true, true);
          world.spawnEntity(otherParent);

          final UnleashedDogEntity baby =
              (UnleashedDogEntity) parent.createChild(world, otherParent);

          context.runAtTick(
              20,
              () -> {
                context.assertTrue(baby != null, "Baby should be created from breeding");
                context.assertTrue(baby.isTamed(), "Baby should inherit tamed status");
                context.assertTrue(baby.isBaby(), "Created entity should be a baby");
                context.complete();
              });
        });
  }

  private <T extends UnleashedDogEntity> void testBoneIsTamingItem(
      final TestContext context, final TestData<T> data) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final T dog = data.factory.apply(data.entityType, world);
    dog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dog);

    final ItemStack bone = new ItemStack(Items.BONE);

    context.assertTrue(dog.isTamingItem(bone), "Bone should be a taming item");
    context.assertFalse(dog.isBreedingItem(bone), "Bone should not be a breeding item");
    context.complete();
  }

  private <T extends UnleashedDogEntity> void testMeatItemsAreBothTamingAndBreeding(
      final TestContext context, final TestData<T> data) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    final T dog = data.factory.apply(data.entityType, world);
    dog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dog);

    final ItemStack chicken = new ItemStack(Items.CHICKEN);

    context.assertTrue(dog.isTamingItem(chicken), "Chicken should be a taming item");
    context.assertTrue(dog.isBreedingItem(chicken), "Chicken should be a breeding item");
    context.complete();
  }

  private <T extends UnleashedDogEntity> void testDogShakesOnceWhenLeavingWater(
      final TestContext context, final TestData<T> data) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();
    final T dog = data.factory.apply(data.entityType, world);
    dog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dog);

    context.runAtTick(
        5,
        () -> {
          context.assertTrue(dog.getShakeProgress() == 0, "Shake progress should start at 0");
          context.assertFalse(dog.isShaking(), "Dog should not be shaking initially");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testShakeProgressDecrementsEachTick(
      final TestContext context, final TestData<T> data) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();
    final T dog = data.factory.apply(data.entityType, world);
    dog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dog);

    context.assertTrue(dog.getShakeProgress() == 0, "Initial shake progress should be 0");
    context.complete();
  }

  private <T extends UnleashedDogEntity> void testShakeProgressPersistsInNbt(
      final TestContext context, final TestData<T> data) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();
    final T dog = data.factory.apply(data.entityType, world);
    dog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dog);

    context.runAtTick(
        5,
        () -> {
          final NbtCompound nbt = new NbtCompound();
          dog.writeCustomDataToNbt(nbt);

          context.assertTrue(
              nbt.contains("ShakeProgress"), "NBT should contain ShakeProgress data");
          context.assertTrue(nbt.contains("WasInWater"), "NBT should contain WasInWater data");

          final T newDog = data.factory.apply(data.entityType, world);
          newDog.readCustomDataFromNbt(nbt);

          context.assertTrue(
              newDog.getShakeProgress() == dog.getShakeProgress(),
              "Shake progress should persist after NBT load");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testHeadTiltingStateCanBeTracked(
      final TestContext context, final TestData<T> data) {
    final BlockPos spawnPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();
    final T dog = data.factory.apply(data.entityType, world);
    dog.refreshPositionAndAngles(spawnPos, 0.0f, 0.0f);
    world.spawnEntity(dog);

    context.runAtTick(
        5,
        () -> {
          context.assertFalse(dog.isHeadTilting(), "Dog should not be tilting head initially");
          context.complete();
        });
  }

  private record TestData<T extends UnleashedDogEntity>(
      EntityType<T> entityType,
      BiFunction<EntityType<? extends TameableEntity>, World, T> factory,
      float expectedWidth,
      float expectedHeight) {}
}
