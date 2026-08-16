package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.math.BlockPos;

/**
 * Breed-specific contracts: per-breed attributes (max health, movement speed, attack damage) and
 * the same-breed {@code createChild} pure-child guarantee. Per-breed bodies fan out via {@link
 * CustomTestProvider} over {@link DogTestData#getAllBreeds()}; the mixed-pair check is a fixed pair
 * of breeds and stays a plain {@link GameTest}, with the full genome contract covered by {@code
 * DogCrossBreedingGameTest}.
 */
public final class DogEntityBreedSpecificTest implements FabricGameTest {

  @CustomTestProvider
  public List<TestFunction> hasCorrectAttributesPerBreed() {
    return generatePerBreed("hasCorrectAttributes", 100, this::testDogHasCorrectAttributes);
  }

  @CustomTestProvider
  public List<TestFunction> createsCorrectBabyPerBreed() {
    return generatePerBreed("createsCorrectBaby", 100, this::testDogCreatesCorrectBaby);
  }

  private List<TestFunction> generatePerBreed(
      final String behavior, final int tickLimit, final PerBreedBody body) {
    return DogTestData.getAllBreeds().stream()
        .map(
            data ->
                new TestFunction(
                    "defaultBatch",
                    "dogentitybreedspecifictest." + behavior + "." + data.breed().serializedId(),
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

  private void testDogHasCorrectAttributes(final TestContext context, final DogTestData data) {
    final UnleashedDogEntity dog = DogTestHelper.spawnDog(context, data);

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

  private void testDogCreatesCorrectBaby(final TestContext context, final DogTestData data) {
    final ServerWorld world = context.getWorld();
    final UnleashedDogEntity parent1 =
        DogTestHelper.spawnTamedDog(context, data, new BlockPos(0, 1, 0));
    final UnleashedDogEntity parent2 =
        DogTestHelper.spawnTamedDog(context, data, new BlockPos(1, 1, 0));

    context.runAtTick(
        10,
        () -> {
          final UnleashedDogEntity baby = (UnleashedDogEntity) parent1.createChild(world, parent2);

          context.assertTrue(
              baby != null, "Baby should be created from two " + data.breed().serializedId() + "s");
          context.assertTrue(
              baby.getType() == data.entityType(), "Baby should be same type as parents");
          context.assertTrue(baby.isBaby(), "Created entity should be a baby");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void mixedPairProducesCrossBreedBaby(TestContext context) {
    final UnleashedDogEntity husky =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, new BlockPos(0, 1, 0));
    final UnleashedDogEntity beagle =
        DogTestHelper.spawnTamedDog(context, DogTestData.BEAGLE, new BlockPos(1, 1, 0));

    final UnleashedDogEntity baby =
        (UnleashedDogEntity) husky.createChild(context.getWorld(), beagle);

    context.assertTrue(baby != null, "A mixed pair should produce a puppy");
    context.assertTrue(
        baby.getBreed() == UnleashedDogBreed.CROSS_BREED,
        "A mixed pair should produce a cross-breed puppy");
    context.assertTrue(
        baby.getType() == ModEntities.CROSS_BREED,
        "A cross-breed puppy should use the cross-breed entity type");
    context.complete();
  }
}
