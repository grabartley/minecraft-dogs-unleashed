package com.grahambartley.dogsunleashed.entity.fetch;

import com.grahambartley.dogsunleashed.entity.DogAnimationKeys;
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

public abstract class AbstractFetchProjectileEntity extends ThrownEntity
    implements GeoEntity, FetchProjectileEntity {
  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  protected AbstractFetchProjectileEntity(
      EntityType<? extends AbstractFetchProjectileEntity> entityType, World world) {
    super(entityType, world);
  }

  protected AbstractFetchProjectileEntity(
      EntityType<? extends ThrownEntity> entityType, LivingEntity thrower, World world) {
    super(entityType, thrower, world);
  }

  @Override
  protected void initDataTracker(DataTracker.Builder builder) {}

  @Override
  protected final void onEntityHit(EntityHitResult entityHitResult) {}

  @Override
  protected final void onBlockHit(BlockHitResult blockHitResult) {
    super.onBlockHit(blockHitResult);
    if (this.getWorld().isClient) {
      return;
    }

    BlockPos landPos = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
    BlockState stateAtPos = this.getWorld().getBlockState(landPos);
    FetchItemType fetchItemType = this.getFetchItemType();

    if (stateAtPos.isAir() || stateAtPos.isReplaceable()) {
      this.getWorld().setBlockState(landPos, fetchItemType.landedBlock().getDefaultState());
      this.enrichLandedBlockEntity(landPos);
      FetchProjectileEntity.notifyPlayingDogsOfLandedFetchItem(this, landPos);
    } else {
      ItemEntity itemEntity =
          new ItemEntity(
              this.getWorld(), this.getX(), this.getY(), this.getZ(), this.buildDropStack());
      this.getWorld().spawnEntity(itemEntity);
      FetchProjectileEntity.notifyPlayingDogsToEndPlayMode(this);
    }

    this.discard();
  }

  protected void enrichLandedBlockEntity(BlockPos landPos) {}

  protected ItemStack buildDropStack() {
    return new ItemStack(this.getFetchItemType().item());
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
