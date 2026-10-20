package com.grahambartley.dogsunleashed.entity.burial;

import com.grahambartley.dogsunleashed.ModAttachments;
import com.grahambartley.dogsunleashed.mixin.FoxEntityAccessor;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class BoneThieves {

  private static final int DROP_PICKUP_DELAY_TICKS = 40;

  private BoneThieves() {}

  public static boolean isWild(final FoxEntity fox) {
    return ((FoxEntityAccessor) fox)
        .dogsUnleashed$getTrustedUuids().stream().allMatch(Objects::isNull);
  }

  public static boolean mayRaid(final FoxEntity fox) {
    return FoxRaidRules.mayRaid(
        fox.getWorld().isNight(),
        isWild(fox),
        fox.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty(),
        fox.isBaby() || fox.isSleeping() || fox.isSitting() || fox.getTarget() != null);
  }

  public static Optional<BlockPos> pickCache(final ServerWorld world, final FoxEntity fox) {
    if (!mayRaid(fox)) {
      return Optional.empty();
    }
    return BoneCaches.get(world)
        .nearest(fox.getBlockPos(), FoxRaidRules.RAID_RANGE)
        .map(BoneCache::pos);
  }

  public static Optional<BlockPos> target(final FoxEntity fox) {
    return Optional.ofNullable(fox.getAttached(ModAttachments.FOX_RAID_TARGET));
  }

  public static void setTarget(final FoxEntity fox, final BlockPos ground) {
    fox.setAttached(ModAttachments.FOX_RAID_TARGET, ground.toImmutable());
  }

  public static void clearTarget(final FoxEntity fox) {
    fox.removeAttached(ModAttachments.FOX_RAID_TARGET);
  }

  public static boolean carriesStolenBone(final FoxEntity fox) {
    return fox.getAttachedOrElse(ModAttachments.FOX_STOLEN_BONE, false)
        && fox.getEquippedStack(EquipmentSlot.MAINHAND).isOf(Items.BONE);
  }

  public static boolean isThief(final FoxEntity fox) {
    return fox.isAlive() && (target(fox).isPresent() || carriesStolenBone(fox));
  }

  public static boolean steal(final ServerWorld world, final FoxEntity fox, final BlockPos ground) {
    if (!fox.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()
        || BoneCaches.get(world).remove(ground).isEmpty()) {
      return false;
    }
    fox.equipStack(EquipmentSlot.MAINHAND, BoneUnearthing.bone());
    fox.setPersistent();
    fox.setAttached(ModAttachments.FOX_STOLEN_BONE, true);
    clearTarget(fox);
    return true;
  }

  public static boolean dropStolenBone(final FoxEntity fox) {
    final boolean carrying = carriesStolenBone(fox);
    fox.removeAttached(ModAttachments.FOX_STOLEN_BONE);
    if (!carrying) {
      return false;
    }
    final ItemStack bone = fox.getEquippedStack(EquipmentSlot.MAINHAND);
    fox.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    final ItemEntity dropped =
        new ItemEntity(fox.getWorld(), fox.getX(), fox.getY(), fox.getZ(), bone);
    dropped.setPickupDelay(DROP_PICKUP_DELAY_TICKS);
    dropped.setThrower(fox);
    fox.getWorld().spawnEntity(dropped);
    return true;
  }
}
