package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlockTags;
import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.block.DogHouseBlock;
import com.grahambartley.dogsunleashed.block.DogHouseLayout;
import com.grahambartley.dogsunleashed.block.DogHousePart;
import com.grahambartley.dogsunleashed.block.DogSleepSpotAssignment;
import com.grahambartley.dogsunleashed.block.entity.AssignedDogHolder;
import com.grahambartley.dogsunleashed.block.entity.DogHouseBlockEntity;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

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

  private static final DyeColor TEST_COLOR = DyeColor.MAGENTA;

  private static void dyeHouse(final TestContext context, final DyeColor color) {
    final DogHouseBlockEntity houseBlockEntity = context.getBlockEntity(rel(DogHousePart.ORIGIN));
    houseBlockEntity.setColor(color);
  }

  private static List<ItemStack> housesDroppedIn(final TestContext context) {
    final Box searchBox = context.getTestBox().expand(2.0);
    return context
        .getWorld()
        .getEntitiesByClass(ItemEntity.class, searchBox, entity -> true)
        .stream()
        .map(ItemEntity::getStack)
        .filter(stack -> stack.isOf(ModItems.DOG_HOUSE))
        .toList();
  }

  /**
   * Only the origin cell holds the block entity carrying the colour, and the cascade that takes the
   * rest of the house down clears that cell too. Whichever cell the player swings at, exactly one
   * house has to drop and it has to remember what colour it was.
   */
  @CustomTestProvider
  public List<TestFunction> breakingAnyCellDropsOneDyedHouse() {
    return java.util.Arrays.stream(DogHousePart.values())
        .map(
            part ->
                new TestFunction(
                    "dog-house-drops",
                    "doghousegametest.breakingacelldropsonedyedhouse." + part.asString(),
                    "dogs-unleashed:dog_arena",
                    40,
                    0L,
                    true,
                    context -> {
                      placeHouse(context);
                      dyeHouse(context, TEST_COLOR);

                      context.getWorld().breakBlock(context.getAbsolutePos(rel(part)), true);

                      context.addInstantFinalTask(
                          () -> {
                            final List<ItemStack> dropped = housesDroppedIn(context);
                            context.assertTrue(
                                dropped.size() == 1,
                                "expected exactly one dog house to drop from breaking "
                                    + part.asString()
                                    + " but got "
                                    + dropped.size());
                            context.assertTrue(
                                dropped.get(0).get(ModComponents.DOG_HOUSE_COLOR) == TEST_COLOR,
                                "the dropped house lost the colour it was dyed");
                          });
                    }))
        .toList();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 40)
  public void anUndyedHouseDropsAWhiteHouse(final TestContext context) {
    placeHouse(context);
    context.getWorld().breakBlock(context.getAbsolutePos(rel(DogHousePart.ORIGIN)), true);

    context.addInstantFinalTask(
        () -> {
          final List<ItemStack> dropped = housesDroppedIn(context);
          context.assertTrue(dropped.size() == 1, "expected exactly one dog house to drop");
          context.assertTrue(
              dropped.get(0).getOrDefault(ModComponents.DOG_HOUSE_COLOR, DyeColor.WHITE)
                  == DyeColor.WHITE,
              "an undyed house should drop white");
        });
  }

  /** The colour lives on the origin, so dyeing has to reach it from whichever cell was clicked. */
  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 40)
  public void dyeingTheHouseColoursTheCellThatHoldsTheBlockEntity(final TestContext context) {
    placeHouse(context);
    dyeHouse(context, DyeColor.LIME);

    context.addInstantFinalTask(
        () -> {
          final DogHouseBlockEntity houseBlockEntity =
              context.getBlockEntity(rel(DogHousePart.ORIGIN));
          context.assertTrue(
              houseBlockEntity.getColor() == DyeColor.LIME, "the house did not take the dye");
        });
  }

  /** A colour that does not survive a save and reload is a colour the player loses on relog. */
  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 40)
  public void theCushionColourSurvivesAnNbtRoundTrip(final TestContext context) {
    placeHouse(context);
    dyeHouse(context, DyeColor.CYAN);

    final DogHouseBlockEntity houseBlockEntity = context.getBlockEntity(rel(DogHousePart.ORIGIN));
    final net.minecraft.nbt.NbtCompound nbt =
        houseBlockEntity.createNbt(context.getWorld().getRegistryManager());

    final DogHouseBlockEntity reloaded =
        new DogHouseBlockEntity(BlockPos.ORIGIN, ModBlocks.DOG_HOUSE.getDefaultState());
    reloaded.read(nbt, context.getWorld().getRegistryManager());

    context.addInstantFinalTask(
        () ->
            context.assertTrue(
                reloaded.getColor() == DyeColor.CYAN,
                "the cushion colour was lost across a save and reload"));
  }

  /**
   * The colour rides from the recipe to the placed house on the item component, so this places a
   * real dyed stack the way a player does rather than setting the colour on the block entity.
   */
  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 40)
  public void placingADyedHouseColoursTheHouseThatAppears(final TestContext context) {
    final BlockPos relFloor = new BlockPos(1, 1, 1);
    final BlockPos relPlaced = relFloor.up();

    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    final Vec3d standingClear = Vec3d.ofBottomCenter(context.getAbsolutePos(new BlockPos(5, 2, 5)));
    player.refreshPositionAndAngles(standingClear.x, standingClear.y, standingClear.z, 0.0f, 0.0f);

    final ItemStack stack = new ItemStack(ModItems.DOG_HOUSE);
    stack.set(ModComponents.DOG_HOUSE_COLOR, DyeColor.ORANGE);
    player.setStackInHand(Hand.MAIN_HAND, stack);

    final BlockPos absFloor = context.getAbsolutePos(relFloor);
    final BlockHitResult hit =
        new BlockHitResult(Vec3d.ofCenter(absFloor), Direction.UP, absFloor, false);
    stack.useOnBlock(new ItemUsageContext(context.getWorld(), player, Hand.MAIN_HAND, stack, hit));

    context.addInstantFinalTask(
        () -> {
          final BlockState placed = context.getBlockState(relPlaced);
          context.assertTrue(placed.isOf(ModBlocks.DOG_HOUSE), "the dyed house did not place");
          context.assertTrue(
              placed.get(DogHouseBlock.PART) == DogHousePart.ORIGIN,
              "the cell under the cursor should be the origin");
          final DogHouseBlockEntity houseBlockEntity = context.getBlockEntity(relPlaced);
          context.assertTrue(
              houseBlockEntity.getColor() == DyeColor.ORANGE,
              "a house placed from an orange stack came out "
                  + houseBlockEntity.getColor().getName());
        });
  }
}
