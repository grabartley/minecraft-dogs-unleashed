package com.grahambartley.entity;

import static com.grahambartley.ModEntities.SHIBA_INU;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class ShibaInuEntity extends UnleashedDogEntity {

  public static DefaultAttributeContainer.Builder createAttributes() {
    return MobEntity.createMobAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 18.0)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.32)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.5);
  }

  public ShibaInuEntity(EntityType<? extends UnleashedDogEntity> entityType, World world) {
    super(entityType, world);
  }

  @Override
  protected UnleashedDogEntity createBaby(ServerWorld world) {
    return new ShibaInuEntity(SHIBA_INU, world);
  }

  @Override
  protected boolean isSameSpecies(MobEntity entity) {
    return entity instanceof ShibaInuEntity;
  }
}
