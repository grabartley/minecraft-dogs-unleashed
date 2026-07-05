package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.fetch.AbstractFetchProjectileEntity;
import com.grahambartley.dogsunleashed.entity.fetch.FetchItemType;
import com.grahambartley.dogsunleashed.entity.fetch.FetchTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class StickProjectileEntity extends AbstractFetchProjectileEntity {

  public StickProjectileEntity(
      EntityType<? extends StickProjectileEntity> entityType, World world) {
    super(entityType, world);
  }

  public StickProjectileEntity(World world, LivingEntity thrower) {
    super(ModEntities.STICK_PROJECTILE, thrower, world);
  }

  @Override
  public FetchItemType getFetchItemType() {
    return FetchTypes.STICK;
  }
}
