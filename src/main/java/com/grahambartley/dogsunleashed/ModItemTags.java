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
}
