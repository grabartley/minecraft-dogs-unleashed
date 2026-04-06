package com.grahambartley;

import com.grahambartley.entity.UnleashedDogBreed;
import com.grahambartley.item.DogBedItem;
import com.grahambartley.item.DogGraveItem;
import com.grahambartley.item.TennisBallItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class ModItems {

  public static final Item TENNIS_BALL =
      Registry.register(
          Registries.ITEM,
          Identifier.of(DogsUnleashed.MOD_ID, "tennis_ball"),
          new TennisBallItem(new Item.Settings().maxCount(16)));

  public static final Item DOG_BED =
      Registry.register(
          Registries.ITEM,
          Identifier.of(DogsUnleashed.MOD_ID, "dog_bed"),
          new DogBedItem(ModBlocks.DOG_BED, new Item.Settings()));

  public static final Item DOG_GRAVE =
      Registry.register(
          Registries.ITEM,
          Identifier.of(DogsUnleashed.MOD_ID, "dog_grave"),
          new DogGraveItem(ModBlocks.DOG_GRAVE, new Item.Settings()));

  public static final Item HUSKY_SPAWN_EGG =
      Registry.register(
          Registries.ITEM,
          Identifier.of(DogsUnleashed.MOD_ID, "husky_spawn_egg"),
          new SpawnEggItem(
              ModEntities.HUSKY,
              UnleashedDogBreed.HUSKY.spawnEggColors().primary(),
              UnleashedDogBreed.HUSKY.spawnEggColors().secondary(),
              new Item.Settings()));

  public static final Item DACHSHUND_SPAWN_EGG =
      Registry.register(
          Registries.ITEM,
          Identifier.of(DogsUnleashed.MOD_ID, "dachshund_spawn_egg"),
          new SpawnEggItem(
              ModEntities.DACHSHUND,
              UnleashedDogBreed.DACHSHUND.spawnEggColors().primary(),
              UnleashedDogBreed.DACHSHUND.spawnEggColors().secondary(),
              new Item.Settings()));

  public static final Item BEAGLE_SPAWN_EGG =
      Registry.register(
          Registries.ITEM,
          Identifier.of(DogsUnleashed.MOD_ID, "beagle_spawn_egg"),
          new SpawnEggItem(
              ModEntities.BEAGLE,
              UnleashedDogBreed.BEAGLE.spawnEggColors().primary(),
              UnleashedDogBreed.BEAGLE.spawnEggColors().secondary(),
              new Item.Settings()));

  public static final Item GOLDEN_RETRIEVER_SPAWN_EGG =
      Registry.register(
          Registries.ITEM,
          Identifier.of(DogsUnleashed.MOD_ID, "goldenretriever_spawn_egg"),
          new SpawnEggItem(
              ModEntities.GOLDEN_RETRIEVER,
              UnleashedDogBreed.GOLDEN_RETRIEVER.spawnEggColors().primary(),
              UnleashedDogBreed.GOLDEN_RETRIEVER.spawnEggColors().secondary(),
              new Item.Settings()));

  public static final Item SHIBA_INU_SPAWN_EGG =
      Registry.register(
          Registries.ITEM,
          Identifier.of(DogsUnleashed.MOD_ID, "shibainu_spawn_egg"),
          new SpawnEggItem(
              ModEntities.SHIBA_INU,
              UnleashedDogBreed.SHIBA_INU.spawnEggColors().primary(),
              UnleashedDogBreed.SHIBA_INU.spawnEggColors().secondary(),
              new Item.Settings()));

  public static void initialize() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
        .register(entries -> entries.add(TENNIS_BALL));
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
        .register(
            entries -> {
              for (DyeColor color : DyeColor.values()) {
                ItemStack stack = new ItemStack(DOG_BED);
                stack.set(ModComponents.DOG_BED_COLOR, color);
                entries.add(stack);
              }
            });
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
        .register(entries -> entries.add(HUSKY_SPAWN_EGG));
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
        .register(entries -> entries.add(DACHSHUND_SPAWN_EGG));
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
        .register(entries -> entries.add(BEAGLE_SPAWN_EGG));
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
        .register(entries -> entries.add(GOLDEN_RETRIEVER_SPAWN_EGG));
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
        .register(entries -> entries.add(SHIBA_INU_SPAWN_EGG));
  }
}
