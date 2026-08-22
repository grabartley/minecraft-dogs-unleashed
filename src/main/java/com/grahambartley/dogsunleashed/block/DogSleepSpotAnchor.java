package com.grahambartley.dogsunleashed.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

/**
 * Where a dog lies down in the bed it was assigned, and which way it points.
 *
 * <p>A one-cell bed puts the dog in the middle of its own cell facing wherever it happened to be
 * looking, which is all a flat bed needs. A dog house frames its occupant in a doorway, so it wants
 * the dog centred in a footprint that is two cells wide, lying on the bedding rather than sunk into
 * it, and turned to face out of the opening.
 */
public final class DogSleepSpotAnchor {

  private static final double CELL_CENTRE = 0.5;

  /** A dog rests a little proud of a flat bed so it does not sink into the block below it. */
  private static final double BED_LIFT = 0.1;

  /** Top of the house's bedding, in blocks above the floor of its lower cells. */
  private static final double HOUSE_BEDDING_TOP = 0.25;

  private DogSleepSpotAnchor() {}

  public static DogSleepPose poseFor(
      final BlockView world, final BlockPos bedPos, final float currentYaw) {
    final BlockState state = world.getBlockState(bedPos);
    if (state.getBlock() instanceof DogHouseBlock) {
      return housePose(bedPos, state);
    }
    return new DogSleepPose(
        new Vec3d(
            bedPos.getX() + CELL_CENTRE, bedPos.getY() + BED_LIFT, bedPos.getZ() + CELL_CENTRE),
        currentYaw);
  }

  private static DogSleepPose housePose(final BlockPos bedPos, final BlockState state) {
    final Direction facing = state.get(DogHouseBlock.FACING);
    final BlockPos origin = DogHouseBlock.originOf(state, bedPos);
    final Vec3d centre = DogHouseLayout.occupantAnchor(origin, facing);
    final Vec3d onBedding = new Vec3d(centre.x, centre.y + HOUSE_BEDDING_TOP, centre.z);
    return new DogSleepPose(onBedding, facing.asRotation());
  }
}
