package com.grahambartley.dogsunleashed.gametest;

import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.ModItems;
import com.grahambartley.dogsunleashed.block.DogHouseLayout;
import com.grahambartley.dogsunleashed.block.DogHousePart;
import com.grahambartley.dogsunleashed.block.WoodPlanks;
import com.grahambartley.dogsunleashed.block.entity.DogBedBlockEntity;
import com.grahambartley.dogsunleashed.block.entity.DogHouseBlockEntity;
import com.grahambartley.dogsunleashed.datagen.WoodenFurniture;
import com.grahambartley.dogsunleashed.datagen.WoodenFurnitureRecipeProvider;
import com.grahambartley.dogsunleashed.entity.sled.SledEntity;
import com.grahambartley.dogsunleashed.gametest.util.DogHouseTestHelper;
import com.grahambartley.dogsunleashed.gametest.util.DogTestHelper;
import com.grahambartley.dogsunleashed.gametest.util.SurvivalTestPlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.WoodType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class WoodenFurnitureGameTest implements FabricGameTest {

  private static final String ARENA = "dogs-unleashed:dog_arena";
  private static final String BATCH = "wooden-furniture";
  private static final BlockPos REL_BED = new BlockPos(1, 2, 1);
  private static final BlockPos REL_HOUSE = new BlockPos(2, 2, 2);
  private static final Direction FACING = Direction.NORTH;
  private static final Vec3d REL_SLED = new Vec3d(2.5, 2, 2.5);
  private static final Vec3d REL_PLAYER = new Vec3d(2.5, 2, 4.5);
  private static final int SLED_BREAKING_HITS = 5;
  private static final float LOOK_DOWN = 45.0f;
  private static final float LOOK_NORTH = 180.0f;

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void everyWoodsRecipeCraftsThatWood(final TestContext context) {
    for (final WoodenFurniture piece : WoodenFurnitureRecipeProvider.furniture()) {
      for (final WoodType wood : WoodPlanks.all()) {
        final ItemStack result = craft(context, grid(piece, wood));
        context.assertTrue(
            result.isOf(piece.item()) && result.get(ModComponents.WOOD) == wood,
            wood.name() + " planks in the " + piece.group() + " pattern made " + result);
      }
    }
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = 20)
  public void dyeingAHouseInTheGridKeepsItsWood(final TestContext context) {
    final ItemStack house = new ItemStack(ModItems.DOG_HOUSE);
    house.set(ModComponents.WOOD, WoodType.CHERRY);

    final ItemStack result =
        craft(context, input(List.of(house, new ItemStack(Items.BLUE_DYE)), 2, 1));

    context.assertTrue(
        result.get(ModComponents.DOG_HOUSE_COLOR) == DyeColor.BLUE,
        "the dyed house is " + result.get(ModComponents.DOG_HOUSE_COLOR));
    context.assertTrue(
        result.get(ModComponents.WOOD) == WoodType.CHERRY,
        "dyeing turned the cherry house into " + result.get(ModComponents.WOOD));
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 40)
  public void aBrokenBedDropsItsWood(final TestContext context) {
    context.setBlockState(REL_BED, ModBlocks.DOG_BED.getDefaultState());
    final DogBedBlockEntity bed = context.getBlockEntity(REL_BED);
    bed.setWood(WoodType.SPRUCE);
    context.getWorld().breakBlock(context.getAbsolutePos(REL_BED), true);

    context.addInstantFinalTask(() -> expectOneDropOf(context, ModItems.DOG_BED, WoodType.SPRUCE));
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 40)
  public void aHouseBrokenAtAnyCellDropsItsWood(final TestContext context) {
    DogHouseTestHelper.placeHouse(context, REL_HOUSE, FACING);
    final DogHouseBlockEntity house = context.getBlockEntity(REL_HOUSE);
    house.setWood(WoodType.MANGROVE);
    final BlockPos roofCell =
        REL_HOUSE.add(DogHouseLayout.offsetFromOrigin(DogHousePart.BACK_RIGHT_UPPER, FACING));
    context.getWorld().breakBlock(context.getAbsolutePos(roofCell), true);

    context.addInstantFinalTask(
        () -> expectOneDropOf(context, ModItems.DOG_HOUSE, WoodType.MANGROVE));
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 40)
  public void aBrokenSledDropsItsWood(final TestContext context) {
    final SledEntity sled = context.spawnEntity(ModEntities.SLED, REL_SLED);
    sled.setWood(WoodType.BIRCH);
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    for (int hit = 0; hit < SLED_BREAKING_HITS && !sled.isRemoved(); hit++) {
      sled.damage(context.getWorld().getDamageSources().playerAttack(player), 1.0f);
    }

    SurvivalTestPlayer.leave(context, player);

    context.addInstantFinalTask(() -> expectOneDropOf(context, ModItems.SLED, WoodType.BIRCH));
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 20)
  public void aSledPlacedFromAnItemKeepsItsWoodThroughASave(final TestContext context) {
    final ServerPlayerEntity player = SurvivalTestPlayer.joinAt(context, REL_PLAYER);
    player.setPitch(LOOK_DOWN);
    player.setYaw(LOOK_NORTH);
    final ItemStack item = new ItemStack(ModItems.SLED);
    item.set(ModComponents.WOOD, WoodType.ACACIA);
    player.setStackInHand(Hand.MAIN_HAND, item);

    ModItems.SLED.use(context.getWorld(), player, Hand.MAIN_HAND);
    SurvivalTestPlayer.leave(context, player);

    final List<SledEntity> sleds =
        context.getWorld().getEntitiesByClass(SledEntity.class, context.getTestBox(), sled -> true);
    context.assertTrue(sleds.size() == 1, "expected one placed sled but got " + sleds.size());
    final NbtCompound saved = new NbtCompound();
    sleds.get(0).writeNbt(saved);
    final SledEntity loaded = new SledEntity(ModEntities.SLED, context.getWorld());
    loaded.readNbt(saved);
    context.assertTrue(
        sleds.get(0).wood() == WoodType.ACACIA, "the placed sled is " + sleds.get(0).wood().name());
    context.assertTrue(
        loaded.wood() == WoodType.ACACIA, "the loaded sled is " + loaded.wood().name());
    context.complete();
  }

  private static void expectOneDropOf(
      final TestContext context, final Item item, final WoodType wood) {
    final List<ItemStack> dropped = DogTestHelper.droppedStacksOf(context, item);
    context.assertTrue(dropped.size() == 1, "expected one drop but got " + dropped.size());
    context.assertTrue(
        dropped.get(0).get(ModComponents.WOOD) == wood,
        "the drop lost its " + wood.name() + " wood: " + dropped.get(0).get(ModComponents.WOOD));
  }

  private static CraftingRecipeInput grid(final WoodenFurniture piece, final WoodType wood) {
    final Map<Character, Ingredient> key =
        piece.keyWith(Ingredient.ofItems(WoodPlanks.planksOf(wood)));
    final List<ItemStack> slots = new ArrayList<>();
    for (final String row : piece.pattern()) {
      for (final char symbol : row.toCharArray()) {
        slots.add(symbol == ' ' ? ItemStack.EMPTY : key.get(symbol).getMatchingStacks()[0].copy());
      }
    }
    return input(slots, piece.pattern().get(0).length(), piece.pattern().size());
  }

  private static CraftingRecipeInput input(
      final List<ItemStack> slots, final int width, final int height) {
    return CraftingRecipeInput.create(width, height, slots);
  }

  private static ItemStack craft(final TestContext context, final CraftingRecipeInput input) {
    final Optional<RecipeEntry<CraftingRecipe>> recipe =
        context
            .getWorld()
            .getRecipeManager()
            .getFirstMatch(RecipeType.CRAFTING, input, context.getWorld());
    return recipe
        .map(entry -> entry.value().craft(input, context.getWorld().getRegistryManager()))
        .orElse(ItemStack.EMPTY);
  }
}
