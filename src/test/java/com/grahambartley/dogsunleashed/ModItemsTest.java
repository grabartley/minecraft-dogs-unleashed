package com.grahambartley.dogsunleashed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.WoodType;
import net.minecraft.component.ComponentType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ModItemsTest {

  static Stream<Arguments> blockItems() {
    return Stream.of(
        Arguments.of("food_bowl", ModItems.FOOD_BOWL, ModBlocks.FOOD_BOWL),
        Arguments.of("water_bowl", ModItems.WATER_BOWL, ModBlocks.WATER_BOWL));
  }

  @ParameterizedTest(name = "dogs-unleashed:{0} places its block")
  @MethodSource("blockItems")
  @DisplayName("each block item is registered under its block's id and places that block")
  void blockItemPlacesItsBlock(final String path, final Item item, final Block block) {
    assertSame(item, Registries.ITEM.get(Identifier.of(DogsUnleashed.MOD_ID, path)));
    assertSame(block, assertInstanceOf(BlockItem.class, item).getBlock());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName(
      "every spawning breed has a spawn egg for its own entity, and the cross-breed has none")
  void spawnEggMatchesItsBreed(final UnleashedDogBreed breed) {
    final Item egg = ModItems.getSpawnEgg(breed);
    if (breed == UnleashedDogBreed.CROSS_BREED) {
      assertNull(egg);
      return;
    }
    assertSame(
        egg,
        Registries.ITEM.get(
            Identifier.of(DogsUnleashed.MOD_ID, breed.serializedId() + "_spawn_egg")));
    assertEquals(
        ModEntities.getDogEntityType(breed),
        assertInstanceOf(SpawnEggItem.class, egg).getEntityType(egg.getDefaultStack()));
  }

  static Stream<Arguments> furniture() {
    return Stream.of(
        Arguments.of("dog_house", ModItems.DOG_HOUSE, ModComponents.DOG_HOUSE_COLOR),
        Arguments.of("dog_bed", ModItems.DOG_BED, ModComponents.DOG_BED_COLOR),
        Arguments.of("sled", ModItems.SLED, null));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("furniture")
  @DisplayName("an oak, white item stacks with a plain one, as crafted, mined and old items do")
  void oakAndWhiteAreTheDefaults(
      final String path, final Item item, final ComponentType<DyeColor> colour) {
    final ItemStack marked = new ItemStack(item);
    marked.set(ModComponents.WOOD, WoodType.OAK);
    if (colour != null) {
      marked.set(colour, DyeColor.WHITE);
    }

    assertTrue(ItemStack.areItemsAndComponentsEqual(new ItemStack(item), marked));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("furniture")
  @DisplayName("an item in another wood keeps it apart from oak")
  void otherWoodsStayApart(
      final String path, final Item item, final ComponentType<DyeColor> colour) {
    final ItemStack spruce = new ItemStack(item);
    spruce.set(ModComponents.WOOD, WoodType.SPRUCE);

    assertFalse(ItemStack.areItemsAndComponentsEqual(new ItemStack(item), spruce));
  }
}
