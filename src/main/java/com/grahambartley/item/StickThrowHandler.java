package com.grahambartley.item;

import com.grahambartley.entity.StickProjectileEntity;
import com.grahambartley.entity.UnleashedDogEntity;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public final class StickThrowHandler {
  private StickThrowHandler() {}

  public static void register() {
    UseItemCallback.EVENT.register(StickThrowHandler::use);
  }

  private static TypedActionResult<ItemStack> use(PlayerEntity player, World world, Hand hand) {
    ItemStack itemStack = player.getStackInHand(hand);
    if (!itemStack.isOf(Items.STICK) || player.isSneaking()) {
      return TypedActionResult.pass(itemStack);
    }

    if (!UnleashedDogEntity.isAnyDogInPlayModeFor(player.getUuid())) {
      return TypedActionResult.pass(itemStack);
    }

    if (!world.isClient) {
      StickProjectileEntity stick = new StickProjectileEntity(world, player);
      stick.setVelocity(player, player.getPitch(), player.getYaw(), 0.0f, 1.5f, 1.0f);
      world.spawnEntity(stick);
      itemStack.decrementUnlessCreative(1, player);
    }

    return TypedActionResult.success(itemStack, world.isClient);
  }
}
