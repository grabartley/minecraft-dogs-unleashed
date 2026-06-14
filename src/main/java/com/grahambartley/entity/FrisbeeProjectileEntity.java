package com.grahambartley.entity;

import com.grahambartley.ModComponents;
import com.grahambartley.ModEntities;
import com.grahambartley.block.entity.FrisbeeBlockEntity;
import com.grahambartley.entity.fetch.FetchItemType;
import com.grahambartley.entity.fetch.FetchProjectileEntity;
import com.grahambartley.entity.fetch.FetchTypes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
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

public class FrisbeeProjectileEntity extends ThrownEntity
    implements GeoEntity, FetchProjectileEntity {

  private static final TrackedData<Integer> COLOR_ID =
      DataTracker.registerData(FrisbeeProjectileEntity.class, TrackedDataHandlerRegistry.INTEGER);

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  public FrisbeeProjectileEntity(
      EntityType<? extends FrisbeeProjectileEntity> entityType, World world) {
    super(entityType, world);
  }

  public FrisbeeProjectileEntity(World world, LivingEntity thrower) {
    super(ModEntities.FRISBEE_PROJECTILE, thrower, world);
  }

  @Override
  protected void initDataTracker(DataTracker.Builder builder) {
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
      if (this.getWorld().getBlockEntity(landPos) instanceof FrisbeeBlockEntity frisbeeBlock) {
        frisbeeBlock.setColor(this.getFrisbeeColor());
      }
      FetchProjectileEntity.notifyPlayingDogsOfLandedFetchItem(this, landPos);
    } else {
      ItemStack dropped = new ItemStack(fetchItemType.item());
      dropped.set(ModComponents.FRISBEE_COLOR, this.getFrisbeeColor());
      ItemEntity itemEntity =
          new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), dropped);
      this.getWorld().spawnEntity(itemEntity);
      FetchProjectileEntity.notifyPlayingDogsToEndPlayMode(this);
    }

    this.discard();
  }

  @Override
  protected void onEntityHit(EntityHitResult entityHitResult) {
    // No damage to entities
  }

  @Override
  public FetchItemType getFetchItemType() {
    return FetchTypes.FRISBEE;
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
