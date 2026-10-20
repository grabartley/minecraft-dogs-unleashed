package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.block.DogStatueBlock;
import com.grahambartley.dogsunleashed.block.OxidizableDogStatueBlock;
import com.grahambartley.dogsunleashed.block.StatueMaterial;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Oxidizable;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModStatueBlocks {

  private static final List<Block> BLOCKS = new ArrayList<>();

  public static final Block STONE =
      register(
          "statue_base_stone",
          new DogStatueBlock(StatueMaterial.STONE, settingsOf(Blocks.SMOOTH_STONE_SLAB)));

  public static final Block GOLD =
      register(
          "statue_base_gold",
          new DogStatueBlock(StatueMaterial.GOLD, settingsOf(Blocks.GOLD_BLOCK)));

  public static final Block COPPER =
      register(
          "statue_base_copper",
          new OxidizableDogStatueBlock(
              Oxidizable.OxidationLevel.UNAFFECTED,
              StatueMaterial.COPPER,
              settingsOf(Blocks.CUT_COPPER_SLAB)));

  public static final Block EXPOSED_COPPER =
      register(
          "statue_base_exposed_copper",
          new OxidizableDogStatueBlock(
              Oxidizable.OxidationLevel.EXPOSED,
              StatueMaterial.EXPOSED_COPPER,
              settingsOf(Blocks.EXPOSED_CUT_COPPER_SLAB)));

  public static final Block WEATHERED_COPPER =
      register(
          "statue_base_weathered_copper",
          new OxidizableDogStatueBlock(
              Oxidizable.OxidationLevel.WEATHERED,
              StatueMaterial.WEATHERED_COPPER,
              settingsOf(Blocks.WEATHERED_CUT_COPPER_SLAB)));

  public static final Block OXIDIZED_COPPER =
      register(
          "statue_base_oxidized_copper",
          new OxidizableDogStatueBlock(
              Oxidizable.OxidationLevel.OXIDIZED,
              StatueMaterial.OXIDIZED_COPPER,
              settingsOf(Blocks.OXIDIZED_CUT_COPPER_SLAB)));

  public static final Block WAXED_COPPER =
      register(
          "statue_base_waxed_copper",
          new DogStatueBlock(StatueMaterial.COPPER, settingsOf(Blocks.WAXED_CUT_COPPER_SLAB)));

  public static final Block WAXED_EXPOSED_COPPER =
      register(
          "statue_base_waxed_exposed_copper",
          new DogStatueBlock(
              StatueMaterial.EXPOSED_COPPER, settingsOf(Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB)));

  public static final Block WAXED_WEATHERED_COPPER =
      register(
          "statue_base_waxed_weathered_copper",
          new DogStatueBlock(
              StatueMaterial.WEATHERED_COPPER, settingsOf(Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB)));

  public static final Block WAXED_OXIDIZED_COPPER =
      register(
          "statue_base_waxed_oxidized_copper",
          new DogStatueBlock(
              StatueMaterial.OXIDIZED_COPPER, settingsOf(Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB)));

  private ModStatueBlocks() {}

  public static List<Block> all() {
    return List.copyOf(BLOCKS);
  }

  private static AbstractBlock.Settings settingsOf(final Block vanilla) {
    return AbstractBlock.Settings.copy(vanilla);
  }

  private static Block register(final String path, final Block block) {
    final Identifier id = Identifier.of(DogsUnleashed.MOD_ID, path);
    Registry.register(Registries.BLOCK, id, block);
    Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
    BLOCKS.add(block);
    return block;
  }

  public static void initialize() {
    OxidizableBlocksRegistry.registerOxidizableBlockPair(COPPER, EXPOSED_COPPER);
    OxidizableBlocksRegistry.registerOxidizableBlockPair(EXPOSED_COPPER, WEATHERED_COPPER);
    OxidizableBlocksRegistry.registerOxidizableBlockPair(WEATHERED_COPPER, OXIDIZED_COPPER);
    OxidizableBlocksRegistry.registerWaxableBlockPair(COPPER, WAXED_COPPER);
    OxidizableBlocksRegistry.registerWaxableBlockPair(EXPOSED_COPPER, WAXED_EXPOSED_COPPER);
    OxidizableBlocksRegistry.registerWaxableBlockPair(WEATHERED_COPPER, WAXED_WEATHERED_COPPER);
    OxidizableBlocksRegistry.registerWaxableBlockPair(OXIDIZED_COPPER, WAXED_OXIDIZED_COPPER);
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
        .register(entries -> BLOCKS.forEach(entries::add));
  }
}
