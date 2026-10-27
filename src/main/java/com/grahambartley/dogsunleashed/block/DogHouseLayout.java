package com.grahambartley.dogsunleashed.block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class DogHouseLayout {

  private static final double CUSHION_TOWARD_DOOR = 2.0 / 16.0;
  private static final double MIDDLE_TO_DOORSTEP = 1.5;

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

  public static Vec3d cushionCentre(final BlockPos origin, final Direction facing) {
    return footprintMiddle(origin, facing).add(towardDoor(facing, CUSHION_TOWARD_DOOR));
  }

  public static Vec3d doorstep(final BlockPos origin, final Direction facing) {
    return footprintMiddle(origin, facing).add(towardDoor(facing, MIDDLE_TO_DOORSTEP));
  }

  public static List<BlockPos> wakeCells(final BlockPos origin, final Direction facing) {
    final Set<BlockPos> footprint = Set.copyOf(cellsOf(origin, facing));
    final Set<BlockPos> ring = new LinkedHashSet<>();
    for (final BlockPos cell : footprint) {
      if (cell.getY() != origin.getY()) {
        continue;
      }
      for (final BlockPos neighbour : BlockPos.iterate(cell.add(-1, 0, -1), cell.add(1, 0, 1))) {
        if (!footprint.contains(neighbour)) {
          ring.add(neighbour.toImmutable());
        }
      }
    }
    final Vec3d doorstep = doorstep(origin, facing);
    return ring.stream()
        .sorted(
            Comparator.comparingDouble(
                cell -> Vec3d.ofBottomCenter(cell).squaredDistanceTo(doorstep)))
        .toList();
  }

  private static Vec3d footprintMiddle(final BlockPos origin, final Direction facing) {
    final Direction right = facing.rotateYClockwise();
    final Direction back = facing.getOpposite();
    return Vec3d.ofBottomCenter(origin)
        .add(
            (right.getOffsetX() + back.getOffsetX()) * 0.5,
            0.0,
            (right.getOffsetZ() + back.getOffsetZ()) * 0.5);
  }

  private static Vec3d towardDoor(final Direction facing, final double distance) {
    return new Vec3d(facing.getOffsetX() * distance, 0.0, facing.getOffsetZ() * distance);
  }
}
