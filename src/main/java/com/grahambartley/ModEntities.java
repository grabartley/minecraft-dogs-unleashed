package com.grahambartley;

import com.grahambartley.entity.DalmatianEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {

  public static final EntityType<DalmatianEntity> DALMATIAN =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "dalmatian"),
          FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, DalmatianEntity::new)
              .dimensions(EntityDimensions.fixed(0.6f, 0.85f))
              .build());

  public static void initialize() {
    // Register entity attributes
    FabricDefaultAttributeRegistry.register(DALMATIAN, WolfEntity.createWolfAttributes());
  }
}
