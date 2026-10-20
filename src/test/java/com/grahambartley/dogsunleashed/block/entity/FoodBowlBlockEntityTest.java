package com.grahambartley.dogsunleashed.block.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class FoodBowlBlockEntityTest {

  private static FoodBowlBlockEntity bowl() {
    return new FoodBowlBlockEntity(BlockPos.ORIGIN, ModBlocks.FOOD_BOWL.getDefaultState());
  }

  static Item[] foods() {
    return new Item[] {Items.BEEF, Items.COOKED_CHICKEN, Items.ROTTEN_FLESH};
  }

  static Item[] notFoods() {
    return new Item[] {Items.BONE, Items.BREAD, ModItems.DOG_TREAT, Items.WATER_BUCKET};
  }

  @ParameterizedTest(name = "adding {0} of a stack moves {1}")
  @CsvSource({"1, 1", "12, 12", "16, 16", "40, 16"})
  @DisplayName("filling by hand moves as much as fits and no more than sixteen")
  void addFoodStopsAtSixteen(final int offered, final int moved) {
    final FoodBowlBlockEntity bowl = bowl();
    assertEquals(moved, bowl.addFood(new ItemStack(Items.COOKED_BEEF, offered)));
    assertEquals(moved, bowl.foodCount());
  }

  @Test
  @DisplayName("mixed foods share the sixteen places")
  void mixedFoodsShareTheCapacity() {
    final FoodBowlBlockEntity bowl = bowl();
    bowl.addFood(new ItemStack(Items.BEEF, 10));
    assertEquals(6, bowl.addFood(new ItemStack(Items.CHICKEN, 10)));
    assertEquals(16, bowl.foodCount());
  }

  @ParameterizedTest(name = "{0} goes in the bowl")
  @MethodSource("foods")
  @DisplayName("hoppers can insert any dog food while there is room")
  void hoppersAcceptDogFood(final Item food) {
    final FoodBowlBlockEntity bowl = bowl();
    assertTrue(bowl.canInsert(0, new ItemStack(food), Direction.DOWN));
  }

  @ParameterizedTest(name = "{0} stays out of the bowl")
  @MethodSource("notFoods")
  @DisplayName("hoppers and players cannot put anything but dog food in")
  void rejectsOtherItems(final Item item) {
    final FoodBowlBlockEntity bowl = bowl();
    assertFalse(bowl.canInsert(0, new ItemStack(item), Direction.DOWN));
    assertEquals(0, bowl.addFood(new ItemStack(item, 4)));
  }

  @Test
  @DisplayName("a full bowl refuses more food from a hopper")
  void fullBowlRefusesHoppers() {
    final FoodBowlBlockEntity bowl = bowl();
    bowl.addFood(new ItemStack(Items.BEEF, 16));
    assertFalse(bowl.canInsert(3, new ItemStack(Items.BEEF), Direction.UP));
  }

  @ParameterizedTest(name = "a hopper below cannot pull from slot {0}")
  @CsvSource({"0", "15"})
  @DisplayName("food only leaves the bowl when a dog eats it")
  void hoppersCannotExtract(final int slot) {
    final FoodBowlBlockEntity bowl = bowl();
    bowl.addFood(new ItemStack(Items.BEEF, 16));
    assertFalse(bowl.canExtract(slot, bowl.getStack(slot), Direction.DOWN));
  }

  @Test
  @DisplayName("a dog takes exactly one food it can eat")
  void takeOneRemovesASingleEdibleFood() {
    final FoodBowlBlockEntity bowl = bowl();
    bowl.addFood(new ItemStack(Items.ROTTEN_FLESH, 2));
    bowl.addFood(new ItemStack(Items.BEEF, 3));
    final ItemStack taken = bowl.takeOne(stack -> stack.isOf(Items.BEEF));
    assertTrue(taken.isOf(Items.BEEF));
    assertEquals(1, taken.getCount());
    assertEquals(4, bowl.foodCount());
  }

  @Test
  @DisplayName("nothing is taken when the dog will not eat what is left")
  void takeOneLeavesFoodTheDogWillNotEat() {
    final FoodBowlBlockEntity bowl = bowl();
    bowl.addFood(new ItemStack(Items.ROTTEN_FLESH, 2));
    assertTrue(bowl.takeOne(stack -> stack.isOf(Items.BEEF)).isEmpty());
    assertFalse(bowl.hasFood(stack -> stack.isOf(Items.BEEF)));
    assertEquals(2, bowl.foodCount());
  }

  @ParameterizedTest(name = "{0} beef and {1} chicken survive a save and load")
  @CsvSource({"0, 0", "3, 0", "7, 9"})
  @DisplayName("the food in a bowl is kept when the world is saved")
  void foodSurvivesASaveAndLoad(final int beef, final int chicken) {
    final RegistryWrapper.WrapperLookup lookup = BuiltinRegistries.createWrapperLookup();
    final FoodBowlBlockEntity saved = bowl();
    saved.addFood(new ItemStack(Items.BEEF, beef));
    saved.addFood(new ItemStack(Items.CHICKEN, chicken));
    final FoodBowlBlockEntity loaded = bowl();
    loaded.read(saved.createNbt(lookup), lookup);
    assertEquals(beef + chicken, loaded.foodCount());
    assertEquals(beef > 0, loaded.hasFood(stack -> stack.isOf(Items.BEEF)));
    assertEquals(chicken > 0, loaded.hasFood(stack -> stack.isOf(Items.CHICKEN)));
  }
}
