package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.ModEntities;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class PuppyBasketItem extends Item {

  public PuppyBasketItem(final Settings settings) {
    super(settings);
  }

  @Override
  public ActionResult useOnBlock(final ItemUsageContext context) {
    if (!(context.getWorld() instanceof ServerWorld world)) {
      return ActionResult.SUCCESS;
    }
    final PlayerEntity player = context.getPlayer();
    if (player == null) {
      return ActionResult.PASS;
    }
    final BlockPos clicked = context.getBlockPos();
    final BlockPos pos =
        world.getBlockState(clicked).getCollisionShape(world, clicked).isEmpty()
            ? clicked
            : clicked.offset(context.getSide());
    final UnleashedDogEntity puppy =
        release(world, player, pos, !clicked.equals(pos) && context.getSide() == Direction.UP);
    if (puppy != null) {
      final ItemStack stack = context.getStack();
      stack.decrementUnlessCreative(1, player);
      world.emitGameEvent(player, GameEvent.ENTITY_PLACE, clicked);
    }
    return ActionResult.CONSUME;
  }

  public static @Nullable UnleashedDogEntity release(
      final ServerWorld world,
      final PlayerEntity player,
      final BlockPos pos,
      final boolean invertY) {
    final UnleashedDogEntity puppy =
        ModEntities.getDogEntityType(PuppyBasketRules.pick(world.random.nextInt()))
            .spawn(world, dog -> dog.setBaby(true), pos, SpawnReason.SPAWN_EGG, true, invertY);
    if (puppy != null) {
      puppy.getInteractions().tame(player);
    }
    return puppy;
  }
}
