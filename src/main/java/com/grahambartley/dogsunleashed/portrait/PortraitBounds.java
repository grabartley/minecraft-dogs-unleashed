package com.grahambartley.dogsunleashed.portrait;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class PortraitBounds {

  public static final double THICKNESS = 1.0 / 16.0;

  private static final double WALL_INSET = 0.5 - THICKNESS / 2.0;

  private PortraitBounds() {}

  public static Box of(final BlockPos pos, final Direction facing, final PortraitSize size) {
    final Vec3d centre =
        Vec3d.ofCenter(pos)
            .offset(facing, -WALL_INSET)
            .offset(facing.rotateYCounterclockwise(), offset(size.width()))
            .offset(Direction.UP, offset(size.height()));
    final boolean facesAlongX = facing.getAxis() == Direction.Axis.X;
    return Box.of(
        centre,
        facesAlongX ? THICKNESS : size.width(),
        size.height(),
        facesAlongX ? size.width() : THICKNESS);
  }

  private static double offset(final int length) {
    return length % 2 == 0 ? 0.5 : 0.0;
  }
}
