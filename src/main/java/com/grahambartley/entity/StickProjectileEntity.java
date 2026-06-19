package com.grahambartley.entity;

import com.grahambartley.ModEntities;
import com.grahambartley.entity.fetch.AbstractFetchProjectileEntity;
import com.grahambartley.entity.fetch.FetchItemType;
import com.grahambartley.entity.fetch.FetchTypes;
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
