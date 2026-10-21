package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModArmorItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

public final class WolfDogArmour {

  private WolfDogArmour() {}

  public static boolean wears(final WolfEntity wolf) {
    return ModArmorItems.isDogArmour(wolf.getBodyArmor());
  }

  public static boolean absorbs(final WolfEntity wolf, final DamageSource source) {
    return wears(wolf) && new DogArmour(wolf).absorbs(source);
  }

  public static boolean tryAbsorb(
      final WolfEntity wolf, final DamageSource source, final float amount) {
    if (!absorbs(wolf, source)) {
      return false;
    }
    new DogArmour(wolf).takeHit(source, amount);
    return true;
  }

  public static @Nullable ActionResult interact(
      final WolfEntity wolf, final PlayerEntity player, final Hand hand) {
    if (wolf.getWorld().isClient() || !wolf.isTamed() || !wolf.isOwner(player)) {
      return null;
    }
    final ItemStack held = player.getStackInHand(hand);
    if (ModArmorItems.isDogArmour(held)
        && wolf.getBodyArmor().isEmpty()
        && DogEquipmentSlot.ARMOUR.fits(wolf.isBaby())) {
      wolf.equipBodyArmor(held.copyWithCount(1));
      held.decrementUnlessCreative(1, player);
      return ActionResult.SUCCESS;
    }
    if (!wears(wolf)) {
      return null;
    }
    if (held.getItem() instanceof ShearsItem) {
      return shear(wolf, player, hand, held);
    }
    return new DogArmour(wolf).tryRepair(player, held);
  }

  private static ActionResult shear(
      final WolfEntity wolf, final PlayerEntity player, final Hand hand, final ItemStack shears) {
    final ItemStack armour = wolf.getBodyArmor();
    if (!DogEquipmentSlot.ARMOUR.releases(
        DogArmour.isBindingCursed(armour), player.isCreative(), true)) {
      return ActionResult.PASS;
    }
    shears.damage(1, player, LivingEntity.getSlotForHand(hand));
    wolf.playSoundIfNotSilent(SoundEvents.ITEM_ARMOR_UNEQUIP_WOLF);
    wolf.equipBodyArmor(ItemStack.EMPTY);
    wolf.dropStack(armour);
    return ActionResult.SUCCESS;
  }
}
