package com.grahambartley.dogsunleashed;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class ModEntityTypeTags {

  public static final TagKey<EntityType<?>> WARDED_UNDEAD = of("warded_undead");
  public static final TagKey<EntityType<?>> WARDED_ARTHROPOD = of("warded_arthropod");
  public static final TagKey<EntityType<?>> WARDED_ILLAGER = of("warded_illager");
  public static final TagKey<EntityType<?>> WARDED_CREEPER = of("warded_creeper");

  private ModEntityTypeTags() {}

  private static TagKey<EntityType<?>> of(final String path) {
    return TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(DogsUnleashed.MOD_ID, path));
  }
}
