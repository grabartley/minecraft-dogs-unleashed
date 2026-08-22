package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlockTags;
import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.block.DogHouseBlock;
import com.grahambartley.dogsunleashed.block.DogHouseLayout;
import com.grahambartley.dogsunleashed.block.DogHousePart;
import com.grahambartley.dogsunleashed.block.DogSleepSpotAssignment;
import com.grahambartley.dogsunleashed.block.entity.AssignedDogHolder;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class DogHouseGameTest implements FabricGameTest {

  private static final BlockPos REL_ORIGIN = new BlockPos(1, 1, 1);
  private static final Direction FACING = Direction.NORTH;

  /**
   * These tests drive sleep imperatively with the AI off, so none of them read the world clock.
   * Touching {@code DO_DAYLIGHT_CYCLE} here would unfreeze it underneath the time-pinned batches
   * running in parallel, so this batch only resets the JVM-global map it can dirty.
   */
  @BeforeBatch(batchId = "dog-house")
  public void beforeBatch(final ServerWorld world) {
    DogSleepSpotAssignment.clearPendingAssignments();
  }

  @AfterBatch(batchId = "dog-house")
  public void afterBatch(final ServerWorld world) {
    DogSleepSpotAssignment.clearPendingAssignments();
  }

  /** The origin goes down first: every other cell refuses to exist without it. */
  private static void placeHouse(final TestContext context) {
    final BlockState origin =
        ModBlocks.DOG_HOUSE
            .getDefaultState()
            .with(DogHouseBlock.FACING, FACING)
            .with(DogHouseBlock.PART, DogHousePart.ORIGIN);
    for (final DogHousePart part : DogHousePart.values()) {
      context.setBlockState(
          REL_ORIGIN.add(DogHouseLayout.offsetFromOrigin(part, FACING)),
          origin.with(DogHouseBlock.PART, part));
    }
  }

  private static BlockPos rel(final DogHousePart part) {
    return REL_ORIGIN.add(DogHouseLayout.offsetFromOrigin(part, FACING));
  }

  private static UnleashedDogEntity sleepingDog(final TestContext context, final BlockPos relBed) {
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final UnleashedDogEntity dog = context.spawnEntity(ModEntities.HUSKY, relBed);
    dog.setTamed(true, true);
    dog.setOwnerUuid(owner.getUuid());
    dog.setAiDisabled(true);

    final BlockPos absBed = context.getAbsolutePos(relBed);
    dog.setAssignedBedPos(absBed);
    dog.getSleepController().startSleepingInBed(absBed);
    return dog;
  }

  /**
   * The sleep goals resolve beds through a tag rather than by naming blocks, so the tag binding is
   * what actually makes a house a bed. Tag bindings only exist on a running server.
   */
  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 40)
  public void dogHouseIsTaggedAsADogBed(final TestContext context) {
    placeHouse(context);

    context.assertTrue(
        context.getBlockState(REL_ORIGIN).isIn(ModBlockTags.DOG_BEDS),
        "The dog house must be in the dog beds tag or the sleep goals reject it");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 40)
  public void dogBedIsStillTaggedAsADogBed(final TestContext context) {
    context.setBlockState(REL_ORIGIN, ModBlocks.DOG_BED.getDefaultState());

    context.assertTrue(
        context.getBlockState(REL_ORIGIN).isIn(ModBlockTags.DOG_BEDS),
        "Moving the goals onto a tag must not drop the original dog bed");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 40)
  public void breakingTheOriginTakesEveryOtherCell(final TestContext context) {
    placeHouse(context);

    context.runAtTick(5, () -> context.setBlockState(REL_ORIGIN, Blocks.AIR.getDefaultState()));
    context.runAtTick(
        15,
        () -> {
          for (final DogHousePart part : DogHousePart.values()) {
            context.assertTrue(
                context.getBlockState(rel(part)).isAir(),
                "No cell may outlive the origin, but " + part.asString() + " did");
          }
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 40)
  public void breakingAFarCornerTakesTheWholeHouse(final TestContext context) {
    placeHouse(context);

    context.runAtTick(
        5,
        () ->
            context.setBlockState(
                rel(DogHousePart.BACK_RIGHT_UPPER), Blocks.AIR.getDefaultState()));
    context.runAtTick(
        15,
        () -> {
          for (final DogHousePart part : DogHousePart.values()) {
            context.assertTrue(
                context.getBlockState(rel(part)).isAir(),
                "Breaking any cell must take the whole house, but " + part.asString() + " stayed");
          }
          context.complete();
        });
  }

  /** The block entity, and so the assignment and the rendered model, exists once per house. */
  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 40)
  public void onlyTheOriginCarriesTheBlockEntity(final TestContext context) {
    placeHouse(context);

    context.assertTrue(
        context.getBlockEntity(REL_ORIGIN) != null, "The origin must carry the block entity");
    for (final DogHousePart part : DogHousePart.values()) {
      if (part == DogHousePart.ORIGIN) {
        continue;
      }
      context.assertTrue(
          context.getWorld().getBlockEntity(context.getAbsolutePos(rel(part))) == null,
          "A second block entity at " + part.asString() + " would draw the house twice");
    }
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 60)
  public void aDogSleepsInAHouseTheSameWayItSleepsInABed(final TestContext context) {
    placeHouse(context);

    context.runAtTick(
        10,
        () -> {
          final UnleashedDogEntity dog = sleepingDog(context, REL_ORIGIN);
          context.assertTrue(dog.isSleepingInBed(), "A dog must be able to sleep in a dog house");
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 60)
  public void wakingInADogHouseGrantsRegeneration(final TestContext context) {
    placeHouse(context);

    context.runAtTick(
        10,
        () -> {
          final UnleashedDogEntity dog = sleepingDog(context, REL_ORIGIN);
          dog.wakeUp();

          context.assertTrue(
              dog.hasStatusEffect(StatusEffects.REGENERATION),
              "Waking in a dog house should leave the dog rested");
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 60)
  public void wakingInAPlainDogBedGrantsNothing(final TestContext context) {
    context.setBlockState(REL_ORIGIN, ModBlocks.DOG_BED.getDefaultState());

    context.runAtTick(
        10,
        () -> {
          final UnleashedDogEntity dog = sleepingDog(context, REL_ORIGIN);
          dog.wakeUp();

          context.assertTrue(
              !dog.hasStatusEffect(StatusEffects.REGENERATION),
              "The comfort buff is what the house is for, so a plain bed must not grant it");
          context.complete();
        });
  }

  /** Tearing the house down calls the same wake path, and must not pay out the buff. */
  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 60)
  public void breakingTheHouseUnderASleepingDogGrantsNothing(final TestContext context) {
    placeHouse(context);
    final AtomicReference<UnleashedDogEntity> occupant = new AtomicReference<>();

    context.runAtTick(
        10,
        () -> {
          final UnleashedDogEntity dog = sleepingDog(context, REL_ORIGIN);
          if (context.getBlockEntity(REL_ORIGIN) instanceof AssignedDogHolder holder) {
            holder.setAssignedDog(dog);
          }
          occupant.set(dog);
        });
    context.runAtTick(20, () -> context.setBlockState(REL_ORIGIN, Blocks.AIR.getDefaultState()));
    context.runAtTick(
        30,
        () -> {
          context.assertTrue(
              !occupant.get().isSleepingInBed(),
              "Losing the house should wake the dog that was sleeping in it");
          context.assertTrue(
              !occupant.get().hasStatusEffect(StatusEffects.REGENERATION),
              "Breaking the house is not a rest, so it must not grant the buff");
          context.complete();
        });
  }
}
