package com.grahambartley.entity;

import static com.grahambartley.ModEntities.BEAGLE;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;

public class BeagleEntity extends UnleashedDogEntity implements GeoEntity, Angerable {

  public static DefaultAttributeContainer.Builder createAttributes() {
    return MobEntity.createMobAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 17.0)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.29)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0);
  }

  public BeagleEntity(EntityType<? extends TameableEntity> entityType, World world) {
    super(entityType, world);
  }

  @Override
  protected UnleashedDogEntity createBaby(ServerWorld world) {
    return new BeagleEntity(BEAGLE, world);
  }

  @Override
  protected boolean isSameSpecies(MobEntity entity) {
    return entity instanceof BeagleEntity;
  }
}
