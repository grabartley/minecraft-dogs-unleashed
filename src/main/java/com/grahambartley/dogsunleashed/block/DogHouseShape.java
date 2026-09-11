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
  private static final int HOUSE_SIZE = 2 * CELL;
  private static final int WALL = 2;
  private static final int FLOOR_TOP = 2;
  private static final int WALL_TOP = 18;
  private static final int DOORWAY_TOP = 15;
  private static final int DOORWAY_MIN_X = 6;
  private static final int DOORWAY_MAX_X = 26;

  private record ShellBox(
      double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {}

  private static final ShellBox FLOOR = new ShellBox(0, 0, 0, HOUSE_SIZE, FLOOR_TOP, HOUSE_SIZE);
  private static final ShellBox LEFT_WALL =
      new ShellBox(0, FLOOR_TOP, 0, WALL, WALL_TOP, HOUSE_SIZE);
  private static final ShellBox RIGHT_WALL =
      new ShellBox(HOUSE_SIZE - WALL, FLOOR_TOP, 0, HOUSE_SIZE, WALL_TOP, HOUSE_SIZE);
  private static final ShellBox BACK_WALL =
      new ShellBox(WALL, FLOOR_TOP, HOUSE_SIZE - WALL, HOUSE_SIZE - WALL, WALL_TOP, HOUSE_SIZE);
  private static final ShellBox LEFT_DOOR_POST =
      new ShellBox(WALL, FLOOR_TOP, 0, DOORWAY_MIN_X, DOORWAY_TOP, WALL);
  private static final ShellBox RIGHT_DOOR_POST =
      new ShellBox(DOORWAY_MAX_X, FLOOR_TOP, 0, HOUSE_SIZE - WALL, DOORWAY_TOP, WALL);
  private static final ShellBox DOOR_LINTEL =
      new ShellBox(WALL, DOORWAY_TOP, 0, HOUSE_SIZE - WALL, WALL_TOP, WALL);

  private static final List<ShellBox> SHELL =
      List.of(
          FLOOR, LEFT_WALL, RIGHT_WALL, BACK_WALL, LEFT_DOOR_POST, RIGHT_DOOR_POST, DOOR_LINTEL);

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
    final int cellMinX = part.right() * CELL;
    final int cellMinZ = part.back() * CELL;
    final List<VoxelShape> boxes = new ArrayList<>();
    for (final ShellBox box : SHELL) {
      final double minX = Math.max(box.minX(), cellMinX);
      final double maxX = Math.min(box.maxX(), cellMinX + (double) CELL);
      final double minZ = Math.max(box.minZ(), cellMinZ);
      final double maxZ = Math.min(box.maxZ(), cellMinZ + (double) CELL);
      if (minX >= maxX || minZ >= maxZ) {
        continue;
      }
      boxes.add(
          VoxelShapes.cuboid(
              (minX - cellMinX) / CELL,
              box.minY() / CELL,
              (minZ - cellMinZ) / CELL,
              (maxX - cellMinX) / CELL,
              Math.min(box.maxY(), CELL) / CELL,
              (maxZ - cellMinZ) / CELL));
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
