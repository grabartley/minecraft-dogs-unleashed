package com.grahambartley;

import com.grahambartley.entity.HuskyEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
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
          FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, HuskyEntity::new)
              .dimensions(
                  EntityDimensions.fixed(ModConstants.HUSKY_WIDTH, ModConstants.HUSKY_HEIGHT))
              .build());

  public static void initialize() {
    FabricDefaultAttributeRegistry.register(HUSKY, HuskyEntity.createHuskyAttributes());
  }
}
