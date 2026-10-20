package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModItems;
import java.util.stream.Stream;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogFoodsTest {

  static Stream<Arguments> items() {
    return Stream.of(
        Arguments.of(Items.COOKED_BEEF, true, true, true),
        Arguments.of(Items.ROTTEN_FLESH, true, true, true),
        Arguments.of(Items.BONE, false, true, true),
        Arguments.of(ModItems.DOG_TREAT, false, false, true),
        Arguments.of(Items.BREAD, false, false, false));
  }

  @ParameterizedTest(name = "{0}: food {1}, tames {2}, gets attention {3}")
  @MethodSource("items")
  @DisplayName("each item sorts into the food, taming and attention sets it belongs to")
  void sortsItemsIntoTheirSets(
      final Item item, final boolean food, final boolean taming, final boolean attention) {
    final ItemStack stack = new ItemStack(item);
    assertEquals(food, DogFoods.isBreedingItem(stack));
    assertEquals(taming, DogFoods.isTamingItem(stack));
    assertEquals(attention, DogFoods.isAttentionItem(stack));
  }

  @Test
  @DisplayName("one food heals a dog by a full heart, by hand or from a bowl")
  void oneFoodHealsAHeart() {
    assertEquals(2.0F, DogFoods.HEAL_AMOUNT);
  }
}
