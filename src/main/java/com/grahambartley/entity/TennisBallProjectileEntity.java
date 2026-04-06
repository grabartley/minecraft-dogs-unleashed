package com.grahambartley.entity;

import com.grahambartley.ModBlocks;
import com.grahambartley.ModEntities;
import com.grahambartley.ModItems;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
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

public class TennisBallProjectileEntity extends ThrownEntity implements GeoEntity {

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  public TennisBallProjectileEntity(
      EntityType<? extends TennisBallProjectileEntity> entityType, World world) {
    super(entityType, world);
  }

  public TennisBallProjectileEntity(World world, LivingEntity thrower) {
    super(ModEntities.TENNIS_BALL_PROJECTILE, thrower, world);
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

    if (stateAtPos.isAir() || stateAtPos.isReplaceable()) {
      this.getWorld().setBlockState(landPos, ModBlocks.TENNIS_BALL.getDefaultState());
      notifyPlayingDog(landPos);
    } else {
      ItemEntity itemEntity =
          new ItemEntity(
              this.getWorld(),
              this.getX(),
              this.getY(),
              this.getZ(),
              new ItemStack(ModItems.TENNIS_BALL));
      this.getWorld().spawnEntity(itemEntity);
      notifyPlayEndForPlayingDog();
    }

    this.discard();
  }

  @Override
  protected void onEntityHit(EntityHitResult entityHitResult) {
    // No damage to entities
  }

  private void notifyPlayingDog(BlockPos ballPos) {
    Entity owner = this.getOwner();
    if (!(owner instanceof PlayerEntity player)) {
      return;
    }
    UUID playerUuid = player.getUuid();
    List<UnleashedDogEntity> playingDogs =
        this.getWorld()
            .getEntitiesByClass(
                UnleashedDogEntity.class,
                this.getBoundingBox().expand(128),
                dog -> dog.isInPlayMode() && playerUuid.equals(dog.getPlayPartnerPlayerUuid()));
    for (UnleashedDogEntity dog : playingDogs) {
      dog.setActiveBallBlockPos(ballPos);
    }
  }

  private void notifyPlayEndForPlayingDog() {
    Entity owner = this.getOwner();
    if (!(owner instanceof PlayerEntity player)) {
      return;
    }
    UUID playerUuid = player.getUuid();
    List<UnleashedDogEntity> playingDogs =
        this.getWorld()
            .getEntitiesByClass(
                UnleashedDogEntity.class,
                this.getBoundingBox().expand(128),
                dog -> dog.isInPlayMode() && playerUuid.equals(dog.getPlayPartnerPlayerUuid()));
    for (UnleashedDogEntity dog : playingDogs) {
      dog.endPlayMode();
    }
  }

  @Override
  public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this, "fly", 0, state -> state.setAndContinue(RawAnimation.begin().thenLoop("fly"))));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
