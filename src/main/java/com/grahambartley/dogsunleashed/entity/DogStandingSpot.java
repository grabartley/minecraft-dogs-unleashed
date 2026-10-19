package com.grahambartley.dogsunleashed.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class DogStandingSpot {

  private static final int MAX_DROP_TO_GROUND = 4;

  private DogStandingSpot() {}

  public static Optional<BlockPos> besideFace(
      final Entity dog, final BlockPos pos, final Direction side) {
    final BlockPos beside = pos.offset(side);
    final List<BlockPos> candidates = new ArrayList<>();
    for (int drop = 0; drop <= MAX_DROP_TO_GROUND; drop++) {
      candidates.add(beside.down(drop));
    }
    candidates.add(pos.up());
    return firstSafe(dog.getWorld(), dog, candidates).map(BlockPos::ofFloored);
  }

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
