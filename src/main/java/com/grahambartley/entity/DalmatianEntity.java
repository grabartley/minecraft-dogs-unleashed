package com.grahambartley.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DalmatianEntity extends WolfEntity implements GeoEntity {

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  public DalmatianEntity(EntityType<? extends WolfEntity> entityType, World world) {
    super(entityType, world);
  }

  @Override
  public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    // Animation controllers will be registered here
    // For now, empty - will use animations from JSON files
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
