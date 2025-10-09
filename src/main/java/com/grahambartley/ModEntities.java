package com.grahambartley;

import com.grahambartley.entity.DalmatianEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEntities {

  public static final RegistryKey<EntityType<?>> DALMATIAN_KEY =
      RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(DogsUnleashed.MOD_ID, "dalmatian"));

  public static final EntityType<DalmatianEntity> DALMATIAN =
      Registry.register(
          Registries.ENTITY_TYPE,
          DALMATIAN_KEY,
          FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, DalmatianEntity::new)
              .dimensions(EntityDimensions.fixed(0.6f, 0.85f))
              .build(DALMATIAN_KEY));

  public static void initialize() {
    // Called to ensure static initialization
  }
}
