package com.grahambartley.dogsunleashed.listener;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.block.entity.DogGraveBlockEntity;
import com.grahambartley.dogsunleashed.entity.DogResurrection;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public final class ResurrectionRitualListener {

  private static final int[] ROD_SEARCH_Y_OFFSETS = {0, -1, 1};

  private ResurrectionRitualListener() {}

  public static void initialize() {
    ServerEntityEvents.ENTITY_LOAD.register(ResurrectionRitualListener::onEntityLoad);
  }

  static void onEntityLoad(final Entity entity, final ServerWorld world) {
    if (!(entity instanceof LightningEntity bolt)) {
      return;
    }
    final BlockPos rodPos = findRitualRod(world, bolt.getBlockPos());
    if (rodPos == null) {
      return;
    }
    final BlockPos gravePos = rodPos.down();
    if (!(world.getBlockEntity(gravePos) instanceof DogGraveBlockEntity grave)
        || !DogResurrection.canResurrect(world, grave)) {
      return;
    }

    bolt.setCosmetic(true);
    DogsUnleashed.runNextTick(() -> resurrectAt(world, gravePos));
  }

  private static void resurrectAt(final ServerWorld world, final BlockPos gravePos) {
    if (world.getBlockEntity(gravePos) instanceof DogGraveBlockEntity grave) {
      DogResurrection.resurrect(world, gravePos, grave);
    }
  }

  private static @Nullable BlockPos findRitualRod(
      final ServerWorld world, final BlockPos strikePos) {
    for (final int yOffset : ROD_SEARCH_Y_OFFSETS) {
      final BlockPos candidate = strikePos.up(yOffset);
      if (world.getBlockState(candidate).isOf(Blocks.LIGHTNING_ROD)) {
        return candidate;
      }
    }
    return null;
  }
}
