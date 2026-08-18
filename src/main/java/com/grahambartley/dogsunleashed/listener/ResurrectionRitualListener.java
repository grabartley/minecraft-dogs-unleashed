package com.grahambartley.dogsunleashed.listener;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.block.DogGraveBlock;
import com.grahambartley.dogsunleashed.block.entity.DogGraveBlockEntity;
import com.grahambartley.dogsunleashed.entity.DogResurrection;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Fires the resurrection ritual: lightning striking a lightning rod that stands on a grave holding
 * a Totem of Undying. Hooking entity load rather than the weather tick catches every lightning
 * bolt, so a Channeling trident works through the same path as a thunderstorm.
 */
public final class ResurrectionRitualListener {

  /**
   * The bolt entity lands on the rod's column but not always on its exact block, so the rod is
   * looked for one block either side of the strike as well.
   */
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
    final BlockState below = world.getBlockState(rodPos.down());
    if (!(below.getBlock() instanceof DogGraveBlock)) {
      return;
    }
    final BlockPos gravePos = DogGraveBlock.basePosOf(below, rodPos.down());
    if (!(world.getBlockEntity(gravePos) instanceof DogGraveBlockEntity grave)
        || !DogResurrection.canResurrect(world, grave)) {
      return;
    }

    // The ritual takes the strike: a live bolt would otherwise set the grave site alight and raise
    // the pet directly into the fire. Cosmetic keeps the flash and the thunder and drops the harm.
    bolt.setCosmetic(true);
    // Deferred so the dog is not spawned from inside the bolt's own load event.
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
