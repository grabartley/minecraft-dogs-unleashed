package com.grahambartley.dogsunleashed.listener;

import com.grahambartley.dogsunleashed.entity.burial.BoneCaches;
import com.grahambartley.dogsunleashed.entity.burial.BoneUnearthing;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;

public final class BoneCacheBreakListener {

  private BoneCacheBreakListener() {}

  public static void initialize() {
    PlayerBlockBreakEvents.AFTER.register(
        (world, player, pos, state, blockEntity) -> {
          if (world instanceof ServerWorld serverWorld) {
            unearth(serverWorld, pos);
          }
        });
  }

  static void unearth(final ServerWorld world, final BlockPos pos) {
    if (BoneCaches.get(world).remove(pos).isEmpty()) {
      return;
    }
    ItemScatterer.spawn(
        world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, BoneUnearthing.bone());
    BoneUnearthing.popBonus(world, pos);
  }
}
