package com.grahambartley.dogsunleashed.entity;

import java.util.Optional;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class DogStandingSpot {

  private DogStandingSpot() {}

  public static Optional<Vec3d> firstSafe(
      final World world, final Entity dog, final Iterable<BlockPos> candidates) {
    for (final BlockPos candidate : candidates) {
      if (world.getFluidState(candidate).isStill()) {
        continue;
      }
      final Vec3d standingPos = Dismounting.findRespawnPos(dog.getType(), world, candidate, false);
      if (standingPos != null) {
        return Optional.of(standingPos);
      }
    }
    return Optional.empty();
  }
}
