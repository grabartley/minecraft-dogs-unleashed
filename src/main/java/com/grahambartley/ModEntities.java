package com.grahambartley;

import com.grahambartley.entity.DachshundEntity;
import com.grahambartley.entity.HuskyEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {

  public static final EntityType<HuskyEntity> HUSKY =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "husky"),
          EntityType.Builder.create(HuskyEntity::new, SpawnGroup.CREATURE)
              .dimensions(ModConstants.HUSKY_WIDTH, ModConstants.HUSKY_HEIGHT)
              .build(Identifier.of(DogsUnleashed.MOD_ID, "husky").toString()));

  public static final EntityType<DachshundEntity> DACHSHUND =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "dachshund"),
          EntityType.Builder.create(DachshundEntity::new, SpawnGroup.CREATURE)
              .dimensions(ModConstants.DACHSHUND_WIDTH, ModConstants.DACHSHUND_HEIGHT)
              .build(Identifier.of(DogsUnleashed.MOD_ID, "dachshund").toString()));

  public static void initialize() {
    FabricDefaultAttributeRegistry.register(HUSKY, HuskyEntity.createAttributes());
    FabricDefaultAttributeRegistry.register(DACHSHUND, DachshundEntity.createAttributes());
  }
}
