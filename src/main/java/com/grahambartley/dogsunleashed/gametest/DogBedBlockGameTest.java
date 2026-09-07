package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.block.DogBedBlock;
import com.grahambartley.dogsunleashed.block.entity.DogBedBlockEntity;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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

public final class DogBedBlockGameTest implements FabricGameTest {

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dogBedCanBePlaced(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final BlockState placedState = world.getBlockState(absBedPos);
    context.assertTrue(placedState.isOf(ModBlocks.DOG_BED), "Dog bed should be placed");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dogBedHasBlockEntity(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final BlockEntity blockEntity = world.getBlockEntity(absBedPos);
    context.assertTrue(
        blockEntity instanceof DogBedBlockEntity, "Dog bed should have DogBedBlockEntity");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dogBedColorCanBeSetAndRetrieved(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    context.runAtTick(
        5,
        () -> {
          final BlockEntity blockEntity = world.getBlockEntity(absBedPos);
          context.assertTrue(
              blockEntity instanceof DogBedBlockEntity, "Block entity should be DogBedBlockEntity");
          final DogBedBlockEntity dogBedEntity = (DogBedBlockEntity) blockEntity;

          dogBedEntity.setColor(DyeColor.GREEN);
          context.assertTrue(
              dogBedEntity.getColor() == DyeColor.GREEN, "Dog bed color should be green after set");

          dogBedEntity.setColor(DyeColor.RED);
          context.assertTrue(
              dogBedEntity.getColor() == DyeColor.RED, "Dog bed color should be red after set");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dogBedColorCanBeChanged(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final BlockEntity blockEntity = world.getBlockEntity(absBedPos);
    context.assertTrue(
        blockEntity instanceof DogBedBlockEntity, "Block entity should be DogBedBlockEntity");
    final DogBedBlockEntity dogBedEntity = (DogBedBlockEntity) blockEntity;
    dogBedEntity.setColor(DyeColor.BLUE);
    context.assertTrue(
        dogBedEntity.getColor() == DyeColor.BLUE, "Dog bed color should be changed to blue");
    context.complete();
  }

  @GameTest(templateName = "dogs-unleashed:dog_bed_pair", tickLimit = 100)
  public void dogCanBeAssignedToBed(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final BlockPos relDogPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, relDogPos);
    husky.setTamed(true, true);
    husky.setAiDisabled(true); // Prevent wandering into other test structures

    context.runAtTick(
        10,
        () -> {
          final BlockEntity blockEntity = world.getBlockEntity(absBedPos);
          context.assertTrue(
              blockEntity instanceof DogBedBlockEntity, "Block entity should be DogBedBlockEntity");
          final DogBedBlockEntity dogBedEntity = (DogBedBlockEntity) blockEntity;

          dogBedEntity.setAssignedDog(husky);
          husky.setAssignedBedPos(absBedPos);

          context.assertTrue(dogBedEntity.hasAssignedDog(), "Bed should have assigned dog");
          context.assertTrue(husky.hasAssignedBed(), "Dog should have assigned bed");
          context.assertTrue(
              husky.getAssignedBedPos().isPresent()
                  && husky.getAssignedBedPos().get().equals(absBedPos),
              "Dog's assigned bed position should match");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dogCanBeCommandedToSleep(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final BlockPos relDogPos = new BlockPos(0, 1, 0);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, relDogPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(absBedPos);
          husky.getSleepController().startSleepingInBed(absBedPos);

          context.assertTrue(
              husky.isSleepingInBed(), "Dog should be sleeping after startSleepingInBed");
          context.assertTrue(
              husky.getAssignedBedPos().isPresent()
                  && husky.getAssignedBedPos().get().equals(absBedPos),
              "Dog should have bed position set");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 100)
  public void dogWakesUpWhenDamaged(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final BlockPos relDogPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, relDogPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(absBedPos);
          husky.getSleepController().startSleepingInBed(absBedPos);
          context.assertTrue(husky.isSleepingInBed(), "Dog should be sleeping");
        });

    context.runAtTick(
        20,
        () -> {
          husky.damage(world.getDamageSources().generic(), 1.0f);

          context.assertTrue(!husky.isSleepingInBed(), "Dog should wake up when damaged");
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_bed_pair", tickLimit = 100)
  public void clearAssignedBedWorks(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final BlockPos relDogPos = new BlockPos(0, 1, 0);

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, relDogPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          husky.setAssignedBedPos(absBedPos);
          context.assertTrue(husky.hasAssignedBed(), "Dog should have assigned bed");

          husky.getSleepController().clearAssignedBed();
          context.assertTrue(
              !husky.hasAssignedBed(), "Dog should not have assigned bed after clearAssignedBed");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void dogBedIsInAxeMineableTag(final TestContext context) {
    final BlockState dogBedState = ModBlocks.DOG_BED.getDefaultState();
    context.assertTrue(
        dogBedState.isIn(net.minecraft.registry.tag.BlockTags.AXE_MINEABLE),
        "Dog bed should be in axe mineable tag");
    context.complete();
  }

  // dogBedHasCorrectHardness and pendingAssignmentCanBeSetAndConsumed live in DogBedBlockTest
  // under src/test/java since they only read static block state and a process-global UUID map.
  // See gametest skill rule 10.

  @GameTest(templateName = "dogs-unleashed:dog_bed_pair", tickLimit = 100)
  public void reAssigningDogToNewBedClearsOldBed(final TestContext context) {
    // Two beds live at separate relative positions. EMPTY_STRUCTURE is 1x1x1; the structure
    // bounds are tracked by TestContext but setBlockState at an off-structure relative pos
    // still resolves via absolute coordinates and works for assertions. Per #210, this
    // test should eventually move to an .nbt template that covers the multi-bed footprint.
    final BlockPos relOldBedPos = new BlockPos(0, 1, 0);
    final BlockPos relNewBedPos = new BlockPos(3, 1, 0);
    final BlockPos relDogPos = new BlockPos(1, 1, 0);
    final BlockPos absOldBedPos = context.getAbsolutePos(relOldBedPos);
    final BlockPos absNewBedPos = context.getAbsolutePos(relNewBedPos);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relOldBedPos, ModBlocks.DOG_BED.getDefaultState());
    context.setBlockState(relNewBedPos, ModBlocks.DOG_BED.getDefaultState());

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, relDogPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          final BlockEntity oldBedBlockEntity = world.getBlockEntity(absOldBedPos);
          context.assertTrue(
              oldBedBlockEntity instanceof DogBedBlockEntity,
              "Old bed block entity should be DogBedBlockEntity");
          final DogBedBlockEntity oldBedEntity = (DogBedBlockEntity) oldBedBlockEntity;
          oldBedEntity.setAssignedDog(husky);
          husky.setAssignedBedPos(absOldBedPos);

          context.assertTrue(oldBedEntity.hasAssignedDog(), "Old bed should have assigned dog");

          final BlockEntity newBedBlockEntity = world.getBlockEntity(absNewBedPos);
          context.assertTrue(
              newBedBlockEntity instanceof DogBedBlockEntity,
              "New bed block entity should be DogBedBlockEntity");
          final DogBedBlockEntity newBedEntity = (DogBedBlockEntity) newBedBlockEntity;
          final BlockEntity oldBedCheckBlockEntity = world.getBlockEntity(absOldBedPos);
          context.assertTrue(
              oldBedCheckBlockEntity instanceof DogBedBlockEntity,
              "Old bed block entity should still be DogBedBlockEntity");
          final DogBedBlockEntity oldBedCheck = (DogBedBlockEntity) oldBedCheckBlockEntity;

          husky
              .getAssignedBedPos()
              .ifPresent(
                  pos -> {
                    if (world.getBlockEntity(pos) instanceof DogBedBlockEntity oldEntity) {
                      oldEntity.clearAssignedDog(null);
                    }
                  });
          newBedEntity.setAssignedDog(husky);
          husky.setAssignedBedPos(absNewBedPos);

          context.assertTrue(newBedEntity.hasAssignedDog(), "New bed should have assigned dog");
          context.assertTrue(
              !oldBedCheck.hasAssignedDog(), "Old bed should no longer have assigned dog");
          context.assertTrue(
              husky.getAssignedBedPos().isPresent()
                  && husky.getAssignedBedPos().get().equals(absNewBedPos),
              "Dog's bed pos should be new bed");
          context.complete();
        });
  }

  @GameTest(templateName = "dogs-unleashed:dog_bed_pair", tickLimit = 100)
  public void dogDeathFreesAssignedBed(final TestContext context) {
    final BlockPos relBedPos = new BlockPos(0, 1, 0);
    final BlockPos absBedPos = context.getAbsolutePos(relBedPos);
    final BlockPos relDogPos = new BlockPos(0, 1, 0);
    final ServerWorld world = context.getWorld();

    context.setBlockState(relBedPos, ModBlocks.DOG_BED.getDefaultState());

    final UnleashedDogEntity husky = context.spawnEntity(ModEntities.HUSKY, relDogPos);
    husky.setTamed(true, true);

    context.runAtTick(
        10,
        () -> {
          final BlockEntity bedBlockEntity = world.getBlockEntity(absBedPos);
          context.assertTrue(
              bedBlockEntity instanceof DogBedBlockEntity,
              "Bed block entity should be DogBedBlockEntity");
          final DogBedBlockEntity bedEntity = (DogBedBlockEntity) bedBlockEntity;
          bedEntity.setAssignedDog(husky);
          husky.setAssignedBedPos(absBedPos);

          context.assertTrue(bedEntity.hasAssignedDog(), "Bed should have assigned dog");
        });

    context.runAtTick(
        20,
        () -> {
          husky.damage(world.getDamageSources().generic(), 999.0f);
        });

    context.runAtTick(
        30,
        () -> {
          final BlockEntity bedBlockEntityAtTick30 = world.getBlockEntity(absBedPos);
          context.assertTrue(
              bedBlockEntityAtTick30 instanceof DogBedBlockEntity,
              "Bed block entity should still be a DogBedBlockEntity");
          final DogBedBlockEntity bedEntity = (DogBedBlockEntity) bedBlockEntityAtTick30;
          context.assertTrue(!bedEntity.hasAssignedDog(), "Bed should be free after dog death");
          context.complete();
        });
  }

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos REL_FLOOR = new BlockPos(1, 1, 1);
  private static final BlockPos REL_BED = REL_FLOOR.up();
  private static final List<DyeColor> DROP_TEST_COLORS = List.of(DyeColor.MAGENTA, DyeColor.LIME);

  private static DogBedBlockEntity placeDyedBed(final TestContext context, final DyeColor color) {
    context.setBlockState(REL_BED, ModBlocks.DOG_BED.getDefaultState());
    final DogBedBlockEntity bedBlockEntity = context.getBlockEntity(REL_BED);
    bedBlockEntity.setColor(color);
    return bedBlockEntity;
  }

  private static List<ItemStack> bedsDroppedIn(final TestContext context) {
    final Box searchBox = context.getTestBox().expand(2.0);
    return context
        .getWorld()
        .getEntitiesByClass(ItemEntity.class, searchBox, entity -> true)
        .stream()
        .map(ItemEntity::getStack)
        .filter(stack -> stack.isOf(ModItems.DOG_BED))
        .toList();
  }

  /**
   * The loot table copies the colour off the block entity's component map, so a bed that never
   * publishes the component drops undyed and the player silently loses the colour they dyed.
   */
  @CustomTestProvider
  public List<TestFunction> breakingADyedBedDropsThatColour() {
    return DROP_TEST_COLORS.stream()
        .map(
            color ->
                new TestFunction(
                    "dog-bed-drops",
                    "dogbedblockgametest.breakingadyedbeddropsthatcolour." + color.getName(),
                    ARENA,
                    40,
                    0L,
                    true,
                    context -> {
                      placeDyedBed(context, color);
                      context.getWorld().breakBlock(context.getAbsolutePos(REL_BED), true);

                      context.addInstantFinalTask(
                          () -> {
                            final List<ItemStack> dropped = bedsDroppedIn(context);
                            context.assertTrue(
                                dropped.size() == 1,
                                "expected exactly one dog bed to drop but got " + dropped.size());
                            context.assertTrue(
                                dropped.get(0).get(ModComponents.DOG_BED_COLOR) == color,
                                "the dropped bed lost the colour it was dyed");
                          });
                    }))
        .toList();
  }

  @GameTest(templateName = ARENA, batchId = "dog-bed-colour", tickLimit = 40)
  public void anUndyedBedDropsAWhiteBed(final TestContext context) {
    context.setBlockState(REL_BED, ModBlocks.DOG_BED.getDefaultState());
    context.getWorld().breakBlock(context.getAbsolutePos(REL_BED), true);

    context.addInstantFinalTask(
        () -> {
          final List<ItemStack> dropped = bedsDroppedIn(context);
          context.assertTrue(dropped.size() == 1, "expected exactly one dog bed to drop");
          context.assertTrue(
              dropped.get(0).getOrDefault(ModComponents.DOG_BED_COLOR, DyeColor.WHITE)
                  == DyeColor.WHITE,
              "an undyed bed should drop white");
        });
  }

  /**
   * The colour rides from the recipe to the placed bed on the item component, so this places a real
   * dyed stack the way a player does rather than setting the colour on the block entity.
   */
  @GameTest(templateName = ARENA, batchId = "dog-bed-colour", tickLimit = 40)
  public void placingADyedBedColoursTheBedThatAppears(final TestContext context) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    final Vec3d standingClear = Vec3d.ofBottomCenter(context.getAbsolutePos(new BlockPos(5, 2, 5)));
    player.refreshPositionAndAngles(standingClear.x, standingClear.y, standingClear.z, 0.0f, 0.0f);

    final ItemStack stack = new ItemStack(ModItems.DOG_BED);
    stack.set(ModComponents.DOG_BED_COLOR, DyeColor.ORANGE);
    player.setStackInHand(Hand.MAIN_HAND, stack);

    final BlockPos absFloor = context.getAbsolutePos(REL_FLOOR);
    final BlockHitResult hit =
        new BlockHitResult(Vec3d.ofCenter(absFloor), Direction.UP, absFloor, false);
    stack.useOnBlock(new ItemUsageContext(context.getWorld(), player, Hand.MAIN_HAND, stack, hit));

    context.addInstantFinalTask(
        () -> {
          context.assertTrue(
              context.getBlockState(REL_BED).isOf(ModBlocks.DOG_BED), "the dyed bed did not place");
          final DogBedBlockEntity bedBlockEntity = context.getBlockEntity(REL_BED);
          context.assertTrue(
              bedBlockEntity.getColor() == DyeColor.ORANGE,
              "a bed placed from an orange stack came out " + bedBlockEntity.getColor().getName());
        });
  }

  /** Middle-clicking a dyed bed in creative has to hand back a bed of that same colour. */
  @GameTest(templateName = ARENA, batchId = "dog-bed-colour", tickLimit = 40)
  public void middleClickingADyedBedYieldsThatColour(final TestContext context) {
    placeDyedBed(context, DyeColor.PURPLE);

    final ItemStack picked =
        ((DogBedBlock) ModBlocks.DOG_BED)
            .getPickStack(
                context.getWorld(),
                context.getAbsolutePos(REL_BED),
                context.getBlockState(REL_BED));

    context.addInstantFinalTask(
        () -> {
          context.assertTrue(
              picked.isOf(ModItems.DOG_BED), "picking a dog bed gave something else");
          context.assertTrue(
              picked.get(ModComponents.DOG_BED_COLOR) == DyeColor.PURPLE,
              "the picked bed lost the colour it was dyed");
        });
  }

  /** A colour that does not survive a save and reload is a colour the player loses on relog. */
  @GameTest(templateName = ARENA, batchId = "dog-bed-colour", tickLimit = 40)
  public void theCushionColourSurvivesAnNbtRoundTrip(final TestContext context) {
    final DogBedBlockEntity bedBlockEntity = placeDyedBed(context, DyeColor.CYAN);
    final NbtCompound nbt = bedBlockEntity.createNbt(context.getWorld().getRegistryManager());

    final DogBedBlockEntity reloaded =
        new DogBedBlockEntity(BlockPos.ORIGIN, ModBlocks.DOG_BED.getDefaultState());
    reloaded.read(nbt, context.getWorld().getRegistryManager());

    context.addInstantFinalTask(
        () ->
            context.assertTrue(
                reloaded.getColor() == DyeColor.CYAN,
                "the cushion colour was lost across a save and reload"));
  }
}
