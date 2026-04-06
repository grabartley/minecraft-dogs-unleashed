package com.grahambartley.entity;

import static com.grahambartley.ModEntities.GOLDEN_RETRIEVER;

import com.grahambartley.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

public class GoldenRetrieverEntity extends UnleashedDogEntity {

  public static DefaultAttributeContainer.Builder createAttributes() {
    return MobEntity.createMobAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.30)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0);
  }

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
  protected SoundEvent getBarkSound() {
    return ModSounds.GOLDEN_RETRIEVER_BARK;
  }
}
