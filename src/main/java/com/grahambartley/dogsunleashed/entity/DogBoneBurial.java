package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.burial.BoneCache;
import com.grahambartley.dogsunleashed.entity.burial.BoneCacheRules;
import com.grahambartley.dogsunleashed.entity.burial.BoneCaches;
import com.grahambartley.dogsunleashed.entity.burial.BoneUnearthing;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class DogBoneBurial extends DogComponent {

  private final UnleashedDogEntity dog;
  private boolean buryPending;

  DogBoneBurial(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public boolean canTakeBone() {
    return this.dog.isTamed() && !this.dog.getFetchCarry().isCarrying() && this.isFreeToDig();
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

  public void tellOwner(final String translationKey) {
    if (this.dog.getOwner() instanceof PlayerEntity player) {
      DogInteractions.tell(this.dog, player, translationKey);
    }
  }

  public boolean isFreeToDig() {
    return this.dog.getOwner() != null
        && BoneCacheRules.buriesOn(this.dog.getCommand())
        && DogIdleness.isFreeForChores(this.dog);
  }

  private void carryBone() {
    final DogFetchCarry carry = this.dog.getFetchCarry();
    carry.setActiveType(null);
    carry.setCarrying(true);
    carry.setCarriedStack(BoneUnearthing.bone());
  }
}
