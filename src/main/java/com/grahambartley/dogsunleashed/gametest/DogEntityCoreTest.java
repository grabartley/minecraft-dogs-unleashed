package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.DogTraits;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.variant.DogCoats;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.DyeColor;

public final class DogEntityCoreTest implements FabricGameTest {

  @CustomTestProvider
  public List<TestFunction> spawnsCorrectlyPerBreed() {
    return generatePerBreed("spawnsCorrectly", 20, this::testDogSpawnsCorrectly);
  }

  @CustomTestProvider
  public List<TestFunction> canBeTamedPerBreed() {
    return generatePerBreed("canBeTamed", 100, this::testDogCanBeTamed);
  }

  @CustomTestProvider
  public List<TestFunction> collarColorPersistsInNbtPerBreed() {
    return generatePerBreed("collarColorPersistsInNbt", 100, this::testCollarColorPersistsInNbt);
  }

  @CustomTestProvider
  public List<TestFunction> traitsPersistInNbtPerBreed() {
    return generatePerBreed("traitsPersistInNbt", 100, this::testTraitsPersistInNbt);
  }

  @CustomTestProvider
  public List<TestFunction> variantGettersMatchBreedCapabilitiesPerBreed() {
    return generatePerBreed(
        "variantGettersMatchBreedCapabilities", 20, this::testVariantGettersMatchBreedCapabilities);
  }

  private List<TestFunction> generatePerBreed(
      final String behavior, final int tickLimit, final PerBreedBody body) {
    return DogTestData.getAllBreeds().stream()
        .map(
            data ->
                new TestFunction(
                    "defaultBatch",
                    "dogentitycoretest." + behavior + "." + data.breed().serializedId(),
                    FabricGameTest.EMPTY_STRUCTURE,
                    tickLimit,
                    0L,
                    true,
                    ctx -> body.run(ctx, data)))
        .toList();
  }

  @FunctionalInterface
  private interface PerBreedBody {
    void run(TestContext context, DogTestData data);
  }

  private void testDogSpawnsCorrectly(final TestContext context, final DogTestData data) {
    final ServerWorld world = context.getWorld();
    final UnleashedDogEntity dog = DogTestHelper.spawnDog(context, data);

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

  private void testDogCanBeTamed(final TestContext context, final DogTestData data) {
    final UnleashedDogEntity dog = DogTestHelper.spawnDog(context, data);

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

  private void testCollarColorPersistsInNbt(final TestContext context, final DogTestData data) {
    final ServerWorld world = context.getWorld();
    final UnleashedDogEntity dog = DogTestHelper.spawnTamedDog(context, data);

    dog.setCollarColor(DyeColor.LIME);

    final NbtCompound nbt = new NbtCompound();
    dog.writeCustomDataToNbt(nbt);

    context.assertTrue(
        nbt.contains(ModNbtKeys.COLLAR_COLOR), "NBT should contain CollarColor data");
    context.assertTrue(
        nbt.getInt(ModNbtKeys.COLLAR_COLOR) == DyeColor.LIME.getId(),
        "NBT should store LIME color ID");

    final UnleashedDogEntity newDog = data.factory().apply(world);
    newDog.readCustomDataFromNbt(nbt);

    context.assertTrue(
        newDog.getCollarColor() == DyeColor.LIME, "Collar color should persist after NBT load");
    context.complete();
  }

  private void testTraitsPersistInNbt(final TestContext context, final DogTestData data) {
    final ServerWorld world = context.getWorld();
    final UnleashedDogEntity dog = DogTestHelper.spawnDog(context, data);
    final DogTraits appliedTraits = new DogTraits(data.breed(), 1, 1);
    dog.applyTraits(appliedTraits);

    context.assertTrue(
        appliedTraits.equals(dog.getTraits()), "Applied traits should read back unchanged");

    final NbtCompound nbt = new NbtCompound();
    dog.writeCustomDataToNbt(nbt);
    final UnleashedDogEntity newDog = data.factory().apply(world);
    newDog.readCustomDataFromNbt(nbt);

    final DogTraits expectedTraits =
        new DogTraits(
            data.breed(),
            DogCoats.hasCoatVariants(data.breed()) ? 1 : 0,
            data.breed().hasEyeColorVariants() ? 1 : 0);
    context.assertTrue(
        expectedTraits.equals(newDog.getTraits()),
        "Traits should persist through NBT for variants the breed supports, but were "
            + newDog.getTraits());
    context.complete();
  }

  private void testVariantGettersMatchBreedCapabilities(
      final TestContext context, final DogTestData data) {
    final UnleashedDogEntity dog = DogTestHelper.spawnDog(context, data);

    context.assertTrue(
        DogCoats.hasCoatVariants(data.breed()) == (dog.getCoatVariant() != null),
        "Coat variant getter must resolve exactly for breeds with coat variants");
    context.assertTrue(
        data.breed().hasEyeColorVariants() == (dog.getEyeColorVariant() != null),
        "Eye color getter must resolve exactly for breeds with eye color variants");
    context.complete();
  }
}
