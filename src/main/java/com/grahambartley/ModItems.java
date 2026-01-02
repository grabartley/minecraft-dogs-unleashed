package com.grahambartley;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

  public static final Item DOG_BED =
      Registry.register(
          Registries.ITEM,
          Identifier.of(DogsUnleashed.MOD_ID, "dog_bed"),
          new BlockItem(ModBlocks.DOG_BED, new Item.Settings()));

  public static final Item HUSKY_SPAWN_EGG =
      Registry.register(
          Registries.ITEM,
          Identifier.of(DogsUnleashed.MOD_ID, "husky_spawn_egg"),
          new SpawnEggItem(
              ModEntities.HUSKY,
              ModConstants.HUSKY_SPAWN_EGG_PRIMARY_COLOR,
              ModConstants.HUSKY_SPAWN_EGG_SECONDARY_COLOR,
              new Item.Settings()));

  public static final Item DACHSHUND_SPAWN_EGG =
      Registry.register(
          Registries.ITEM,
          Identifier.of(DogsUnleashed.MOD_ID, "dachshund_spawn_egg"),
          new SpawnEggItem(
              ModEntities.DACHSHUND,
              ModConstants.DACHSHUND_SPAWN_EGG_PRIMARY_COLOR,
              ModConstants.DACHSHUND_SPAWN_EGG_SECONDARY_COLOR,
              new Item.Settings()));

  public static final Item BEAGLE_SPAWN_EGG =
      Registry.register(
          Registries.ITEM,
          Identifier.of(DogsUnleashed.MOD_ID, "beagle_spawn_egg"),
          new SpawnEggItem(
              ModEntities.BEAGLE,
              ModConstants.BEAGLE_SPAWN_EGG_PRIMARY_COLOR,
              ModConstants.BEAGLE_SPAWN_EGG_SECONDARY_COLOR,
              new Item.Settings()));

  public static void initialize() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
        .register(entries -> entries.add(DOG_BED));
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
        .register(entries -> entries.add(HUSKY_SPAWN_EGG));
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
        .register(entries -> entries.add(DACHSHUND_SPAWN_EGG));
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
        .register(entries -> entries.add(BEAGLE_SPAWN_EGG));
  }
}
