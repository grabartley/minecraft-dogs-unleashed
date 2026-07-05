package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.ModComponents;
import com.grahambartley.dogsunleashed.entity.FrisbeeProjectileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class FrisbeeItem extends Item {

  public FrisbeeItem(Settings settings) {
    super(settings);
  }

  @Override
  public Text getName(ItemStack stack) {
    DyeColor color = stack.getOrDefault(ModComponents.FRISBEE_COLOR, DyeColor.WHITE);
    String colorName =
        color.getName().substring(0, 1).toUpperCase()
            + color.getName().substring(1).replace('_', ' ');
    return Text.translatable("item.dogs-unleashed.frisbee.colored", colorName);
  }

  @Override
  public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
    ItemStack itemStack = user.getStackInHand(hand);

    if (!world.isClient) {
      FrisbeeProjectileEntity frisbee = new FrisbeeProjectileEntity(world, user);
      DyeColor color = itemStack.getOrDefault(ModComponents.FRISBEE_COLOR, DyeColor.WHITE);
      frisbee.setFrisbeeColor(color);
      frisbee.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 1.5f, 1.0f);
      world.spawnEntity(frisbee);
      itemStack.decrementUnlessCreative(1, user);
    }

    return TypedActionResult.success(itemStack, world.isClient);
  }
}
