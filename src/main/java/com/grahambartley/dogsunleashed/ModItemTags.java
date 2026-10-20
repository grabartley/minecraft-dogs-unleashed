package com.grahambartley.dogsunleashed;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModItemTags {

  public static final TagKey<Item> PENDANTS =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(DogsUnleashed.MOD_ID, "pendants"));

  public static final TagKey<Item> DOG_COSMETICS =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(DogsUnleashed.MOD_ID, "dog_cosmetics"));

  public static final TagKey<Item> DOG_COSTUMES =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(DogsUnleashed.MOD_ID, "dog_costumes"));

  public static final TagKey<Item> SCENT_ITEMS =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(DogsUnleashed.MOD_ID, "scent_items"));

  public static final TagKey<Item> PACK_WEAPONS =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(DogsUnleashed.MOD_ID, "pack_weapons"));
}
