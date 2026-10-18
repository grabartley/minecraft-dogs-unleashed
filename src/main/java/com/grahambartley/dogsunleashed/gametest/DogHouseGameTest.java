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
import com.grahambartley.dogsunleashed.gametest.util.DogHouseTestHelper;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.gametest.util.GeneratedGameTest;
import com.grahambartley.dogsunleashed.gametest.util.SleepBatchFixture;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class DogHouseGameTest implements FabricGameTest {

  private static final BlockPos REL_ORIGIN = new BlockPos(2, 2, 2);
  private static final Direction FACING = Direction.NORTH;
  private static final double ROOF_SLAB_TOP_IN_CELL = 6.0 / 16.0;
  private static final double CUSHION_TOP = 4.0 / 16.0;
  private static final double WALK_AWAY_DISTANCE = 1.0;
  private static final double CARPET_HEIGHT = 1.0 / 16.0;

  @BeforeBatch(batchId = "dog-house")
  public void beforeBatch(final ServerWorld world) {
    DogSleepSpotAssignment.clearPendingAssignments();
  }

  @AfterBatch(batchId = "dog-house")
  public void afterBatch(final ServerWorld world) {
    DogSleepSpotAssignment.clearPendingAssignments();
  }

  @BeforeBatch(batchId = "dog-house-night")
  public void beforeNightBatch(final ServerWorld world) {
    SleepBatchFixture.prepare(world);
    DogSleepSpotAssignment.clearPendingAssignments();
    SleepBatchFixture.pinNight(world);
  }

  @AfterBatch(batchId = "dog-house-night")
  public void afterNightBatch(final ServerWorld world) {
    SleepBatchFixture.teardown(world);
    DogSleepSpotAssignment.clearPendingAssignments();
  }

  private static void placeHouse(final TestContext context) {
    DogHouseTestHelper.placeHouse(context, REL_ORIGIN, FACING);
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
    dog.getSleepController().setAssignedBedPos(absBed);
    dog.getSleepController().startSleepingInBed(absBed);
    return dog;
  }

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
  public void somethingDroppedOnTheRoofRestsOnTheRoof(final TestContext context) {
    placeHouse(context);
    final BlockPos roofCell = rel(DogHousePart.FRONT_LEFT_UPPER);
    final ItemEntity dropped =
        context.spawnItem(
            Items.OAK_PLANKS,
            roofCell.getX() + 0.2f,
            roofCell.getY() + 0.7f,
            roofCell.getZ() + 0.5f);
    dropped.setVelocity(0, 0, 0);

    context.runAtEveryTick(
        () -> {
          final double itemY = context.getRelative(dropped.getPos()).y;
          context.assertTrue(
              itemY > roofCell.getY() + ROOF_SLAB_TOP_IN_CELL,
              "Something dropped on the roof should stay on it, not fall inside; fell to y="
                  + itemY);
          if (dropped.isOnGround()) {
            context.complete();
          }
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house-night", tickLimit = 200)
  public void aDogThatPutsItselfToBedLiesOnTheMiddleOfTheCushion(final TestContext context) {
    assertSleepsOnTheCushionCentre(context, (dog, bed) -> {});
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house-night", tickLimit = 200)
  public void aDogSentToBedLiesOnTheMiddleOfTheCushion(final TestContext context) {
    assertSleepsOnTheCushionCentre(
        context, (dog, bed) -> dog.getSleepController().commandToSleep(bed));
  }

  private static void assertSleepsOnTheCushionCentre(
      final TestContext context, final BiConsumer<UnleashedDogEntity, BlockPos> sendToBed) {
    placeHouse(context);
    final ServerPlayerEntity owner = context.createMockCreativeServerPlayerInWorld();
    final UnleashedDogEntity dog =
        context.spawnEntity(ModEntities.SHIBA_INU, REL_ORIGIN.offset(FACING));
    dog.setTamed(true, true);
    dog.setOwnerUuid(owner.getUuid());
    dog.setInvulnerable(true);
    final BlockPos absOrigin = context.getAbsolutePos(REL_ORIGIN);
    dog.getSleepController().setAssignedBedPos(absOrigin);
    sendToBed.accept(dog, absOrigin);

    context.runAtEveryTick(
        () -> {
          if (!dog.getSleepController().isSleepingInBed()) {
            return;
          }
          final Vec3d cushion = DogHouseLayout.cushionCentre(absOrigin, FACING);
          final double offCentre = Math.hypot(dog.getX() - cushion.x, dog.getZ() - cushion.z);
          context.assertTrue(
              offCentre < 1.0e-3,
              "A dog asleep in a house should lie on the middle of the cushion, was off by "
                  + offCentre);
          context.assertTrue(
              Math.abs(dog.getY() - (cushion.y + CUSHION_TOP)) < 1.0e-3,
              "A dog asleep in a house should lie on top of the cushion, was at y="
                  + (dog.getY() - absOrigin.getY()));
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 40)
  public void aDogThatWakesInAHouseStepsOntoTheDoorstep(final TestContext context) {
    placeHouse(context);
    final UnleashedDogEntity dog = sleepingDog(context, REL_ORIGIN);
    dog.getSleepController().markManuallyWoken();
    dog.wakeUp();

    assertSteppedOut(context, dog);
    context.assertTrue(
        context.getRelative(dog.getPos()).z < REL_ORIGIN.getZ(),
        "With nothing in the way, a waking dog should step out in front of the door");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 40)
  public void aDogThatWakesInAHouseStepsOntoACarpetedDoorstep(final TestContext context) {
    placeHouse(context);
    context.setBlockState(REL_ORIGIN.offset(FACING), Blocks.RED_CARPET);
    context.setBlockState(REL_ORIGIN.add(1, 0, 0).offset(FACING), Blocks.RED_CARPET);
    final UnleashedDogEntity dog = sleepingDog(context, REL_ORIGIN);
    dog.getSleepController().markManuallyWoken();
    dog.wakeUp();

    assertSteppedOut(context, dog);
    context.assertTrue(
        context.getRelative(dog.getPos()).z < REL_ORIGIN.getZ()
            && Math.abs(context.getRelative(dog.getPos()).y - (REL_ORIGIN.getY() + CARPET_HEIGHT))
                < 1.0e-6,
        "A waking dog should stand on top of a carpet in front of the door, was at "
            + context.getRelative(dog.getPos()));
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 40)
  public void aDogThatWakesInAHouseWithABlockedDoorstepStepsOutBesideIt(final TestContext context) {
    placeHouse(context);
    context.setBlockState(REL_ORIGIN.offset(FACING), Blocks.STONE);
    context.setBlockState(REL_ORIGIN.add(1, 0, 0).offset(FACING), Blocks.STONE);
    final UnleashedDogEntity dog = sleepingDog(context, REL_ORIGIN);
    dog.getSleepController().markManuallyWoken();
    dog.wakeUp();

    final List<BlockPos> frontCorners =
        DogHouseLayout.wakeCells(REL_ORIGIN, FACING).subList(2, 4).stream()
            .map(context::getAbsolutePos)
            .toList();
    context.assertTrue(
        frontCorners.contains(dog.getBlockPos()),
        "With the door blocked, a waking dog should step out at a front corner of the house");
    assertSteppedOut(context, dog);
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 40)
  public void aDogThatWakesInAWalledInHouseStaysOnTheCushion(final TestContext context) {
    placeHouse(context);
    final BlockPos absOrigin = context.getAbsolutePos(REL_ORIGIN);
    for (final BlockPos cell : DogHouseLayout.wakeCells(REL_ORIGIN, FACING)) {
      context.setBlockState(cell, Blocks.STONE);
      context.setBlockState(cell.up(), Blocks.STONE);
    }
    final UnleashedDogEntity dog = sleepingDog(context, REL_ORIGIN);
    dog.getSleepController().markManuallyWoken();
    dog.wakeUp();

    final Vec3d cushion = DogHouseLayout.cushionCentre(absOrigin, FACING);
    context.assertFalse(dog.getSleepController().isSleepingInBed(), "The dog should be awake");
    context.assertTrue(
        dog.getPos().squaredDistanceTo(cushion.add(0.0, CUSHION_TOP, 0.0)) < 1.0e-6,
        "With nowhere to step out to, a waking dog should stay on the cushion");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 40)
  public void unassigningASleepingDogStepsItOutOfTheHouse(final TestContext context) {
    placeHouse(context);
    final UnleashedDogEntity dog = sleepingDog(context, REL_ORIGIN);
    dog.getSleepController().clearAssignedBed();

    assertSteppedOut(context, dog);
    context.complete();
  }

  private static void assertSteppedOut(final TestContext context, final UnleashedDogEntity dog) {
    final BlockPos absOrigin = context.getAbsolutePos(REL_ORIGIN);
    context.assertFalse(dog.getSleepController().isSleepingInBed(), "The dog should be awake");
    context.assertFalse(
        DogHouseLayout.cellsOf(absOrigin, FACING).contains(dog.getBlockPos()),
        "A waking dog should end up outside the house, was at "
            + context.getRelative(dog.getPos()));
    context.assertTrue(
        context.getWorld().isSpaceEmpty(dog, dog.getBoundingBox()),
        "A dog that stepped out should not be wedged in anything");
    final Vec3d middle = Vec3d.ofBottomCenter(absOrigin).add(0.5, 0.0, 0.5);
    final Vec3d away =
        new Vec3d(dog.getX() - middle.x, 0.0, dog.getZ() - middle.z)
            .normalize()
            .multiply(WALK_AWAY_DISTANCE);
    final Vec3d start = dog.getPos();
    dog.move(MovementType.SELF, away);
    context.assertTrue(
        dog.getPos().distanceTo(start) > WALK_AWAY_DISTANCE - 1.0e-6,
        "A dog that stepped out should be free to walk away, moved "
            + dog.getPos().distanceTo(start));
  }

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 60)
  public void aDogSleepsInAHouseTheSameWayItSleepsInABed(final TestContext context) {
    placeHouse(context);

    context.runAtTick(
        10,
        () -> {
          final UnleashedDogEntity dog = sleepingDog(context, REL_ORIGIN);
          context.assertTrue(
              dog.getSleepController().isSleepingInBed(),
              "A dog must be able to sleep in a dog house");
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
              !occupant.get().getSleepController().isSleepingInBed(),
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

  @CustomTestProvider
  public List<TestFunction> breakingAnyCellDropsOneDyedHouse() {
    return java.util.Arrays.stream(DogHousePart.values())
        .map(
            part ->
                GeneratedGameTest.of(
                    "dog-house-drops",
                    "doghousegametest.breakingacelldropsonedyedhouse." + part.asString(),
                    "dogs-unleashed:dog_arena",
                    40,
                    context -> {
                      placeHouse(context);
                      dyeHouse(context, TEST_COLOR);

                      context.getWorld().breakBlock(context.getAbsolutePos(rel(part)), true);

                      context.addInstantFinalTask(
                          () -> {
                            final List<ItemStack> dropped =
                                DogTestHelper.droppedStacksOf(context, ModItems.DOG_HOUSE);
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
          final List<ItemStack> dropped =
              DogTestHelper.droppedStacksOf(context, ModItems.DOG_HOUSE);
          context.assertTrue(dropped.size() == 1, "expected exactly one dog house to drop");
          context.assertTrue(
              dropped.get(0).get(ModComponents.DOG_HOUSE_COLOR) == DyeColor.WHITE,
              "an undyed house should drop white");
        });
  }

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

  @GameTest(templateName = "dogs-unleashed:dog_arena", batchId = "dog-house", tickLimit = 40)
  public void placingADyedHouseColoursTheHouseThatAppears(final TestContext context) {
    final BlockPos relFloor = new BlockPos(1, 1, 1);
    final BlockPos relPlaced = relFloor.up();

    final ServerPlayerEntity player = DogTestHelper.mockPlayerStandingClearInArena(context);

    final ItemStack stack = new ItemStack(ModItems.DOG_HOUSE);
    stack.set(ModComponents.DOG_HOUSE_COLOR, DyeColor.ORANGE);
    DogTestHelper.placeStackOnTopOf(context, player, stack, relFloor);

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
