package com.grahambartley.entity;

import com.grahambartley.ModEntities;
import com.grahambartley.entity.fetch.FetchItemType;
import com.grahambartley.entity.fetch.FetchProjectileEntity;
import com.grahambartley.entity.fetch.FetchTypes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class StickProjectileEntity extends ThrownEntity
    implements GeoEntity, FetchProjectileEntity {
  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  public StickProjectileEntity(
      EntityType<? extends StickProjectileEntity> entityType, World world) {
    super(entityType, world);
  }

  public StickProjectileEntity(World world, LivingEntity thrower) {
    super(ModEntities.STICK_PROJECTILE, thrower, world);
  }

  @Override
  protected void initDataTracker(DataTracker.Builder builder) {}

  @Override
  protected void onBlockHit(BlockHitResult blockHitResult) {
    super.onBlockHit(blockHitResult);
    if (this.getWorld().isClient) {
      return;
    }

    BlockPos landPos = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
    BlockState stateAtPos = this.getWorld().getBlockState(landPos);
    FetchItemType fetchItemType = this.getFetchItemType();

    if (stateAtPos.isAir() || stateAtPos.isReplaceable()) {
      this.getWorld().setBlockState(landPos, fetchItemType.landedBlock().getDefaultState());
      FetchProjectileEntity.notifyPlayingDogsOfLandedFetchItem(this, landPos);
    } else {
      ItemEntity itemEntity =
          new ItemEntity(
              this.getWorld(),
              this.getX(),
              this.getY(),
              this.getZ(),
              new ItemStack(fetchItemType.item()));
      this.getWorld().spawnEntity(itemEntity);
      FetchProjectileEntity.notifyPlayingDogsToEndPlayMode(this);
    }

    this.discard();
  }

  @Override
  protected void onEntityHit(EntityHitResult entityHitResult) {
    // Stick fetch is play-only, it should not damage entities it touches.
  }

  @Override
  public FetchItemType getFetchItemType() {
    return FetchTypes.STICK;
  }

  @Override
  public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            DogAnimationKeys.FLY,
            0,
            state -> state.setAndContinue(RawAnimation.begin().thenLoop(DogAnimationKeys.FLY))));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
