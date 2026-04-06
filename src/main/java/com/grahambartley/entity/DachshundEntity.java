package com.grahambartley.entity;

import static com.grahambartley.ModEntities.DACHSHUND;

import com.grahambartley.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

public class DachshundEntity extends UnleashedDogEntity {
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
