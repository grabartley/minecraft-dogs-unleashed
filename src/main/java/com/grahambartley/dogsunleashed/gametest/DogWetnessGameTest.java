package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestData;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.biome.BiomeKeys;

/**
 * Weather is world state, so every test here pins rain for the whole batch and only the {@code
 * skyAccess = true} tests can actually be rained on: the framework roofs every other structure with
 * barrier blocks, which is also what keeps this batch's rain from reaching dogs in other batches.
 */
public final class DogWetnessGameTest implements FabricGameTest {

  private static final String BATCH = "dog-wetness";
  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos DOG_POS = new BlockPos(3, 2, 3);
  private static final BlockPos ROOF_POS = new BlockPos(3, 4, 3);
  private static final BlockPos OWNER_POS = new BlockPos(5, 2, 3);
  private static final int RAIN_DURATION_TICKS = 1_000_000;
  private static final int SOAK_TICKS = 20;
  private static final int WATER_SOAK_TICKS = 5;
  private static final int SHAKE_ASSERT_TICK = 120;
  private static final int DRY_ASSERT_TICK = 60;
  private static final int TICK_LIMIT = 140;
  private static final int DRY_TICK_LIMIT = 80;

  @BeforeBatch(batchId = BATCH)
  public void startRaining(final ServerWorld world) {
    world.setWeather(0, RAIN_DURATION_TICKS, true, false);
    world.setRainGradient(1.0f);
  }

  @AfterBatch(batchId = BATCH)
  public void stopRaining(final ServerWorld world) {
    world.setRainGradient(0.0f);
    world.resetWeather();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TICK_LIMIT, skyAccess = true)
  public void rainSoakedDogShakesOnceItIsUnderShelter(final TestContext context) {
    context.setBiome(BiomeKeys.PLAINS);
    final UnleashedDogEntity dog = spawnStationaryDog(context);
    final AtomicBoolean sheltered = new AtomicBoolean(false);
    final AtomicBoolean shookWhileSheltered = new AtomicBoolean(false);

    context.runAtEveryTick(
        () -> {
          if (sheltered.get() && dog.isShaking()) {
            shookWhileSheltered.set(true);
          }
        });

    context.runAtTick(
        SOAK_TICKS,
        () -> {
          context.assertTrue(
              context.getWorld().isRaining(),
              "This batch must be raining for the test to mean anything");
          context.assertTrue(
              dog.isTouchingWaterOrRain(), "A dog stood out in the rain should register as wet");
          context.assertFalse(
              dog.isShaking(), "A dog should not shake while the rain is still soaking it");
          context.setBlockState(ROOF_POS, Blocks.STONE);
          sheltered.set(true);
        });

    context.runAtTick(
        SHAKE_ASSERT_TICK,
        () -> {
          context.assertTrue(
              shookWhileSheltered.get(),
              "A rain-soaked dog should shake the water off once it is out of the weather");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = DRY_TICK_LIMIT, skyAccess = true)
  public void shelteredDogStaysDryWhileItRains(final TestContext context) {
    context.setBiome(BiomeKeys.PLAINS);
    context.setBlockState(ROOF_POS, Blocks.STONE);
    final UnleashedDogEntity dog = spawnStationaryDog(context);
    final AtomicBoolean shook = new AtomicBoolean(false);

    context.runAtEveryTick(
        () -> {
          if (dog.isShaking()) {
            shook.set(true);
          }
        });

    context.runAtTick(
        DRY_ASSERT_TICK,
        () -> {
          context.assertTrue(
              context.getWorld().isRaining(),
              "This batch must be raining for the test to mean anything");
          context.assertFalse(
              dog.isTouchingWaterOrRain(),
              "A dog stood under a roof should stay dry while it rains");
          context.assertFalse(shook.get(), "A dog that never got wet should never shake");
          context.complete();
        });
  }

  /**
   * The roofed structure keeps the batch's rain off this dog, so the only thing that can wet it is
   * the water it is stood in. The water sits in a walled basin so that draining the source block
   * cannot leave flowing water behind to keep the dog wet.
   */
  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = DRY_TICK_LIMIT)
  public void dogShakesAfterClimbingOutOfWater(final TestContext context) {
    for (final Direction side : Direction.Type.HORIZONTAL) {
      context.setBlockState(DOG_POS.offset(side), Blocks.STONE);
    }
    context.setBlockState(DOG_POS, Blocks.WATER);
    final UnleashedDogEntity dog = spawnStationaryDog(context);
    final AtomicBoolean drained = new AtomicBoolean(false);
    final AtomicBoolean shookWhileDry = new AtomicBoolean(false);

    context.runAtEveryTick(
        () -> {
          if (drained.get() && dog.isShaking()) {
            shookWhileDry.set(true);
          }
        });

    context.runAtTick(
        WATER_SOAK_TICKS,
        () -> {
          context.assertTrue(
              dog.isTouchingWaterOrRain(), "A dog stood in water should register as wet");
          context.setBlockState(DOG_POS, Blocks.AIR);
          drained.set(true);
        });

    context.runAtTick(
        DRY_ASSERT_TICK,
        () -> {
          context.assertTrue(
              shookWhileDry.get(), "A dog should still shake the water off after leaving water");
          context.complete();
        });
  }

  /**
   * Sitting suppresses the shake, and an owner keeps vanilla {@code SitGoal} from putting the dog
   * into that pose on its own, so every test here spawns a stationary owned dog.
   */
  private static UnleashedDogEntity spawnStationaryDog(final TestContext context) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final BlockPos absoluteOwnerPos = context.getAbsolutePos(OWNER_POS);
    owner.refreshPositionAndAngles(
        absoluteOwnerPos.getX() + 0.5,
        absoluteOwnerPos.getY(),
        absoluteOwnerPos.getZ() + 0.5,
        0f,
        0f);
    final UnleashedDogEntity dog =
        DogTestHelper.spawnTamedDog(context, DogTestData.HUSKY, DOG_POS, owner.getUuid());
    dog.setAiDisabled(true);
    return dog;
  }
}
