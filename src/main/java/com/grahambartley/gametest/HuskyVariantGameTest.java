package com.grahambartley.gametest;

import com.grahambartley.entity.HuskyEntity;
import com.grahambartley.entity.variant.HuskyCoat;
import com.grahambartley.entity.variant.HuskyEyeColor;
import com.grahambartley.gametest.util.DogTestData;
import com.grahambartley.gametest.util.DogTestHelper;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class HuskyVariantGameTest implements FabricGameTest {

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskySpawnHasCoatVariant(TestContext context) {
    final HuskyEntity husky = DogTestHelper.spawnDog(context, DogTestData.HUSKY);

    context.assertTrue(
        husky.getCoatVariant() != null, "Husky coat variant must not be null after spawn");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskySpawnHasEyeColorVariant(TestContext context) {
    final HuskyEntity husky = DogTestHelper.spawnDog(context, DogTestData.HUSKY);

    context.assertTrue(
        husky.getEyeColorVariant() != null, "Husky eye color variant must not be null after spawn");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyCoatVariantIsValidEnumValue(TestContext context) {
    final HuskyEntity husky = DogTestHelper.spawnDog(context, DogTestData.HUSKY);
    final HuskyCoat coat = husky.getCoatVariant();

    boolean isValid = false;
    for (final HuskyCoat value : HuskyCoat.values()) {
      if (value == coat) {
        isValid = true;
        break;
      }
    }

    context.assertTrue(isValid, "Husky coat variant must be a valid HuskyCoat enum value");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyEyeColorVariantIsValidEnumValue(TestContext context) {
    final HuskyEntity husky = DogTestHelper.spawnDog(context, DogTestData.HUSKY);
    final HuskyEyeColor eyeColor = husky.getEyeColorVariant();

    boolean isValid = false;
    for (final HuskyEyeColor value : HuskyEyeColor.values()) {
      if (value == eyeColor) {
        isValid = true;
        break;
      }
    }

    context.assertTrue(isValid, "Husky eye color must be a valid HuskyEyeColor enum value");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyCoatVariantPersistsThroughNbt(TestContext context) {
    final ServerWorld world = context.getWorld();
    final HuskyEntity husky = DogTestHelper.spawnDog(context, DogTestData.HUSKY);
    final HuskyCoat originalCoat = husky.getCoatVariant();

    final NbtCompound nbt = new NbtCompound();
    husky.writeCustomDataToNbt(nbt);

    context.assertTrue(nbt.contains("CoatVariant"), "NBT must contain CoatVariant key");

    final HuskyEntity loaded = DogTestData.HUSKY.factory().apply(world);
    loaded.readCustomDataFromNbt(nbt);

    context.assertTrue(
        loaded.getCoatVariant() == originalCoat,
        "Coat variant must be the same after NBT save and load");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyEyeColorVariantPersistsThroughNbt(TestContext context) {
    final ServerWorld world = context.getWorld();
    final HuskyEntity husky = DogTestHelper.spawnDog(context, DogTestData.HUSKY);
    final HuskyEyeColor originalEyeColor = husky.getEyeColorVariant();

    final NbtCompound nbt = new NbtCompound();
    husky.writeCustomDataToNbt(nbt);

    context.assertTrue(nbt.contains("EyeColorVariant"), "NBT must contain EyeColorVariant key");

    final HuskyEntity loaded = DogTestData.HUSKY.factory().apply(world);
    loaded.readCustomDataFromNbt(nbt);

    context.assertTrue(
        loaded.getEyeColorVariant() == originalEyeColor,
        "Eye color variant must be the same after NBT save and load");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyBabyHasVariantsAssigned(TestContext context) {
    final ServerWorld world = context.getWorld();
    final HuskyEntity parent = DogTestHelper.spawnDog(context, DogTestData.HUSKY);

    context.runAtTick(
        5,
        () -> {
          final HuskyEntity baby = (HuskyEntity) parent.createChild(world, parent);

          context.assertTrue(baby != null, "Baby husky must be created");
          context.assertTrue(
              baby.getCoatVariant() != null, "Baby husky must have a coat variant assigned");
          context.assertTrue(
              baby.getEyeColorVariant() != null,
              "Baby husky must have an eye color variant assigned");
          context.complete();
        });
  }
}
