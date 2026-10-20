package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.DogMud;
import com.grahambartley.dogsunleashed.entity.DogMudRules;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class DogMudGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String MUD_ON = "dog-mud-on";
  private static final String MUD_OFF = "dog-mud-off";
  private static final BlockPos REL_FLOOR = new BlockPos(3, 1, 3);
  private static final BlockPos REL_STAND = new BlockPos(3, 2, 3);
  private static final Vec3d ON_MUD = new Vec3d(3.5, 1.875, 3.5);
  private static final Vec3d IN_CAULDRON = new Vec3d(3.5, 2.25, 3.5);
  private static final int MUDDY_BY = DogMudRules.TICKS_TO_GET_MUDDY + 10;

  private static DogsUnleashedConfig original = DogsUnleashedConfig.defaults();

  @BeforeBatch(batchId = MUD_ON)
  public void beforeMudOn(final ServerWorld world) {
    original = DogsUnleashed.SERVER_CONFIG;
    DogsUnleashed.SERVER_CONFIG = original.withGameplay(original.gameplay().withMuddyDogs(true));
  }

  @AfterBatch(batchId = MUD_ON)
  public void afterMudOn(final ServerWorld world) {
    DogsUnleashed.SERVER_CONFIG = original;
  }

  @BeforeBatch(batchId = MUD_OFF)
  public void beforeMudOff(final ServerWorld world) {
    original = DogsUnleashed.SERVER_CONFIG;
    DogsUnleashed.SERVER_CONFIG = original.withGameplay(original.gameplay().withMuddyDogs(false));
  }

  @AfterBatch(batchId = MUD_OFF)
  public void afterMudOff(final ServerWorld world) {
    DogsUnleashed.SERVER_CONFIG = original;
  }

  @GameTest(templateName = ARENA, batchId = MUD_ON, tickLimit = MUDDY_BY + 10)
  public void aDogStandingOnMudGetsMuddy(final TestContext context) {
    context.setBlockState(REL_FLOOR, Blocks.MUD);
    final UnleashedDogEntity dog = still(context, ON_MUD);
    context.runAtTick(
        5, () -> context.assertFalse(mud(dog).isMuddy(), "A dog is not muddy the moment it lands"));
    context.runAtTick(
        MUDDY_BY,
        () -> {
          context.assertTrue(mud(dog).isMuddy(), "A dog standing on mud gets muddy");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = MUD_ON, tickLimit = 60)
  public void aDogOnStoneStaysClean(final TestContext context) {
    final UnleashedDogEntity dog = still(context, Vec3d.of(REL_STAND).add(0.5, 0.0, 0.5));
    context.runAtTick(
        MUDDY_BY,
        () -> {
          context.assertFalse(mud(dog).isMuddy(), "A dog on a dry stone floor stays clean");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = MUD_ON, tickLimit = 20)
  public void aWaterCauldronWashesAMuddyDogAndLosesOneLevel(final TestContext context) {
    context.setBlockState(
        REL_STAND, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
    final UnleashedDogEntity dog = still(context, IN_CAULDRON);
    mud(dog).makeMuddy();
    context.runAtTick(
        5,
        () -> {
          context.assertFalse(mud(dog).isMuddy(), "A muddy dog in a water cauldron is washed");
          context.checkBlockState(
              REL_STAND,
              state ->
                  state.isOf(Blocks.WATER_CAULDRON) && state.get(LeveledCauldronBlock.LEVEL) == 2,
              () -> "Washing a dog uses one level of the cauldron's water");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = MUD_ON, tickLimit = 20)
  public void aCleanDogInACauldronLeavesTheWater(final TestContext context) {
    context.setBlockState(
        REL_STAND, Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
    still(context, IN_CAULDRON);
    context.runAtTick(
        10,
        () -> {
          context.checkBlockState(
              REL_STAND,
              state -> state.get(LeveledCauldronBlock.LEVEL) == 3,
              () -> "A clean dog does not use up cauldron water");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = MUD_OFF, tickLimit = MUDDY_BY + 10)
  public void withMuddyDogsOffADogOnMudStaysClean(final TestContext context) {
    context.setBlockState(REL_FLOOR, Blocks.MUD);
    final UnleashedDogEntity dog = still(context, ON_MUD);
    context.runAtTick(
        MUDDY_BY,
        () -> {
          context.assertFalse(mud(dog).isMuddy(), "With Muddy Dogs off a dog never gets muddy");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = MUD_OFF, tickLimit = 20)
  public void withMuddyDogsOffAMuddyDogIsCleaned(final TestContext context) {
    final UnleashedDogEntity dog = still(context, Vec3d.of(REL_STAND).add(0.5, 0.0, 0.5));
    mud(dog).makeMuddy();
    context.runAtTick(
        5,
        () -> {
          context.assertFalse(mud(dog).isMuddy(), "Turning Muddy Dogs off cleans every dog");
          context.complete();
        });
  }

  private static UnleashedDogEntity still(final TestContext context, final Vec3d pos) {
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.BEAGLE, pos);
    dog.setAiDisabled(true);
    return dog;
  }

  private static DogMud mud(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogMud.class);
  }
}
