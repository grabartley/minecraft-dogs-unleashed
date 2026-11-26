package com.grahambartley.dogsunleashed.block.entity;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.ModItems;
import net.minecraft.block.WoodType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogBedBlockEntityTest {

  private static final BlockPos POS = new BlockPos(1, 70, 2);

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"oak", "spruce", "cherry", "crimson", "bamboo"})
  @DisplayName("a placed dog bed takes its wood from the item and gives it back when broken")
  void woodTravelsBetweenItemAndBlock(final String name) {
    final WoodType wood = woodNamed(name);
    final ItemStack placed = new ItemStack(ModItems.DOG_BED);
    placed.set(ModComponents.WOOD, wood);
    final DogBedBlockEntity entity = entity();

    entity.readComponents(placed);

    assertSame(wood, entity.getWood());
    assertSame(wood, entity.createComponentMap().get(ModComponents.WOOD));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"oak", "birch", "dark_oak", "warped", "mangrove"})
  @DisplayName("a dog bed's wood survives a save and reload")
  void woodSurvivesSaving(final String name) {
    final DogBedBlockEntity saved = entity();
    saved.setWood(woodNamed(name));
    final NbtCompound nbt = new NbtCompound();
    saved.writeNbt(nbt, null);
    final DogBedBlockEntity loaded = entity();

    loaded.readNbt(nbt, null);

    assertSame(woodNamed(name), loaded.getWood());
  }

  private static DogBedBlockEntity entity() {
    return new DogBedBlockEntity(POS, ModBlocks.DOG_BED.getDefaultState());
  }

  private static WoodType woodNamed(final String name) {
    return WoodType.stream().filter(wood -> wood.name().equals(name)).findFirst().orElseThrow();
  }
}
