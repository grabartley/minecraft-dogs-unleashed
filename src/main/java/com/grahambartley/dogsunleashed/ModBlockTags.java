package com.grahambartley.dogsunleashed;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModBlockTags {

  public static final TagKey<Block> DOGS_SPAWNABLE_ON =
      TagKey.of(RegistryKeys.BLOCK, Identifier.of(DogsUnleashed.MOD_ID, "dogs_spawnable_on"));

  /** Every block a dog will accept as a bed, so a new sleep spot needs no goal changes. */
  public static final TagKey<Block> DOG_BEDS =
      TagKey.of(RegistryKeys.BLOCK, Identifier.of(DogsUnleashed.MOD_ID, "dog_beds"));
}
