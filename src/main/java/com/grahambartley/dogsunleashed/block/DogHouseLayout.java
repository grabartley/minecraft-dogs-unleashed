package com.grahambartley.dogsunleashed.block;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Where the dog house's eight cells sit relative to each other.
 *
 * <p>Every part is defined in the house's own frame, so the same part means the same corner
 * whichever way the house faces. {@code FACING} is the direction the doorway points, which makes
 * the house's right hand the clockwise turn from it and its back the direction behind it.
 */
public final class DogHouseLayout {

  private DogHouseLayout() {}

  /** Where a part sits relative to the origin cell. */
  public static BlockPos offsetFromOrigin(final DogHousePart part, final Direction facing) {
    return BlockPos.ORIGIN
        .offset(facing.rotateYClockwise(), part.right())
        .offset(Direction.UP, part.up())
        .offset(facing.getOpposite(), part.back());
  }

  /** The origin cell of the house that {@code pos} belongs to, given what part it is. */
  public static BlockPos originOf(
      final DogHousePart part, final Direction facing, final BlockPos pos) {
    return pos.subtract(offsetFromOrigin(part, facing));
  }

  /** Every cell of the house whose origin is {@code origin}, the origin cell included. */
  public static List<BlockPos> cellsOf(final BlockPos origin, final Direction facing) {
    final List<BlockPos> cells = new ArrayList<>(DogHousePart.values().length);
    for (final DogHousePart part : DogHousePart.values()) {
      cells.add(origin.add(offsetFromOrigin(part, facing)));
    }
    return cells;
  }

  /**
   * Where an occupant lies. The footprint is two cells on a side, so its centre is the corner the
   * four lower cells share rather than the middle of any one of them.
   */
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
