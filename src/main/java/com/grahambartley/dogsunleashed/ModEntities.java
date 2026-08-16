package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.entity.FrisbeeProjectileEntity;
import com.grahambartley.dogsunleashed.entity.StickProjectileEntity;
import com.grahambartley.dogsunleashed.entity.TennisBallProjectileEntity;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
  private static final float TENNIS_BALL_PROJECTILE_SIZE = 0.25f;

  public static final EntityType<UnleashedDogEntity> HUSKY = registerDog(UnleashedDogBreed.HUSKY);

  public static final EntityType<UnleashedDogEntity> DACHSHUND =
      registerDog(UnleashedDogBreed.DACHSHUND);

  public static final EntityType<UnleashedDogEntity> BEAGLE = registerDog(UnleashedDogBreed.BEAGLE);

  public static final EntityType<UnleashedDogEntity> GOLDEN_RETRIEVER =
      registerDog(UnleashedDogBreed.GOLDEN_RETRIEVER);

  public static final EntityType<UnleashedDogEntity> SHIBA_INU =
      registerDog(UnleashedDogBreed.SHIBA_INU);

  public static final EntityType<UnleashedDogEntity> CROSS_BREED =
      registerDog(UnleashedDogBreed.CROSS_BREED);

  public static final EntityType<TennisBallProjectileEntity> TENNIS_BALL_PROJECTILE =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "tennis_ball_projectile"),
          EntityType.Builder.<TennisBallProjectileEntity>create(
                  TennisBallProjectileEntity::new, SpawnGroup.MISC)
              .dimensions(TENNIS_BALL_PROJECTILE_SIZE, TENNIS_BALL_PROJECTILE_SIZE)
              .build(Identifier.of(DogsUnleashed.MOD_ID, "tennis_ball_projectile").toString()));

  public static final EntityType<StickProjectileEntity> STICK_PROJECTILE =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "stick_projectile"),
          EntityType.Builder.<StickProjectileEntity>create(
                  StickProjectileEntity::new, SpawnGroup.MISC)
              .dimensions(TENNIS_BALL_PROJECTILE_SIZE, TENNIS_BALL_PROJECTILE_SIZE)
              .build(Identifier.of(DogsUnleashed.MOD_ID, "stick_projectile").toString()));

  public static final EntityType<FrisbeeProjectileEntity> FRISBEE_PROJECTILE =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(DogsUnleashed.MOD_ID, "frisbee_projectile"),
          EntityType.Builder.<FrisbeeProjectileEntity>create(
                  FrisbeeProjectileEntity::new, SpawnGroup.MISC)
              .dimensions(TENNIS_BALL_PROJECTILE_SIZE, TENNIS_BALL_PROJECTILE_SIZE)
              .build(Identifier.of(DogsUnleashed.MOD_ID, "frisbee_projectile").toString()));

  private static EntityType<UnleashedDogEntity> registerDog(final UnleashedDogBreed breed) {
    final Identifier id = Identifier.of(DogsUnleashed.MOD_ID, breed.serializedId());
    return Registry.register(
        Registries.ENTITY_TYPE,
        id,
        EntityType.Builder.<UnleashedDogEntity>create(
                (type, world) -> new UnleashedDogEntity(type, world, breed), SpawnGroup.CREATURE)
            .dimensions(breed.dimensions().width(), breed.dimensions().height())
            .build(id.toString()));
  }

  public static EntityType<UnleashedDogEntity> getDogEntityType(final UnleashedDogBreed breed) {
    return switch (breed) {
      case HUSKY -> HUSKY;
      case DACHSHUND -> DACHSHUND;
      case BEAGLE -> BEAGLE;
      case GOLDEN_RETRIEVER -> GOLDEN_RETRIEVER;
      case SHIBA_INU -> SHIBA_INU;
      case CROSS_BREED -> CROSS_BREED;
    };
  }

  public static void initialize() {
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      FabricDefaultAttributeRegistry.register(getDogEntityType(breed), breed.createAttributes());
    }
  }
}
