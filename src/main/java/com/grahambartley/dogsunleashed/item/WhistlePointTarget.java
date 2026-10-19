package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.item.WhistlePointRules.Order;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public record WhistlePointTarget(
    Order order, @Nullable Entity entity, BlockPos blockPos, Direction side) {

  public static WhistlePointTarget atEntity(final Order order, final Entity entity) {
    return new WhistlePointTarget(order, entity, entity.getBlockPos(), Direction.UP);
  }

  public static WhistlePointTarget atBlock(final BlockPos pos, final Direction side) {
    return new WhistlePointTarget(Order.GO_THERE, null, pos, side);
  }
}
