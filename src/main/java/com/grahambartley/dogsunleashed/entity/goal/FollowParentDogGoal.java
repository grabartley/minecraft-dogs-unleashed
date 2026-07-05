package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;

/**
 * Keeps a puppy trailing the parent it was bred from for as long as that parent is alive and
 * loaded. The parent is resolved by UUID each time the goal evaluates, so a dead, despawned, or
 * unloaded parent simply drops the puppy back to its normal wandering. The goal only runs while the
 * dog is a baby, so it ends on its own once the puppy grows up.
 */
public class FollowParentDogGoal extends Goal {

  private static final double STOP_DISTANCE = 3.0;
  private static final double START_DISTANCE = 16.0;
  private static final double GIVE_UP_DISTANCE = 32.0;
  private static final int RECALCULATE_INTERVAL_TICKS = 10;
  private static final float MAX_LOOK_YAW_CHANGE = 10.0f;

  private final UnleashedDogEntity puppy;
  private final double speed;
  private UnleashedDogEntity parent;
  private int recalculateCooldown;

  public FollowParentDogGoal(final UnleashedDogEntity puppy, final double speed) {
    this.puppy = puppy;
    this.speed = speed;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (!this.puppy.isBaby()) {
      return false;
    }
    final UnleashedDogEntity resolved = this.puppy.getParentDog();
    if (resolved == null) {
      return false;
    }
    final double distance = this.puppy.squaredDistanceTo(resolved);
    if (distance < STOP_DISTANCE * STOP_DISTANCE || distance > START_DISTANCE * START_DISTANCE) {
      return false;
    }
    this.parent = resolved;
    return true;
  }

  @Override
  public boolean shouldContinue() {
    if (!this.puppy.isBaby() || this.parent == null || !this.parent.isAlive()) {
      return false;
    }
    final double distance = this.puppy.squaredDistanceTo(this.parent);
    return distance > STOP_DISTANCE * STOP_DISTANCE
        && distance < GIVE_UP_DISTANCE * GIVE_UP_DISTANCE;
  }

  @Override
  public void start() {
    this.recalculateCooldown = 0;
  }

  @Override
  public void stop() {
    this.parent = null;
    this.puppy.getNavigation().stop();
  }

  @Override
  public void tick() {
    if (this.parent == null) {
      return;
    }
    this.puppy
        .getLookControl()
        .lookAt(this.parent, MAX_LOOK_YAW_CHANGE, this.puppy.getMaxLookPitchChange());
    if (--this.recalculateCooldown <= 0) {
      this.recalculateCooldown = RECALCULATE_INTERVAL_TICKS;
      this.puppy.getNavigation().startMovingTo(this.parent, this.speed);
    }
  }
}
