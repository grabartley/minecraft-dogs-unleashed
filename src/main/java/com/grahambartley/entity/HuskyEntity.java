package com.grahambartley.entity;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.passive.WolfVariant;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class HuskyEntity extends WolfEntity implements GeoEntity {

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  public HuskyEntity(EntityType<? extends WolfEntity> entityType, World world) {
    super(entityType, world);
  }

  @Override
  public EntityData initialize(
      ServerWorldAccess world,
      LocalDifficulty difficulty,
      SpawnReason spawnReason,
      EntityData entityData) {
    RegistryEntry<WolfVariant> variant =
        world
            .getRegistryManager()
            .get(RegistryKeys.WOLF_VARIANT)
            .getDefaultEntry()
            .orElseThrow(() -> new IllegalStateException("Missing default wolf variant"));
    this.setVariant(variant);
    return super.initialize(world, difficulty, spawnReason, entityData);
  }

  @Override
  public void readCustomDataFromNbt(NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    if (!nbt.contains("variant")) {
      RegistryEntry<WolfVariant> defaultVariant =
          this.getWorld()
              .getRegistryManager()
              .get(RegistryKeys.WOLF_VARIANT)
              .getDefaultEntry()
              .orElse(null);
      if (defaultVariant != null) {
        this.setVariant(defaultVariant);
      }
    }
  }

  public static DefaultAttributeContainer.Builder createHuskyAttributes() {
    return WolfEntity.createWolfAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 25.0)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0);
  }

  @Override
  public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            "controller",
            0,
            state -> {
              if (state.getAnimatable().isInSittingPose()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("sit"));
              }
              if (state.getAnimatable().getVelocity().horizontalLengthSquared() > 0.01) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("walk"));
              }
              return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
            }));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
