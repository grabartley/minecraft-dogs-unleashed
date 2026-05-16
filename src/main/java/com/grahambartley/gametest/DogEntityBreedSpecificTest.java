package com.grahambartley.gametest;

import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.gametest.util.DogTestData;
import com.grahambartley.gametest.util.DogTestHelper;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class DogEntityBreedSpecificTest implements FabricGameTest {

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyHasCorrectAttributes(TestContext context) {
    testDogHasCorrectAttributes(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dachshundHasCorrectAttributes(TestContext context) {
    testDogHasCorrectAttributes(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void beagleHasCorrectAttributes(TestContext context) {
    testDogHasCorrectAttributes(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void goldenRetrieverHasCorrectAttributes(TestContext context) {
    testDogHasCorrectAttributes(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void shibaInuHasCorrectAttributes(TestContext context) {
    testDogHasCorrectAttributes(context, DogTestData.SHIBA_INU);
  }

  private <T extends UnleashedDogEntity> void testDogHasCorrectAttributes(
      TestContext context, DogTestData<T> data) {
    T dog = DogTestHelper.spawnDog(context, data);

    context.assertTrue(
        dog.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) == data.expectedMaxHealth(),
        data.breed().serializedId() + " max health should be " + data.expectedMaxHealth());
    context.assertTrue(
        dog.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)
            == data.expectedMovementSpeed(),
        data.breed().serializedId() + " movement speed should be " + data.expectedMovementSpeed());
    context.assertTrue(
        dog.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)
            == data.expectedAttackDamage(),
        data.breed().serializedId() + " attack damage should be " + data.expectedAttackDamage());

    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void huskyCreatesHuskyBaby(TestContext context) {
    testDogCreatesCorrectBaby(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dachshundCreatesDachshundBaby(TestContext context) {
    testDogCreatesCorrectBaby(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void beagleCreatesBeagleBaby(TestContext context) {
    testDogCreatesCorrectBaby(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void goldenRetrieverCreatesGoldenRetrieverBaby(TestContext context) {
    testDogCreatesCorrectBaby(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void shibaInuCreatesShibaInuBaby(TestContext context) {
    testDogCreatesCorrectBaby(context, DogTestData.SHIBA_INU);
  }

  private <T extends UnleashedDogEntity> void testDogCreatesCorrectBaby(
      TestContext context, DogTestData<T> data) {
    ServerWorld world = context.getWorld();
    T parent1 = DogTestHelper.spawnTamedDog(context, data, new BlockPos(0, 1, 0));
    T parent2 = DogTestHelper.spawnTamedDog(context, data, new BlockPos(1, 1, 0));

    context.runAtTick(
        10,
        () -> {
          @SuppressWarnings("unchecked")
          T baby = (T) parent1.createChild(world, parent2);

          context.assertTrue(
              baby != null, "Baby should be created from two " + data.breed().serializedId() + "s");
          context.assertTrue(
              baby.getType() == data.entityType(), "Baby should be same type as parents");
          context.assertTrue(baby.isBaby(), "Created entity should be a baby");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dogOnlyBreedsSameSpecies(TestContext context) {
    UnleashedDogEntity husky =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, new BlockPos(0, 1, 0));
    UnleashedDogEntity beagle =
        DogTestHelper.spawnTamedDog(context, DogTestData.BEAGLE, new BlockPos(1, 1, 0));

    UnleashedDogEntity baby = (UnleashedDogEntity) husky.createChild(context.getWorld(), beagle);

    context.assertTrue(baby == null, "Different species should not be able to breed");
    context.complete();
  }
}
