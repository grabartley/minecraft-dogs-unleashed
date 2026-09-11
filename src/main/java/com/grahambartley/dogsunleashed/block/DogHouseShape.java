package com.grahambartley.dogsunleashed.block;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public final class DogHouseShape {

  private static final int CELL = 16;

  private static final double[][] SHELL = {
    {0, 0, 0, 32, 2, 32},
    {0, 2, 0, 2, 18, 32},
    {30, 2, 0, 32, 18, 32},
    {2, 2, 30, 30, 18, 32},
    {2, 2, 0, 6, 15, 2},
    {26, 2, 0, 30, 15, 2},
    {2, 15, 0, 30, 18, 2},
  };

  private static final Map<DogHousePart, Map<Direction, VoxelShape>> SHAPES = buildShapes();

  private DogHouseShape() {}

  public static VoxelShape collision(final DogHousePart part, final Direction facing) {
    if (!part.isLower()) {
      return VoxelShapes.empty();
    }
    return SHAPES.get(part).get(facing);
  }

  private static Map<DogHousePart, Map<Direction, VoxelShape>> buildShapes() {
    final Map<DogHousePart, Map<Direction, VoxelShape>> shapes = new EnumMap<>(DogHousePart.class);
    for (final DogHousePart part : DogHousePart.values()) {
      if (!part.isLower()) {
        continue;
      }
      final VoxelShape north = sliceCell(part);
      final Map<Direction, VoxelShape> byFacing = new EnumMap<>(Direction.class);
      byFacing.put(Direction.NORTH, north);
      byFacing.put(Direction.EAST, rotateClockwise(north, 1));
      byFacing.put(Direction.SOUTH, rotateClockwise(north, 2));
      byFacing.put(Direction.WEST, rotateClockwise(north, 3));
      shapes.put(part, byFacing);
    }
    return shapes;
  }

  private static VoxelShape sliceCell(final DogHousePart part) {
    final int minU = part.right() * CELL;
    final int minW = part.back() * CELL;
    final List<VoxelShape> boxes = new ArrayList<>();
    for (final double[] box : SHELL) {
      final double u0 = Math.max(box[0], minU);
      final double u1 = Math.min(box[3], minU + (double) CELL);
      final double w0 = Math.max(box[2], minW);
      final double w1 = Math.min(box[5], minW + (double) CELL);
      if (u0 >= u1 || w0 >= w1) {
        continue;
      }
      boxes.add(
          VoxelShapes.cuboid(
              (u0 - minU) / CELL,
              box[1] / CELL,
              (w0 - minW) / CELL,
              (u1 - minU) / CELL,
              Math.min(box[4], CELL) / CELL,
              (w1 - minW) / CELL));
    }
    return boxes.stream().reduce(VoxelShapes.empty(), VoxelShapes::union).simplify();
  }

  private static VoxelShape rotateClockwise(final VoxelShape shape, final int quarterTurns) {
    VoxelShape rotated = shape;
    for (int turn = 0; turn < quarterTurns; turn++) {
      final VoxelShape[] accumulator = {VoxelShapes.empty()};
      rotated.forEachBox(
          (minX, minY, minZ, maxX, maxY, maxZ) ->
              accumulator[0] =
                  VoxelShapes.union(
                      accumulator[0],
                      VoxelShapes.cuboid(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
      rotated = accumulator[0].simplify();
    }
    return rotated;
  }
}
