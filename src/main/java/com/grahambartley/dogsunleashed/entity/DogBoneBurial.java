package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.burial.BoneCache;
import com.grahambartley.dogsunleashed.entity.burial.BoneCaches;
import com.grahambartley.dogsunleashed.entity.burial.BoneUnearthing;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class DogBoneBurial extends DogComponent {

  private static final int DIG_PARTICLES = 6;
  private static final double DIG_PARTICLE_SPREAD = 0.3;
  private static final float DIG_SOUND_VOLUME = 0.6F;

  private final UnleashedDogEntity dog;
  private boolean buryPending;

  DogBoneBurial(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public boolean canTakeBone() {
    final DogCommand command = this.dog.getCommand();
    return this.dog.isTamed()
        && (command == DogCommand.FREE_ROAM || command == DogCommand.STAY)
        && !this.dog.getFetchCarry().isCarrying()
        && !this.dog.getPlaySession().isInPlayMode()
        && !this.dog.getComponents().get(DogScent.class).isTracking()
        && !this.dog.getSleepController().isAsleep();
  }

  void takeBone(final PlayerEntity player, final ItemStack stack) {
    stack.decrementUnlessCreative(1, player);
    this.carryBone();
    this.buryPending = true;
  }

  public boolean isBuryPending() {
    return this.buryPending && this.isCarryingBone();
  }

  public void abandonBurial() {
    this.buryPending = false;
    this.setDigging(false);
  }

  public boolean bury(final ServerWorld world, final BlockPos ground) {
    final UUID owner = this.dog.getOwnerUuid();
    if (!this.isBuryPending() || owner == null) {
      return false;
    }
    if (!BoneCaches.get(world)
        .add(
            new BoneCache(ground.toImmutable(), owner, this.dog.getUuid(), world.getTimeOfDay()))) {
      return false;
    }
    this.dog.getFetchCarry().setCarrying(false);
    this.abandonBurial();
    return true;
  }

  public boolean digUp(final ServerWorld world, final BlockPos ground) {
    if (this.dog.getFetchCarry().isCarrying() || BoneCaches.get(world).remove(ground).isEmpty()) {
      return false;
    }
    this.carryBone();
    this.setDigging(false);
    BoneUnearthing.popBonus(world, ground);
    return true;
  }

  public boolean isCarryingBone() {
    return this.dog.getFetchCarry().isCarryingFetchedItem()
        && this.dog.getFetchCarry().carriedStack().isOf(Items.BONE);
  }

  public boolean isDigging() {
    return this.dog.getDataTracker().get(UnleashedDogEntity.BONE_DIGGING);
  }

  public void setDigging(final boolean digging) {
    this.dog.getDataTracker().set(UnleashedDogEntity.BONE_DIGGING, digging);
  }

  public void digEffects(final ServerWorld world, final BlockPos ground) {
    final BlockState state = world.getBlockState(ground);
    final Vec3d top = Vec3d.ofCenter(ground).add(0.0, 0.5, 0.0);
    world.spawnParticles(
        new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
        top.x,
        top.y,
        top.z,
        DIG_PARTICLES,
        DIG_PARTICLE_SPREAD,
        0.1,
        DIG_PARTICLE_SPREAD,
        0.0);
    this.dog.playSound(
        state.getSoundGroup().getHitSound(), DIG_SOUND_VOLUME, state.getSoundGroup().getPitch());
  }

  public void tellOwner(final String translationKey) {
    if (this.dog.getOwner() instanceof PlayerEntity player) {
      DogInteractions.tell(this.dog, player, translationKey);
    }
  }

  public boolean isFreeToDig() {
    final DogCommand command = this.dog.getCommand();
    return this.dog.getOwner() != null
        && (command == DogCommand.FREE_ROAM || command == DogCommand.STAY)
        && this.dog.getTarget() == null
        && !this.dog.isInSittingPose()
        && !this.dog.isLeashed()
        && !this.dog.getSleepController().isAsleep();
  }

  private void carryBone() {
    final DogFetchCarry carry = this.dog.getFetchCarry();
    carry.setActiveType(null);
    carry.setCarrying(true);
    carry.setCarriedStack(BoneUnearthing.bone());
  }
}
