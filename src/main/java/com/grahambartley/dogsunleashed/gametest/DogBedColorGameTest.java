package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.block.DogBedBlock;
import com.grahambartley.dogsunleashed.block.entity.DogBedBlockEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.CustomTestProvider;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class DogBedColorGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final BlockPos REL_FLOOR = new BlockPos(1, 1, 1);
  private static final BlockPos REL_BED = REL_FLOOR.up();
  private static final BlockPos REL_STANDING_CLEAR = new BlockPos(5, 2, 5);
  private static final List<DyeColor> DROP_TEST_COLORS = List.of(DyeColor.MAGENTA, DyeColor.LIME);

  private static DogBedBlockEntity placeDyedBed(final TestContext context, final DyeColor color) {
    context.setBlockState(REL_BED, ModBlocks.DOG_BED.getDefaultState());
    final DogBedBlockEntity bedBlockEntity = context.getBlockEntity(REL_BED);
    bedBlockEntity.setColor(color);
    return bedBlockEntity;
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
                    "dogbedcolorgametest.breakingadyedbeddropsthatcolour." + color.getName(),
                    ARENA,
                    40,
                    0L,
                    true,
                    context -> {
                      placeDyedBed(context, color);
                      context.getWorld().breakBlock(context.getAbsolutePos(REL_BED), true);

                      context.addInstantFinalTask(
                          () -> {
                            final List<ItemStack> dropped =
                                DogTestHelper.droppedStacksOf(context, ModItems.DOG_BED);
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

  /**
   * The crafting recipe stamps an explicit white component, so an undyed bed has to drop with that
   * same component rather than none at all, or the two will not stack.
   */
  @GameTest(templateName = ARENA, batchId = "dog-bed-colour", tickLimit = 40)
  public void anUndyedBedDropsAWhiteBed(final TestContext context) {
    context.setBlockState(REL_BED, ModBlocks.DOG_BED.getDefaultState());
    context.getWorld().breakBlock(context.getAbsolutePos(REL_BED), true);

    context.addInstantFinalTask(
        () -> {
          final List<ItemStack> dropped = DogTestHelper.droppedStacksOf(context, ModItems.DOG_BED);
          context.assertTrue(dropped.size() == 1, "expected exactly one dog bed to drop");
          context.assertTrue(
              dropped.get(0).get(ModComponents.DOG_BED_COLOR) == DyeColor.WHITE,
              "an undyed bed should drop white");
        });
  }

  @GameTest(templateName = ARENA, batchId = "dog-bed-colour", tickLimit = 40)
  public void placingADyedBedColoursTheBedThatAppears(final TestContext context) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    final Vec3d standingClear = Vec3d.ofBottomCenter(context.getAbsolutePos(REL_STANDING_CLEAR));
    player.refreshPositionAndAngles(standingClear.x, standingClear.y, standingClear.z, 0.0f, 0.0f);

    final ItemStack stack = new ItemStack(ModItems.DOG_BED);
    stack.set(ModComponents.DOG_BED_COLOR, DyeColor.ORANGE);
    DogTestHelper.placeStackOnTopOf(context, player, stack, REL_FLOOR);

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
