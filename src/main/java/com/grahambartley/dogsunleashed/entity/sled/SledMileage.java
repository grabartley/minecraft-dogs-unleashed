package com.grahambartley.dogsunleashed.entity.sled;

import com.grahambartley.dogsunleashed.ModAttachments;
import com.grahambartley.dogsunleashed.advancement.SledTravelledCriterion;
import com.grahambartley.dogsunleashed.entity.DireWolfStamina;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

public final class SledMileage {

  private SledMileage() {}

  public static final double MAX_BLOCKS_PER_TICK =
      SledSpeed.TOP_SPEED * DireWolfStamina.SPRINT_SPEED_MULTIPLIER;

  public static double capped(final double blocks) {
    return Math.min(blocks, MAX_BLOCKS_PER_TICK);
  }

  public static double add(final double travelled, final double blocks, final boolean snowy) {
    return snowy && blocks > 0.0 ? travelled + blocks : travelled;
  }

  static void record(final ServerPlayerEntity rider, final Entity sled, final double blocks) {
    final boolean snowy =
        sled.getWorld().getBiome(sled.getBlockPos()).value().isCold(sled.getBlockPos());
    final double before = rider.getAttachedOrElse(ModAttachments.SLED_SNOW_BLOCKS, 0.0);
    final double after = add(before, blocks, snowy);
    if (after != before) {
      rider.setAttached(ModAttachments.SLED_SNOW_BLOCKS, after);
      SledTravelledCriterion.INSTANCE.trigger(rider, after);
    }
  }
}
