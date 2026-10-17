package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModArmorItems;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;

public final class WolfDogArmour {

  private WolfDogArmour() {}

  public static boolean wears(final WolfEntity wolf) {
    return ModArmorItems.isDogArmour(wolf.getBodyArmor());
  }

  public static boolean tryAbsorb(
      final WolfEntity wolf, final DamageSource source, final float amount) {
    final DogArmour armour = new DogArmour(wolf);
    if (!wears(wolf) || !armour.absorbs(source)) {
      return false;
    }
    armour.takeHit(source, amount);
    return true;
  }

  public static @Nullable ActionResult interact(
      final WolfEntity wolf, final PlayerEntity player, final ItemStack held) {
    if (wolf.getWorld().isClient() || !wolf.isTamed() || !wolf.isOwner(player)) {
      return null;
    }
    if (ModArmorItems.isDogArmour(held) && wolf.getBodyArmor().isEmpty() && !wolf.isBaby()) {
      wolf.equipBodyArmor(held.copyWithCount(1));
      held.decrementUnlessCreative(1, player);
      return ActionResult.SUCCESS;
    }
    return wears(wolf) ? new DogArmour(wolf).tryRepair(player, held) : null;
  }
}
