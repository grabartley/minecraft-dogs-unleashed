package com.grahambartley.dogsunleashed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.dogsunleashed.block.DogStatueBlock;
import com.grahambartley.dogsunleashed.block.StatueMaterial;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.Oxidizable;
import net.minecraft.item.BlockItem;
import net.minecraft.item.HoneycombItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ModStatueBlocksTest {

  static Stream<Arguments> bases() {
    return Stream.of(
        Arguments.of("statue_base_stone", ModStatueBlocks.STONE, StatueMaterial.STONE),
        Arguments.of("statue_base_gold", ModStatueBlocks.GOLD, StatueMaterial.GOLD),
        Arguments.of("statue_base_copper", ModStatueBlocks.COPPER, StatueMaterial.COPPER),
        Arguments.of(
            "statue_base_exposed_copper",
            ModStatueBlocks.EXPOSED_COPPER,
            StatueMaterial.EXPOSED_COPPER),
        Arguments.of(
            "statue_base_weathered_copper",
            ModStatueBlocks.WEATHERED_COPPER,
            StatueMaterial.WEATHERED_COPPER),
        Arguments.of(
            "statue_base_oxidized_copper",
            ModStatueBlocks.OXIDIZED_COPPER,
            StatueMaterial.OXIDIZED_COPPER),
        Arguments.of(
            "statue_base_waxed_copper", ModStatueBlocks.WAXED_COPPER, StatueMaterial.COPPER),
        Arguments.of(
            "statue_base_waxed_exposed_copper",
            ModStatueBlocks.WAXED_EXPOSED_COPPER,
            StatueMaterial.EXPOSED_COPPER),
        Arguments.of(
            "statue_base_waxed_weathered_copper",
            ModStatueBlocks.WAXED_WEATHERED_COPPER,
            StatueMaterial.WEATHERED_COPPER),
        Arguments.of(
            "statue_base_waxed_oxidized_copper",
            ModStatueBlocks.WAXED_OXIDIZED_COPPER,
            StatueMaterial.OXIDIZED_COPPER));
  }

  static Stream<Arguments> ageing() {
    return Stream.of(
        Arguments.of(ModStatueBlocks.COPPER, ModStatueBlocks.EXPOSED_COPPER),
        Arguments.of(ModStatueBlocks.EXPOSED_COPPER, ModStatueBlocks.WEATHERED_COPPER),
        Arguments.of(ModStatueBlocks.WEATHERED_COPPER, ModStatueBlocks.OXIDIZED_COPPER));
  }

  static Stream<Arguments> waxing() {
    return Stream.of(
        Arguments.of(ModStatueBlocks.COPPER, ModStatueBlocks.WAXED_COPPER),
        Arguments.of(ModStatueBlocks.EXPOSED_COPPER, ModStatueBlocks.WAXED_EXPOSED_COPPER),
        Arguments.of(ModStatueBlocks.WEATHERED_COPPER, ModStatueBlocks.WAXED_WEATHERED_COPPER),
        Arguments.of(ModStatueBlocks.OXIDIZED_COPPER, ModStatueBlocks.WAXED_OXIDIZED_COPPER));
  }

  @ParameterizedTest(name = "dogs-unleashed:{0} carves in {2}")
  @MethodSource("bases")
  @DisplayName("each base is registered with a matching item and carves in its own material")
  void baseIsRegistered(final String path, final Block block, final StatueMaterial material) {
    final Identifier id = Identifier.of(DogsUnleashed.MOD_ID, path);
    assertSame(block, Registries.BLOCK.get(id));
    final BlockItem item = assertInstanceOf(BlockItem.class, Registries.ITEM.get(id));
    assertSame(block, item.getBlock());
    assertEquals(material, ((DogStatueBlock) block).material());
  }

  @ParameterizedTest(name = "{0} ages into {1}")
  @MethodSource("ageing")
  @DisplayName("copper bases age one vanilla stage at a time and scrape back the same way")
  void copperAgesLikeVanilla(final Block younger, final Block older) {
    assertEquals(Optional.of(older), Oxidizable.getIncreasedOxidationBlock(younger));
    assertEquals(Optional.of(younger), Oxidizable.getDecreasedOxidationBlock(older));
  }

  @ParameterizedTest(name = "{0} waxes into {1}")
  @MethodSource("waxing")
  @DisplayName("honeycomb waxes each copper base and an axe takes the wax back off")
  void copperWaxesLikeVanilla(final Block unwaxed, final Block waxed) {
    assertEquals(
        Optional.of(waxed.getDefaultState()),
        HoneycombItem.getWaxedState(unwaxed.getDefaultState()));
    assertEquals(unwaxed, HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().get(waxed));
  }

  @Test
  @DisplayName("fully oxidized and waxed bases stop ageing")
  void endOfTheLineStopsAgeing() {
    assertEquals(
        Optional.empty(), Oxidizable.getIncreasedOxidationBlock(ModStatueBlocks.OXIDIZED_COPPER));
    assertEquals(
        Optional.empty(), Oxidizable.getIncreasedOxidationBlock(ModStatueBlocks.WAXED_COPPER));
  }

  @Test
  @DisplayName("all() lists every base exactly once")
  void allListsEveryBase() {
    assertEquals(bases().count(), ModStatueBlocks.all().stream().distinct().count());
    assertEquals(bases().count(), ModStatueBlocks.all().size());
  }
}
