package com.grahambartley.dogsunleashed.render.pawprint;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public final class PawPrintGround {

  static final double SURFACE_TOLERANCE = 1.0e-3;

  private PawPrintGround() {}

  public static boolean holdsPrint(
      final BlockView world, final double x, final double y, final double z) {
    final BlockPos feet = BlockPos.ofFloored(x, y + SURFACE_TOLERANCE, z);
    if (!world.getFluidState(feet).isEmpty()) {
      return false;
    }
    for (final BlockPos ground : new BlockPos[] {feet, feet.down()}) {
      final VoxelShape shape = world.getBlockState(ground).getCollisionShape(world, ground);
      if (shape.isEmpty()) {
        continue;
      }
      final double top = shape.getMax(Direction.Axis.Y);
      if (restsOnSurface(y, ground.getY() + top, hasFlatFullTop(shape, top))) {
        return true;
      }
    }
    return false;
  }

  static boolean restsOnSurface(final double y, final double surfaceY, final boolean flatFullTop) {
    return flatFullTop && Math.abs(y - surfaceY) <= SURFACE_TOLERANCE;
  }

  private static boolean hasFlatFullTop(final VoxelShape shape, final double top) {
    final boolean[] found = {false};
    shape.forEachBox(
        (minX, minY, minZ, maxX, maxY, maxZ) ->
            found[0] |=
                maxY >= top - SURFACE_TOLERANCE
                    && minX <= SURFACE_TOLERANCE
                    && minZ <= SURFACE_TOLERANCE
                    && maxX >= 1.0 - SURFACE_TOLERANCE
                    && maxZ >= 1.0 - SURFACE_TOLERANCE);
    return found[0];
  }
}
