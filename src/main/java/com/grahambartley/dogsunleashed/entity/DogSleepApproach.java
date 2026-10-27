package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.block.DogSleepSpotAnchor;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

public final class DogSleepApproach {

  public static final double CLOSE_ENOUGH_BLOCKS = 2.0;
  public static final double SPEED = 1.0;

  private DogSleepApproach() {}

  public static boolean hasArrived(final Vec3d dogPos, final Vec3d approachPoint) {
    final double dx = dogPos.x - approachPoint.x;
    final double dz = dogPos.z - approachPoint.z;
    return dx * dx + dz * dz <= CLOSE_ENOUGH_BLOCKS * CLOSE_ENOUGH_BLOCKS
        && Math.abs(dogPos.y - approachPoint.y) <= CLOSE_ENOUGH_BLOCKS;
  }

  public static boolean hasArrived(
      final BlockView world, final Vec3d dogPos, final BlockPos bedPos) {
    return hasArrived(dogPos, DogSleepSpotAnchor.approachPoint(world, bedPos))
        || hasArrived(dogPos, Vec3d.ofBottomCenter(bedPos));
  }

  public static void step(final UnleashedDogEntity dog, final BlockPos bedPos) {
    if (hasArrived(dog.getWorld(), dog.getPos(), bedPos)) {
      dog.getSleepController().startSleepingInBed(bedPos);
    } else {
      walkTo(dog.getWorld(), dog.getNavigation(), bedPos);
    }
  }

  public static void walkTo(
      final BlockView world, final EntityNavigation navigation, final BlockPos bedPos) {
    final Vec3d point = DogSleepSpotAnchor.approachPoint(world, bedPos);
    navigation.startMovingTo(point.x, point.y, point.z, SPEED);
  }
}
