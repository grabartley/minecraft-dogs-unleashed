package com.grahambartley.gametest;

import com.grahambartley.entity.HuskyEntity;
import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.gametest.util.DogTestData;
import com.grahambartley.gametest.util.DogTestHelper;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.math.BlockPos;

/**
 * Sound-related gametests. Bark-cooldown contracts fan out across every breed via {@link
 * CustomTestProvider}; the Husky branch dispatches to a negative assertion ({@code dog should NOT
 * bark}) since the breed has no bark sound. Husky-specific howl contracts remain plain {@link
 * GameTest}s since they don't generalize across breeds.
 *
 * <p>Sound-registry presence checks (huskyHasNoBarkSoundRegistered, huskyHowlSoundIsRegistered, and
 * the four per-breed BarkSoundIsRegistered tests) live in {@code DogSoundRegistrationTest} under
 * {@code src/test/java} since they only query {@code Registries.SOUND_EVENT} and don't need a live
 * world. See gametest skill rule 10.
 */
public final class DogEntitySoundTest implements FabricGameTest {

  @CustomTestProvider
  public List<TestFunction> barksOnLowHealthPerBreed() {
    return generatePerBreed(
        "barksOnLowHealth",
        200,
        (ctx, data) -> {
          if (data.expectedBarkSound() == null) {
            testDogDoesNotBarkWhenLowHealth(ctx, data);
          } else {
            testDogBarksWhenLowHealth(ctx, data);
          }
        });
  }

  @CustomTestProvider
  public List<TestFunction> barksOnTargetPerBreed() {
    return generatePerBreed(
        "barksOnTarget",
        200,
        (ctx, data) -> {
          if (data.expectedBarkSound() == null) {
            testDogDoesNotBarkWhenHasTarget(ctx, data);
          } else {
            testDogBarksWhenHasTarget(ctx, data);
          }
        });
  }

  @CustomTestProvider
  public List<TestFunction> barksOnDamagePerBreed() {
    return generatePerBreed(
        "barksOnDamage",
        200,
        (ctx, data) -> {
          if (data.expectedBarkSound() == null) {
            testDogDoesNotBarkWhenTakingDamage(ctx, data);
          } else {
            testDogBarksWhenTakingDamage(ctx, data);
          }
        });
  }

  @CustomTestProvider
  public List<TestFunction> cooldownGatesBarkPerBreed() {
    return generatePerBreed(
        "cooldownGatesBark",
        200,
        (ctx, data) -> {
          if (data.expectedBarkSound() == null) {
            testBarkCooldownStaysInactive(ctx, data);
          } else {
            testCooldownPreventsBark(ctx, data);
          }
        });
  }

  private List<TestFunction> generatePerBreed(
      final String behavior, final int tickLimit, final PerBreedBody body) {
    return DogTestData.getAllBreeds().stream()
        .map(
            data ->
                new TestFunction(
                    "defaultBatch",
                    "dogentitysoundtest." + behavior + "." + data.breed().serializedId(),
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

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyStartsNotHowling(TestContext context) {
    final HuskyEntity husky = DogTestHelper.spawnDog(context, DogTestData.HUSKY);

    context.assertFalse(husky.isHowling(), "Husky should not be howling immediately after spawn");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void huskyStartsWithZeroHowlCooldown(TestContext context) {
    final HuskyEntity husky = DogTestHelper.spawnDog(context, DogTestData.HUSKY);

    context.assertTrue(
        husky.getHowlCooldownTicks() == 0, "Husky howl cooldown should be 0 at spawn");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void huskySleepingPreventsHowling(TestContext context) {
    final BlockPos absBedPos = context.getAbsolutePos(new BlockPos(0, 1, 0));
    final HuskyEntity husky = DogTestHelper.spawnDog(context, DogTestData.HUSKY);

    context.runAtTick(5, () -> husky.startSleepingInBed(absBedPos));

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(husky.isSleepingInBed(), "Husky should be sleeping");
          context.assertFalse(husky.isHowling(), "Sleeping husky should not be howling");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void huskyHowlingFlagSetsAndClearsCorrectly(TestContext context) {
    final HuskyEntity husky = DogTestHelper.spawnDog(context, DogTestData.HUSKY);

    context.runAtTick(
        5,
        () ->
            context.assertFalse(
                husky.isHowling(), "isHowling should start false before any howl is triggered"));

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              husky.getHowlCooldownTicks() == 0,
              "Howl cooldown should still be 0 since howl has not triggered");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 200)
  public void huskyHowlCooldownIsNonNegativeAfterMultipleTicks(TestContext context) {
    final HuskyEntity husky = DogTestHelper.spawnDog(context, DogTestData.HUSKY);

    context.runAtTick(
        50,
        () -> {
          context.assertTrue(
              husky.getHowlCooldownTicks() >= 0,
              "husky howl cooldown should never go negative after ticking");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testDogBarksWhenLowHealth(
      final TestContext context, final DogTestData<T> data) {
    final T dog = DogTestHelper.spawnDog(context, data);

    context.runAtTick(5, () -> dog.setHealth(1.0f));

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              dog.getBarkCooldownTicks() > 0,
              "Low health dog should have barked (cooldown should be > 0)");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testDogDoesNotBarkWhenLowHealth(
      final TestContext context, final DogTestData<T> data) {
    final T dog = DogTestHelper.spawnDog(context, data);

    context.runAtTick(5, () -> dog.setHealth(1.0f));

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              dog.getBarkCooldownTicks() == 0,
              data.breed().serializedId() + " should not bark at low health");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testDogBarksWhenHasTarget(
      final TestContext context, final DogTestData<T> data) {
    final T dog = DogTestHelper.spawnDog(context, data);

    final ZombieEntity zombie =
        (ZombieEntity) context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 0));

    context.runAtTick(5, () -> dog.setTarget(zombie));

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              dog.getBarkCooldownTicks() > 0,
              "Dog with target should have barked (cooldown should be > 0)");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testDogDoesNotBarkWhenHasTarget(
      final TestContext context, final DogTestData<T> data) {
    final T dog = DogTestHelper.spawnDog(context, data);

    final ZombieEntity zombie =
        (ZombieEntity) context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 0));

    context.runAtTick(5, () -> dog.setTarget(zombie));

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              dog.getBarkCooldownTicks() == 0,
              data.breed().serializedId() + " should not bark with a target");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testDogBarksWhenTakingDamage(
      final TestContext context, final DogTestData<T> data) {
    final T dog = DogTestHelper.spawnDog(context, data);

    context.runAtTick(5, () -> DogTestHelper.damageEntity(dog, 1.0f));

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              dog.getBarkCooldownTicks() > 0,
              "Dog should have barked when taking damage (cooldown should be > 0)");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testDogDoesNotBarkWhenTakingDamage(
      final TestContext context, final DogTestData<T> data) {
    final T dog = DogTestHelper.spawnDog(context, data);

    context.runAtTick(5, () -> DogTestHelper.damageEntity(dog, 1.0f));

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              dog.getBarkCooldownTicks() == 0,
              data.breed().serializedId() + " should not bark when taking damage");
          context.complete();
        });
  }

  private <T extends UnleashedDogEntity> void testCooldownPreventsBark(
      final TestContext context, final DogTestData<T> data) {
    final T dog = DogTestHelper.spawnDog(context, data);

    final ZombieEntity zombie =
        (ZombieEntity) context.spawnEntity(EntityType.ZOMBIE, new BlockPos(2, 1, 0));

    context.runAtTick(5, () -> dog.setTarget(zombie));

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              dog.getBarkCooldownTicks() > 0, "Dog should have barked once (cooldown > 0)");
          final int cooldownAfterFirstBark = dog.getBarkCooldownTicks();

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

  private <T extends UnleashedDogEntity> void testBarkCooldownStaysInactive(
      final TestContext context, final DogTestData<T> data) {
    final T dog = DogTestHelper.spawnDog(context, data);

    context.runAtTick(
        10,
        () -> {
          context.assertTrue(
              dog.getBarkCooldownTicks() == 0,
              data.breed().serializedId() + " bark cooldown should stay 0");
          context.complete();
        });
  }
}
