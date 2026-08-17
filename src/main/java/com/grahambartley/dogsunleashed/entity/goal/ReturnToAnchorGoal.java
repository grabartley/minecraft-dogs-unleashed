package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

/**
 * Leash for anchored commands (Stay, Guard): when the dog drifts beyond the anchor radius it walks
 * back until comfortably inside. Registered at the same priority as the follow goals; the command
 * gates keep them mutually exclusive.
 */
public class ReturnToAnchorGoal extends Goal {

  private static final double MOVE_SPEED = 1.0;
  private static final double START_DISTANCE = 8.0;
  private static final double STOP_DISTANCE = 4.0;
  private static final double POSITION_CENTER_OFFSET = 0.5;
  private static final int REPATH_INTERVAL_TICKS = 10;

  private final UnleashedDogEntity dog;
  private BlockPos anchorPos;
  private int repathCooldown;

  public ReturnToAnchorGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE));
  }

  @Override
  public boolean canStart() {
    if (!this.dog.isTamed()) {
      return false;
    }
    if (!this.dog.getCommand().isAnchored()) {
      return false;
    }
    if (this.dog.isInSittingPose() || this.dog.isSleepingInBed()) {
      return false;
    }
    final BlockPos anchor = this.dog.getCommandController().getAnchorPos();
    if (anchor == null) {
      return false;
    }
    if (this.dog.getBlockPos().getSquaredDistance(anchor) <= START_DISTANCE * START_DISTANCE) {
      return false;
    }
    this.anchorPos = anchor;
    return true;
  }

  @Override
  public boolean shouldContinue() {
    if (!this.dog.getCommand().isAnchored() || this.anchorPos == null) {
      return false;
    }
    return this.dog.getBlockPos().getSquaredDistance(this.anchorPos)
        > STOP_DISTANCE * STOP_DISTANCE;
  }

  @Override
  public void start() {
    this.repathCooldown = 0;
  }

  @Override
  public void tick() {
    if (this.repathCooldown-- > 0) {
      return;
    }
    this.repathCooldown = REPATH_INTERVAL_TICKS;
    this.dog
        .getNavigation()
        .startMovingTo(
            this.anchorPos.getX() + POSITION_CENTER_OFFSET,
            this.anchorPos.getY(),
            this.anchorPos.getZ() + POSITION_CENTER_OFFSET,
            MOVE_SPEED);
  }

  @Override
  public void stop() {
    this.anchorPos = null;
    this.dog.getNavigation().stop();
  }
}
