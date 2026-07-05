package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

/**
 * Behavioral gametests fanned out across every breed via {@link CustomTestProvider}. Each generator
 * produces one {@link TestFunction} per breed in {@link DogTestData#getAllBreeds()}; adding a sixth
 * breed automatically extends every test below.
 */
public final class DogEntityBehaviorTest implements FabricGameTest {

  @CustomTestProvider
  public List<TestFunction> tamedCollarColorCanBeChangedPerBreed() {
    return generatePerBreed(
        "tamedCollarColorCanBeChanged", 100, this::testTamedDogCollarColorCanBeChanged);
  }

  @CustomTestProvider
  public List<TestFunction> babyHasCollarWhenTamedPerBreed() {
    return generatePerBreed("babyHasCollarWhenTamed", 100, this::testBabyDogHasCollarWhenTamed);
  }

  @CustomTestProvider
  public List<TestFunction> allDyeColorsWorkOnCollarPerBreed() {
    return generatePerBreed("allDyeColorsWorkOnCollar", 100, this::testAllDyeColorsWorkOnCollar);
  }

  @CustomTestProvider
  public List<TestFunction> bredBabyInheritsParentTamedStatusPerBreed() {
    return generatePerBreed(
        "bredBabyInheritsParentTamedStatus", 200, this::testBredBabyInheritsParentTamedStatus);
  }

  @CustomTestProvider
  public List<TestFunction> boneIsTamingItemPerBreed() {
    return generatePerBreed("boneIsTamingItem", 100, this::testBoneIsTamingItem);
  }

  @CustomTestProvider
  public List<TestFunction> meatItemsAreBothTamingAndBreedingPerBreed() {
    return generatePerBreed(
        "meatItemsAreBothTamingAndBreeding", 100, this::testMeatItemsAreBothTamingAndBreeding);
  }

  @CustomTestProvider
  public List<TestFunction> shakeProgressPersistsInNbtPerBreed() {
    return generatePerBreed(
        "shakeProgressPersistsInNbt", 100, this::testShakeProgressPersistsInNbt);
  }

  private List<TestFunction> generatePerBreed(
      final String behavior, final int tickLimit, final PerBreedBody body) {
    return DogTestData.getAllBreeds().stream()
        .map(
            data ->
                new TestFunction(
                    "defaultBatch",
                    "dogentitybehaviortest." + behavior + "." + data.breed().serializedId(),
                    FabricGameTest.EMPTY_STRUCTURE,
                    tickLimit,
                    0L,
                    true,
                    ctx -> body.run(ctx, data)))
        .toList();
  }

  @FunctionalInterface
  private interface PerBreedBody {
    void run(TestContext context, DogTestData<? extends UnleashedDogEntity> data);
  }

  private <T extends UnleashedDogEntity> void testTamedDogCollarColorCanBeChanged(
      final TestContext context, final DogTestData<T> data) {
    final T dog = DogTestHelper.spawnTamedDog(context, data);

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
      final TestContext context, final DogTestData<T> data) {
    final T babyDog = DogTestHelper.spawnDog(context, data);
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
      final TestContext context, final DogTestData<T> data) {
    final T dog = DogTestHelper.spawnTamedDog(context, data);

    context.runAtTick(
        10,
        () -> {
          final DyeColor[] testColors = {
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

          for (final DyeColor color : testColors) {
            dog.setCollarColor(color);
            context.assertTrue(
                dog.getCollarColor() == color,
                "Collar should accept " + color.getName() + " color");
          }

          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testBredBabyInheritsParentTamedStatus(
      final TestContext context, final DogTestData<T> data) {
    final ServerWorld world = context.getWorld();
    final UUID ownerUuid = UUID.randomUUID();
    final T parent = DogTestHelper.spawnTamedDog(context, data, new BlockPos(0, 1, 0), ownerUuid);
    parent.setCollarColor(DyeColor.YELLOW);

    context.runAtTick(
        10,
        () -> {
          final T otherParent =
              DogTestHelper.spawnTamedDog(context, data, new BlockPos(1, 1, 0), ownerUuid);
          final UnleashedDogEntity baby =
              (UnleashedDogEntity) parent.createChild(world, otherParent);

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
      final TestContext context, final DogTestData<T> data) {
    final T dog = DogTestHelper.spawnDog(context, data);
    final ItemStack bone = new ItemStack(Items.BONE);

    context.assertTrue(dog.isTamingItem(bone), "Bone should be a taming item");
    context.assertFalse(dog.isBreedingItem(bone), "Bone should not be a breeding item");
    context.complete();
  }

  private <T extends UnleashedDogEntity> void testMeatItemsAreBothTamingAndBreeding(
      final TestContext context, final DogTestData<T> data) {
    final T dog = DogTestHelper.spawnDog(context, data);
    final ItemStack chicken = new ItemStack(Items.CHICKEN);

    context.assertTrue(dog.isTamingItem(chicken), "Chicken should be a taming item");
    context.assertTrue(dog.isBreedingItem(chicken), "Chicken should be a breeding item");
    context.complete();
  }

  private <T extends UnleashedDogEntity> void testShakeProgressPersistsInNbt(
      final TestContext context, final DogTestData<T> data) {
    final ServerWorld world = context.getWorld();
    final T dog = DogTestHelper.spawnDog(context, data);

    context.runAtTick(
        5,
        () -> {
          final NbtCompound nbt = new NbtCompound();
          dog.writeCustomDataToNbt(nbt);

          context.assertTrue(
              nbt.contains("ShakeProgress"), "NBT should contain ShakeProgress data");
          context.assertTrue(nbt.contains("WasInWater"), "NBT should contain WasInWater data");

          final T newDog = data.factory().apply(world);
          newDog.readCustomDataFromNbt(nbt);

          context.assertTrue(
              newDog.getShakeProgress() == dog.getShakeProgress(),
              "Shake progress should persist after NBT load");
          context.complete();
        });
  }
}
