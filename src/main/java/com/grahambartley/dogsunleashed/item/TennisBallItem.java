package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.entity.TennisBallProjectileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class TennisBallItem extends Item {

  public TennisBallItem(Settings settings) {
    super(settings);
  }

  @Override
  public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
    ItemStack itemStack = user.getStackInHand(hand);

    if (!world.isClient) {
      TennisBallProjectileEntity ball = new TennisBallProjectileEntity(world, user);
      ball.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 1.5f, 1.0f);
      world.spawnEntity(ball);
      itemStack.decrementUnlessCreative(1, user);
    }

    return TypedActionResult.success(itemStack, world.isClient);
  }
}
