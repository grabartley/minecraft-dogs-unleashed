package com.grahambartley.gametest;

import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.gametest.util.DogTestData;
import com.grahambartley.gametest.util.DogTestHelper;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

public final class DogEntityBehaviorTest implements FabricGameTest {

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void tamedHuskyCollarColorCanBeChanged(TestContext context) {
    testTamedDogCollarColorCanBeChanged(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void tamedDachshundCollarColorCanBeChanged(TestContext context) {
    testTamedDogCollarColorCanBeChanged(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void tamedBeagleCollarColorCanBeChanged(TestContext context) {
    testTamedDogCollarColorCanBeChanged(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void tamedGoldenRetrieverCollarColorCanBeChanged(TestContext context) {
    testTamedDogCollarColorCanBeChanged(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void tamedShibaInuCollarColorCanBeChanged(TestContext context) {
    testTamedDogCollarColorCanBeChanged(context, DogTestData.SHIBA_INU);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void babyHuskyHasCollarWhenTamed(TestContext context) {
    testBabyDogHasCollarWhenTamed(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void babyDachshundHasCollarWhenTamed(TestContext context) {
    testBabyDogHasCollarWhenTamed(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void babyBeagleHasCollarWhenTamed(TestContext context) {
    testBabyDogHasCollarWhenTamed(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void babyGoldenRetrieverHasCollarWhenTamed(TestContext context) {
    testBabyDogHasCollarWhenTamed(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void babyShibaInuHasCollarWhenTamed(TestContext context) {
    testBabyDogHasCollarWhenTamed(context, DogTestData.SHIBA_INU);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void huskyAllDyeColorsWorkOnCollar(TestContext context) {
    testAllDyeColorsWorkOnCollar(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dachshundAllDyeColorsWorkOnCollar(TestContext context) {
    testAllDyeColorsWorkOnCollar(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void beagleAllDyeColorsWorkOnCollar(TestContext context) {
    testAllDyeColorsWorkOnCollar(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void goldenRetrieverAllDyeColorsWorkOnCollar(TestContext context) {
    testAllDyeColorsWorkOnCollar(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void shibaInuAllDyeColorsWorkOnCollar(TestContext context) {
    testAllDyeColorsWorkOnCollar(context, DogTestData.SHIBA_INU);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void huskyBredBabyInheritsParentTamedStatus(TestContext context) {
    testBredBabyInheritsParentTamedStatus(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void dachshundBredBabyInheritsParentTamedStatus(TestContext context) {
    testBredBabyInheritsParentTamedStatus(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void beagleBredBabyInheritsParentTamedStatus(TestContext context) {
    testBredBabyInheritsParentTamedStatus(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void goldenRetrieverBredBabyInheritsParentTamedStatus(TestContext context) {
    testBredBabyInheritsParentTamedStatus(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void shibaInuBredBabyInheritsParentTamedStatus(TestContext context) {
    testBredBabyInheritsParentTamedStatus(context, DogTestData.SHIBA_INU);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyBoneIsTamingItem(TestContext context) {
    testBoneIsTamingItem(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dachshundBoneIsTamingItem(TestContext context) {
    testBoneIsTamingItem(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void beagleBoneIsTamingItem(TestContext context) {
    testBoneIsTamingItem(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void goldenRetrieverBoneIsTamingItem(TestContext context) {
    testBoneIsTamingItem(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void shibaInuBoneIsTamingItem(TestContext context) {
    testBoneIsTamingItem(context, DogTestData.SHIBA_INU);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyMeatItemsAreBothTamingAndBreeding(TestContext context) {
    testMeatItemsAreBothTamingAndBreeding(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dachshundMeatItemsAreBothTamingAndBreeding(TestContext context) {
    testMeatItemsAreBothTamingAndBreeding(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void beagleMeatItemsAreBothTamingAndBreeding(TestContext context) {
    testMeatItemsAreBothTamingAndBreeding(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void goldenRetrieverMeatItemsAreBothTamingAndBreeding(TestContext context) {
    testMeatItemsAreBothTamingAndBreeding(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void shibaInuMeatItemsAreBothTamingAndBreeding(TestContext context) {
    testMeatItemsAreBothTamingAndBreeding(context, DogTestData.SHIBA_INU);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void huskyShakeProgressPersistsInNbt(TestContext context) {
    testShakeProgressPersistsInNbt(context, DogTestData.HUSKY);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dachshundShakeProgressPersistsInNbt(TestContext context) {
    testShakeProgressPersistsInNbt(context, DogTestData.DACHSHUND);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void beagleShakeProgressPersistsInNbt(TestContext context) {
    testShakeProgressPersistsInNbt(context, DogTestData.BEAGLE);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void goldenRetrieverShakeProgressPersistsInNbt(TestContext context) {
    testShakeProgressPersistsInNbt(context, DogTestData.GOLDEN_RETRIEVER);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void shibaInuShakeProgressPersistsInNbt(TestContext context) {
    testShakeProgressPersistsInNbt(context, DogTestData.SHIBA_INU);
  }

  private <T extends UnleashedDogEntity> void testTamedDogCollarColorCanBeChanged(
      TestContext context, DogTestData<T> data) {
    T dog = DogTestHelper.spawnTamedDog(context, data);

    context.runAtTick(
        10,
        () -> {
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

  private <T extends UnleashedDogEntity> void testBabyDogHasCollarWhenTamed(
      TestContext context, DogTestData<T> data) {
    T babyDog = DogTestHelper.spawnDog(context, data);
    babyDog.setBaby(true);

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
      TestContext context, DogTestData<T> data) {
    T dog = DogTestHelper.spawnTamedDog(context, data);

    context.runAtTick(
        10,
        () -> {
          DyeColor[] testColors = {
            DyeColor.RED,
            DyeColor.BLUE,
            DyeColor.GREEN,
            DyeColor.YELLOW,
            DyeColor.ORANGE,
            DyeColor.PURPLE,
            DyeColor.CYAN,
            DyeColor.MAGENTA,
            DyeColor.WHITE,
            DyeColor.BLACK,
            DyeColor.GRAY,
            DyeColor.LIGHT_GRAY,
            DyeColor.LIME,
            DyeColor.PINK,
            DyeColor.LIGHT_BLUE,
            DyeColor.BROWN
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
      TestContext context, DogTestData<T> data) {
    ServerWorld world = context.getWorld();
    UUID ownerUuid = UUID.randomUUID();
    T parent = DogTestHelper.spawnTamedDog(context, data, new BlockPos(0, 1, 0), ownerUuid);
    parent.setCollarColor(DyeColor.YELLOW);

    context.runAtTick(
        10,
        () -> {
          T otherParent =
              DogTestHelper.spawnTamedDog(context, data, new BlockPos(1, 1, 0), ownerUuid);
          UnleashedDogEntity baby = (UnleashedDogEntity) parent.createChild(world, otherParent);

          context.runAtTick(
              20,
              () -> {
                context.assertTrue(baby != null, "Baby should be created from breeding");
                context.assertTrue(
                    baby.isTamed(), "Baby should inherit tamed status from parents with owner");
                context.assertTrue(baby.isBaby(), "Created entity should be a baby");
                context.assertTrue(
                    ownerUuid.equals(baby.getOwnerUuid()), "Baby should inherit owner UUID");
                context.complete();
              });
        });
  }

  private <T extends UnleashedDogEntity> void testBoneIsTamingItem(
      TestContext context, DogTestData<T> data) {
    T dog = DogTestHelper.spawnDog(context, data);
    ItemStack bone = new ItemStack(Items.BONE);

    context.assertTrue(dog.isTamingItem(bone), "Bone should be a taming item");
    context.assertFalse(dog.isBreedingItem(bone), "Bone should not be a breeding item");
    context.complete();
  }

  private <T extends UnleashedDogEntity> void testMeatItemsAreBothTamingAndBreeding(
      TestContext context, DogTestData<T> data) {
    T dog = DogTestHelper.spawnDog(context, data);
    ItemStack chicken = new ItemStack(Items.CHICKEN);

    context.assertTrue(dog.isTamingItem(chicken), "Chicken should be a taming item");
    context.assertTrue(dog.isBreedingItem(chicken), "Chicken should be a breeding item");
    context.complete();
  }

  private <T extends UnleashedDogEntity> void testShakeProgressPersistsInNbt(
      TestContext context, DogTestData<T> data) {
    ServerWorld world = context.getWorld();
    T dog = DogTestHelper.spawnDog(context, data);

    context.runAtTick(
        5,
        () -> {
          NbtCompound nbt = new NbtCompound();
          dog.writeCustomDataToNbt(nbt);

          context.assertTrue(
              nbt.contains("ShakeProgress"), "NBT should contain ShakeProgress data");
          context.assertTrue(nbt.contains("WasInWater"), "NBT should contain WasInWater data");

          T newDog = data.factory().apply(world);
          newDog.readCustomDataFromNbt(nbt);

          context.assertTrue(
              newDog.getShakeProgress() == dog.getShakeProgress(),
              "Shake progress should persist after NBT load");
          context.complete();
        });
  }
}
