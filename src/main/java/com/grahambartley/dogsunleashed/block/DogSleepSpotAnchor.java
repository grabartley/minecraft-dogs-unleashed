package com.grahambartley.dogsunleashed.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

public final class DogSleepSpotAnchor {

  private static final double CELL_CENTRE = 0.5;

  private static final double BED_LIFT = 0.1;

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
