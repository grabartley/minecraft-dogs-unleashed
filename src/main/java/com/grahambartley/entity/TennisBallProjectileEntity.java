package com.grahambartley.entity;

import com.grahambartley.ModEntities;
import com.grahambartley.entity.fetch.AbstractFetchProjectileEntity;
import com.grahambartley.entity.fetch.FetchItemType;
import com.grahambartley.entity.fetch.FetchTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class TennisBallProjectileEntity extends AbstractFetchProjectileEntity {

  public TennisBallProjectileEntity(
      EntityType<? extends TennisBallProjectileEntity> entityType, World world) {
    super(entityType, world);
  }

  public TennisBallProjectileEntity(World world, LivingEntity thrower) {
    super(ModEntities.TENNIS_BALL_PROJECTILE, thrower, world);
  }

  @Override
  public FetchItemType getFetchItemType() {
    return FetchTypes.TENNIS_BALL;
  }
}
