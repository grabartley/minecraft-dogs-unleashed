package com.grahambartley.dogsunleashed.block;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

/**
 * The collision shape of each of the dog house's cells.
 *
 * <p>A default full-cube shape would make the doorway decorative and leave an occupant standing in
 * a wall, so the four lower cells carry a hollow shell of floor, side walls, back wall and doorway
 * frame, sliced out of the geometry the player sees.
 *
 * <p>The upper cells collide with nothing. An occupant's hitbox is 1.1 blocks tall even while it
 * lies down at 0.4, so a solid roof would leave it suffocating in the ceiling and the damage would
 * wake it the moment it fell asleep. The roof is still drawn, and still aimable through the outline
 * shape; it just does not squeeze the dog it was built for.
 *
 * <p>The shell is authored once in the house's own 32-unit frame and then cut per cell, so the
 * doorway lands correctly even though it straddles both front cells.
 */
public final class DogHouseShape {

  private static final int CELL = 16;

  /** Solid parts of the shell, in the same 32-unit frame the model is authored in. */
  private static final double[][] SHELL = {
    {0, 0, 0, 32, 2, 32}, // floor
    {0, 2, 0, 2, 16, 32}, // left wall
    {30, 2, 0, 32, 16, 32}, // right wall
    {2, 2, 30, 30, 16, 32}, // back wall
    {2, 2, 0, 7, 12, 2}, // left door post
    {25, 2, 0, 30, 12, 2}, // right door post
    {2, 12, 0, 30, 16, 2}, // lintel
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

  /** Cuts the shell down to one cell and rebases it into that cell's own 0..1 coordinates. */
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

  /** Rotates a cell's shape about its own vertical centre, a quarter turn at a time. */
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
