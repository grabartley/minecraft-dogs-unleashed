package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.burial.BoneCache;
import com.grahambartley.dogsunleashed.entity.burial.BoneCacheRules;
import com.grahambartley.dogsunleashed.entity.burial.BoneCaches;
import com.grahambartley.dogsunleashed.entity.burial.BoneUnearthing;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class DogBoneBurial extends DogComponent {

  private final UnleashedDogEntity dog;
  private boolean buryPending;
  private @Nullable DogCommand takenOn;

  DogBoneBurial(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public boolean canTakeBone() {
    return this.dog.isTamed()
        && this.dog.getOwner() != null
        && !this.dog.getFetchCarry().isCarrying()
        && !this.dog.getComponents().get(DogDeathWatch.class).isKeepingWatch()
        && !this.dog.getComponents().get(DogSledHarness.class).isHitched()
        && DogIdleness.isFreeIgnoringSitPose(this.dog);
  }

  void takeBone(final PlayerEntity player, final ItemStack stack) {
    stack.decrementUnlessCreative(1, player);
    this.carryBone();
    this.buryPending = true;
    this.takenOn = this.dog.getCommand();
    if (BoneCacheRules.standsToBury(this.takenOn)) {
      this.dog.setSitting(false);
      this.dog.setInSittingPose(false);
    }
  }

  public boolean canBuryNow() {
    return this.isBuryPending()
        && this.dog.getOwner() != null
        && this.takenOn != null
        && BoneCacheRules.keepsBurying(this.takenOn, this.dog.getCommand())
        && DogIdleness.isFreeForChores(this.dog);
  }

  public boolean isCalledOff() {
    return this.takenOn == null
        || !BoneCacheRules.keepsBurying(this.takenOn, this.dog.getCommand());
  }

  @Override
  void tick() {
    if (this.buryPending && !this.isCarryingBone()) {
      this.abandonBurial();
    }
  }

  public boolean isBuryPending() {
    return this.buryPending && this.isCarryingBone();
  }

  public void abandonBurial() {
    final boolean sitsBackDown =
        this.buryPending
            && this.takenOn != null
            && BoneCacheRules.sitsBackDownAfter(this.takenOn, this.dog.getCommand());
    this.buryPending = false;
    this.takenOn = null;
    this.setDigging(false);
    if (sitsBackDown) {
      this.dropBone();
      this.dog.setSitting(true);
    }
  }

  @Override
  void writeNbt(final NbtCompound nbt) {
    if (this.buryPending && this.takenOn != null) {
      nbt.putInt(ModNbtKeys.BONE_BURY_COMMAND, this.takenOn.id());
    }
  }

  @Override
  void readNbt(final NbtCompound nbt) {
    if (nbt.contains(ModNbtKeys.BONE_BURY_COMMAND, NbtElement.NUMBER_TYPE)) {
      this.takenOn = DogCommand.fromId(nbt.getInt(ModNbtKeys.BONE_BURY_COMMAND));
      this.buryPending = true;
    }
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
        && BoneCacheRules.digsUpOn(this.dog.getCommand())
        && DogIdleness.isFreeForChores(this.dog);
  }

  private void dropBone() {
    if (!this.isCarryingBone()) {
      return;
    }
    this.dog.dropStack(this.dog.getFetchCarry().carriedStack().copy());
    this.dog.getFetchCarry().setCarrying(false);
  }

  private void carryBone() {
    final DogFetchCarry carry = this.dog.getFetchCarry();
    carry.setActiveType(null);
    carry.setCarrying(true);
    carry.setCarriedStack(BoneUnearthing.bone());
  }
}
