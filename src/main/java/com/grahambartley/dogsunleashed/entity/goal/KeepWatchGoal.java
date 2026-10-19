package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DeathWatchRules;
import com.grahambartley.dogsunleashed.entity.DogDeathWatch;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class KeepWatchGoal extends Goal {

  private static final double MOVE_SPEED = 1.0;
  private static final int REPATH_INTERVAL_TICKS = 10;

  private final UnleashedDogEntity dog;
  private int repathCooldown;

  public KeepWatchGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.JUMP));
  }

  @Override
  public boolean canStart() {
    return this.dog.isTamed()
        && this.dog.getComponents().get(DogDeathWatch.class).isKeepingWatch()
        && this.dog.getCommandController().getAnchorPos() != null
        && !this.dog.getSleepController().isAsleep();
  }

  @Override
  public boolean shouldContinue() {
    return this.canStart();
  }

  @Override
  public void start() {
    this.repathCooldown = 0;
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void tick() {
    final BlockPos spot = this.dog.getCommandController().getAnchorPos();
    if (spot == null) {
      return;
    }
    final Vec3d target = Vec3d.ofBottomCenter(spot);
    if (DeathWatchRules.hasArrived(this.dog.getPos().subtract(target).horizontalLengthSquared())) {
      this.dog.getNavigation().stop();
      this.dog.setInSittingPose(true);
      return;
    }
    this.liftPose();
    if (this.repathCooldown-- > 0) {
      return;
    }
    this.repathCooldown = REPATH_INTERVAL_TICKS;
    this.dog.getNavigation().startMovingTo(target.x, target.y, target.z, MOVE_SPEED);
  }

  @Override
  public void stop() {
    this.liftPose();
    this.dog.getNavigation().stop();
  }

  private void liftPose() {
    if (this.dog.isInSittingPose() && !this.dog.isSitting()) {
      this.dog.setInSittingPose(false);
    }
  }
}
