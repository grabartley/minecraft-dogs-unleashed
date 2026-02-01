package com.grahambartley;

import com.grahambartley.entity.BeagleEntity;
import com.grahambartley.entity.DachshundEntity;
import com.grahambartley.entity.GoldenRetrieverEntity;
import com.grahambartley.entity.HuskyEntity;
import com.grahambartley.entity.ShibaInuEntity;
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

  public static final EntityType<BeagleEntity> BEAGLE =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "beagle"),
          EntityType.Builder.create(BeagleEntity::new, SpawnGroup.CREATURE)
              .dimensions(ModConstants.BEAGLE_WIDTH, ModConstants.BEAGLE_HEIGHT)
              .build(Identifier.of(DogsUnleashed.MOD_ID, "beagle").toString()));

  public static final EntityType<GoldenRetrieverEntity> GOLDEN_RETRIEVER =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "goldenretriever"),
          EntityType.Builder.create(GoldenRetrieverEntity::new, SpawnGroup.CREATURE)
              .dimensions(ModConstants.GOLDEN_RETRIEVER_WIDTH, ModConstants.GOLDEN_RETRIEVER_HEIGHT)
              .build(Identifier.of(DogsUnleashed.MOD_ID, "goldenretriever").toString()));

  public static final EntityType<ShibaInuEntity> SHIBA_INU =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "shibainu"),
          EntityType.Builder.create(ShibaInuEntity::new, SpawnGroup.CREATURE)
              .dimensions(ModConstants.SHIBA_INU_WIDTH, ModConstants.SHIBA_INU_HEIGHT)
              .build(Identifier.of(DogsUnleashed.MOD_ID, "shibainu").toString()));

  public static void initialize() {
    FabricDefaultAttributeRegistry.register(HUSKY, HuskyEntity.createAttributes());
    FabricDefaultAttributeRegistry.register(DACHSHUND, DachshundEntity.createAttributes());
    FabricDefaultAttributeRegistry.register(BEAGLE, BeagleEntity.createAttributes());
    FabricDefaultAttributeRegistry.register(
        GOLDEN_RETRIEVER, GoldenRetrieverEntity.createAttributes());
    FabricDefaultAttributeRegistry.register(SHIBA_INU, ShibaInuEntity.createAttributes());
  }
}
