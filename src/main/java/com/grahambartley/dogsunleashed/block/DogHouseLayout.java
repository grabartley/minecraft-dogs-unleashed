package com.grahambartley.dogsunleashed.block;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class DogHouseLayout {

  private DogHouseLayout() {}

  public static BlockPos offsetFromOrigin(final DogHousePart part, final Direction facing) {
    return BlockPos.ORIGIN
        .offset(facing.rotateYClockwise(), part.right())
        .offset(Direction.UP, part.up())
        .offset(facing.getOpposite(), part.back());
  }

  public static BlockPos originOf(
      final DogHousePart part, final Direction facing, final BlockPos pos) {
    return pos.subtract(offsetFromOrigin(part, facing));
  }

  public static List<BlockPos> cellsOf(final BlockPos origin, final Direction facing) {
    final List<BlockPos> cells = new ArrayList<>(DogHousePart.values().length);
    for (final DogHousePart part : DogHousePart.values()) {
      cells.add(origin.add(offsetFromOrigin(part, facing)));
    }
    return cells;
  }

  public static Vec3d occupantAnchor(final BlockPos origin, final Direction facing) {
    final Direction right = facing.rotateYClockwise();
    final Direction back = facing.getOpposite();
    return Vec3d.ofBottomCenter(origin)
        .add(
            (right.getOffsetX() + back.getOffsetX()) * 0.5,
            0.0,
            (right.getOffsetZ() + back.getOffsetZ()) * 0.5);
  }
}
