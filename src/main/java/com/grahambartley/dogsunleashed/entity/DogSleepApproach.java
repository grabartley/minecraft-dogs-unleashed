package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.block.DogSleepSpotAnchor;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
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

  public enum Step {
    SLEEP,
    WALK,
    GIVE_UP
  }

  public static Step next(
      final boolean arrived, final boolean onGround, final boolean pathReaches) {
    if (arrived) {
      return Step.SLEEP;
    }
    return onGround && !pathReaches ? Step.GIVE_UP : Step.WALK;
  }

  public static void step(final UnleashedDogEntity dog, final BlockPos bedPos) {
    final boolean arrived =
        hasArrived(dog.getPos(), DogSleepSpotAnchor.approachPoint(dog.getWorld(), bedPos));
    if (arrived) {
      dog.getSleepController().startSleepingInBed(bedPos);
      return;
    }
    walkTo(dog.getWorld(), dog.getNavigation(), bedPos);
    final Path path = dog.getNavigation().getCurrentPath();
    if (next(false, dog.isOnGround(), path != null && path.reachesTarget()) == Step.GIVE_UP) {
      dog.getSleepController().giveUpOnBed();
    }
  }

  public static void walkTo(
      final BlockView world, final EntityNavigation navigation, final BlockPos bedPos) {
    final Vec3d point = DogSleepSpotAnchor.approachPoint(world, bedPos);
    navigation.startMovingTo(point.x, point.y, point.z, SPEED);
  }
}
