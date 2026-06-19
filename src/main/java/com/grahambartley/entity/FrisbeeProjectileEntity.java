package com.grahambartley.entity;

import com.grahambartley.ModComponents;
import com.grahambartley.ModEntities;
import com.grahambartley.block.entity.FrisbeeBlockEntity;
import com.grahambartley.entity.fetch.AbstractFetchProjectileEntity;
import com.grahambartley.entity.fetch.FetchItemType;
import com.grahambartley.entity.fetch.FetchTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FrisbeeProjectileEntity extends AbstractFetchProjectileEntity {

  private static final TrackedData<Integer> COLOR_ID =
      DataTracker.registerData(FrisbeeProjectileEntity.class, TrackedDataHandlerRegistry.INTEGER);

  public FrisbeeProjectileEntity(
      EntityType<? extends FrisbeeProjectileEntity> entityType, World world) {
    super(entityType, world);
  }

  public FrisbeeProjectileEntity(World world, LivingEntity thrower) {
    super(ModEntities.FRISBEE_PROJECTILE, thrower, world);
  }

  @Override
  protected void initDataTracker(DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(COLOR_ID, DyeColor.WHITE.getId());
  }

  public DyeColor getFrisbeeColor() {
    return DyeColor.byId(this.dataTracker.get(COLOR_ID));
  }

  public void setFrisbeeColor(DyeColor color) {
    this.dataTracker.set(COLOR_ID, color.getId());
  }

  @Override
  protected double getGravity() {
    return 0.01;
  }

  @Override
  protected void enrichLandedBlockEntity(BlockPos landPos) {
    if (this.getWorld().getBlockEntity(landPos) instanceof FrisbeeBlockEntity frisbeeBlock) {
      frisbeeBlock.setColor(this.getFrisbeeColor());
    }
  }

  @Override
  protected ItemStack buildDropStack() {
    ItemStack dropped = new ItemStack(this.getFetchItemType().item());
    dropped.set(ModComponents.FRISBEE_COLOR, this.getFrisbeeColor());
    return dropped;
  }

  @Override
  public FetchItemType getFetchItemType() {
    return FetchTypes.FRISBEE;
  }
}
