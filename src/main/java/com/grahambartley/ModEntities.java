package com.grahambartley;

import com.grahambartley.entity.BeagleEntity;
import com.grahambartley.entity.DachshundEntity;
import com.grahambartley.entity.GoldenRetrieverEntity;
import com.grahambartley.entity.HuskyEntity;
import com.grahambartley.entity.ShibaInuEntity;
import com.grahambartley.entity.TennisBallProjectileEntity;
import com.grahambartley.entity.UnleashedDogBreed;
import com.grahambartley.entity.UnleashedDogEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
  private static final float TENNIS_BALL_PROJECTILE_SIZE = 0.25f;

  public static final EntityType<HuskyEntity> HUSKY =
      registerDog(
          UnleashedDogBreed.HUSKY,
          HuskyEntity::new,
          ModConstants.HUSKY_WIDTH,
          ModConstants.HUSKY_HEIGHT);

  public static final EntityType<DachshundEntity> DACHSHUND =
      registerDog(
          UnleashedDogBreed.DACHSHUND,
          DachshundEntity::new,
          ModConstants.DACHSHUND_WIDTH,
          ModConstants.DACHSHUND_HEIGHT);

  public static final EntityType<BeagleEntity> BEAGLE =
      registerDog(
          UnleashedDogBreed.BEAGLE,
          BeagleEntity::new,
          ModConstants.BEAGLE_WIDTH,
          ModConstants.BEAGLE_HEIGHT);

  public static final EntityType<GoldenRetrieverEntity> GOLDEN_RETRIEVER =
      registerDog(
          UnleashedDogBreed.GOLDEN_RETRIEVER,
          GoldenRetrieverEntity::new,
          ModConstants.GOLDEN_RETRIEVER_WIDTH,
          ModConstants.GOLDEN_RETRIEVER_HEIGHT);

  public static final EntityType<ShibaInuEntity> SHIBA_INU =
      registerDog(
          UnleashedDogBreed.SHIBA_INU,
          ShibaInuEntity::new,
          ModConstants.SHIBA_INU_WIDTH,
          ModConstants.SHIBA_INU_HEIGHT);

  public static final EntityType<TennisBallProjectileEntity> TENNIS_BALL_PROJECTILE =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "tennis_ball_projectile"),
          EntityType.Builder.<TennisBallProjectileEntity>create(
                  TennisBallProjectileEntity::new, SpawnGroup.MISC)
              .dimensions(TENNIS_BALL_PROJECTILE_SIZE, TENNIS_BALL_PROJECTILE_SIZE)
              .build(Identifier.of(DogsUnleashed.MOD_ID, "tennis_ball_projectile").toString()));

  private static <T extends UnleashedDogEntity> EntityType<T> registerDog(
      final UnleashedDogBreed breed,
      final EntityType.EntityFactory<T> factory,
      final float width,
      final float height) {
    final Identifier id = Identifier.of(DogsUnleashed.MOD_ID, breed.serializedId());
    return Registry.register(
        Registries.ENTITY_TYPE,
        id,
        EntityType.Builder.create(factory, SpawnGroup.CREATURE)
            .dimensions(width, height)
            .build(id.toString()));
  }

  public static EntityType<? extends UnleashedDogEntity> getDogEntityType(
      final UnleashedDogBreed breed) {
    return switch (breed) {
      case HUSKY -> HUSKY;
      case DACHSHUND -> DACHSHUND;
      case BEAGLE -> BEAGLE;
      case GOLDEN_RETRIEVER -> GOLDEN_RETRIEVER;
      case SHIBA_INU -> SHIBA_INU;
    };
  }

  public static void initialize() {
    FabricDefaultAttributeRegistry.register(HUSKY, HuskyEntity.createAttributes());
    FabricDefaultAttributeRegistry.register(DACHSHUND, DachshundEntity.createAttributes());
    FabricDefaultAttributeRegistry.register(BEAGLE, BeagleEntity.createAttributes());
    FabricDefaultAttributeRegistry.register(
        GOLDEN_RETRIEVER, GoldenRetrieverEntity.createAttributes());
    FabricDefaultAttributeRegistry.register(SHIBA_INU, ShibaInuEntity.createAttributes());
  }
}
