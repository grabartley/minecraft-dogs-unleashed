package com.grahambartley.dogsunleashed.entity;

import static com.grahambartley.dogsunleashed.ModEntities.GOLDEN_RETRIEVER;

import com.grahambartley.dogsunleashed.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

public class GoldenRetrieverEntity extends UnleashedDogEntity {
  public GoldenRetrieverEntity(EntityType<? extends UnleashedDogEntity> entityType, World world) {
    super(entityType, world);
  }

  @Override
  protected UnleashedDogEntity createBaby(ServerWorld world) {
    return new GoldenRetrieverEntity(GOLDEN_RETRIEVER, world);
  }

  @Override
  protected boolean isSameSpecies(MobEntity entity) {
    return entity instanceof GoldenRetrieverEntity;
  }

  @Override
  public UnleashedDogBreed getBreed() {
    return UnleashedDogBreed.GOLDEN_RETRIEVER;
  }

  @Override
  protected String getSleepInBedMovementAnimationName() {
    return DogAnimationKeys.SLEEP;
  }

  @Override
  protected SoundEvent getBarkSound() {
    return ModSounds.GOLDEN_RETRIEVER_BARK;
  }
}
