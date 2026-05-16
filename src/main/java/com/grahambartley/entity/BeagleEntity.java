package com.grahambartley.entity;

import static com.grahambartley.ModEntities.BEAGLE;

import com.grahambartley.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

public class BeagleEntity extends UnleashedDogEntity {
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

  @Override
  public UnleashedDogBreed getBreed() {
    return UnleashedDogBreed.BEAGLE;
  }

  @Override
  protected SoundEvent getBarkSound() {
    return ModSounds.BEAGLE_BARK;
  }
}
