package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
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

/**
 * Core dog-entity gametests, fanned out across every breed in {@link DogTestData#getAllBreeds()}
 * via {@link CustomTestProvider}. Adding a sixth breed to {@code getAllBreeds()} automatically
 * extends every test below to the new breed with no further edits required. Each generator names
 * its child tests {@code dogentitycoretest.<behavior>.<breed-id>} so the gametest XML report keeps
 * per-breed granularity for failure diagnosis.
 *
 * <p>See gametest skill rule on {@code @CustomTestProvider} (Yarn name for what Mojang and Forge
 * docs call {@code @GameTestGenerator}).
 */
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

  /**
   * Functional interface for a per-breed gametest body. Equivalent to {@code
   * BiConsumer<TestContext, DogTestData<? extends UnleashedDogEntity>>} but expressed as a named
   * SAM so generator call sites can pass method references like {@code this::testDogCanBeTamed}
   * without explicit casts.
   */
  @FunctionalInterface
  private interface PerBreedBody {
    void run(TestContext context, DogTestData<? extends UnleashedDogEntity> data);
  }

  private <T extends UnleashedDogEntity> void testDogSpawnsCorrectly(
      final TestContext context, final DogTestData<T> data) {
    final ServerWorld world = context.getWorld();
    final T dog = DogTestHelper.spawnDog(context, data);

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

  private <T extends UnleashedDogEntity> void testDogCanBeTamed(
      final TestContext context, final DogTestData<T> data) {
    final T dog = DogTestHelper.spawnDog(context, data);

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

  private <T extends UnleashedDogEntity> void testCollarColorPersistsInNbt(
      final TestContext context, final DogTestData<T> data) {
    final ServerWorld world = context.getWorld();
    final T dog = DogTestHelper.spawnTamedDog(context, data);

    dog.setCollarColor(DyeColor.LIME);

    final NbtCompound nbt = new NbtCompound();
    dog.writeCustomDataToNbt(nbt);

    context.assertTrue(
        nbt.contains(ModNbtKeys.COLLAR_COLOR), "NBT should contain CollarColor data");
    context.assertTrue(
        nbt.getInt(ModNbtKeys.COLLAR_COLOR) == DyeColor.LIME.getId(),
        "NBT should store LIME color ID");

    final T newDog = data.factory().apply(world);
    newDog.readCustomDataFromNbt(nbt);

    context.assertTrue(
        newDog.getCollarColor() == DyeColor.LIME, "Collar color should persist after NBT load");
    context.complete();
  }
}
