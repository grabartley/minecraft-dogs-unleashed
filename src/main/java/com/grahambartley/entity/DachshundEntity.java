package com.grahambartley.entity;

import static com.grahambartley.ModEntities.DACHSHUND;

import com.grahambartley.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

public class DachshundEntity extends UnleashedDogEntity {

  public static DefaultAttributeContainer.Builder createAttributes() {
    return MobEntity.createMobAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
  }

  public DachshundEntity(EntityType<? extends TameableEntity> entityType, World world) {
    super(entityType, world);
  }

  @Override
  protected UnleashedDogEntity createBaby(ServerWorld world) {
    return new DachshundEntity(DACHSHUND, world);
  }

  @Override
  protected boolean isSameSpecies(MobEntity entity) {
    return entity instanceof DachshundEntity;
  }

  @Override
  public UnleashedDogBreed getBreed() {
    return UnleashedDogBreed.DACHSHUND;
  }

  @Override
  protected SoundEvent getBarkSound() {
    return ModSounds.DACHSHUND_BARK;
  }
}
