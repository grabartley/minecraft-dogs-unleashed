package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.block.DogSleepSpotAnchor;
import java.util.OptionalDouble;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class DogSleepApproach {

  public static final double ARRIVED_BLOCKS = 1.0;
  public static final double MIN_PROGRESS_BLOCKS = 0.5;
  public static final double SPEED = 1.0;

  private DogSleepApproach() {}

  public enum Step {
    SLEEP,
    WALK,
    GIVE_UP
  }

  public static Step next(
      final double dogToSpot, final OptionalDouble pathEndToSpot, final boolean onGround) {
    if (dogToSpot <= ARRIVED_BLOCKS) {
      return Step.SLEEP;
    }
    if (!onGround) {
      return Step.WALK;
    }
    final boolean getsCloser =
        pathEndToSpot.isPresent() && pathEndToSpot.getAsDouble() <= dogToSpot - MIN_PROGRESS_BLOCKS;
    return getsCloser ? Step.WALK : Step.GIVE_UP;
  }

  public static Vec3d walkTo(final UnleashedDogEntity dog, final BlockPos bedPos) {
    final BlockPos cell = DogSleepSpotAnchor.approachCell(dog.getWorld(), bedPos);
    dog.getNavigation().startMovingAlong(dog.getNavigation().findPathTo(cell, 0), SPEED);
    return Vec3d.ofBottomCenter(cell);
  }

  public static void step(final UnleashedDogEntity dog, final BlockPos bedPos) {
    final Vec3d spot = walkTo(dog, bedPos);
    final Step next =
        next(
            distance(dog.getPos(), spot),
            pathEndTo(dog.getNavigation().getCurrentPath(), spot),
            dog.isOnGround());
    switch (next) {
      case SLEEP -> dog.getSleepController().startSleepingInBed(bedPos);
      case GIVE_UP -> dog.getSleepController().giveUpOnBed();
      case WALK -> {}
    }
  }

  private static OptionalDouble pathEndTo(final Path path, final Vec3d spot) {
    return path == null || path.getEnd() == null
        ? OptionalDouble.empty()
        : OptionalDouble.of(distance(Vec3d.ofBottomCenter(path.getEnd().getBlockPos()), spot));
  }

  private static double distance(final Vec3d from, final Vec3d to) {
    return Math.abs(from.y - to.y) > ARRIVED_BLOCKS
        ? Double.MAX_VALUE
        : Math.hypot(from.x - to.x, from.z - to.z);
  }
}
