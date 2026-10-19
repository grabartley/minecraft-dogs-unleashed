package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogSleepController;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.world.World;

public class GroundNapGoal extends Goal {

  private final UnleashedDogEntity dog;
  private long lastNappedPeriod = GroundNapRules.NEVER;
  private long startedAt;

  public GroundNapGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
  }

  @Override
  public boolean canStart() {
    final DogSleepController sleep = this.dog.getSleepController();
    return !this.dog.getBreed().nature().sleepsInBeds()
        && this.dog.isTamed()
        && !this.dog.isInSittingPose()
        && GroundNapRules.allowsCommand(this.dog.getCommand())
        && !sleep.isAsleep()
        && !sleep.isCommandedToSleep()
        && GroundNapRules.isDue(
            this.dog.getWorld().getTimeOfDay(),
            this.lastNappedPeriod,
            this.dog.getUuid().getLeastSignificantBits())
        && this.situation().allowsNapToStart();
  }

  @Override
  public boolean shouldContinue() {
    return this.dog.getSleepController().isNapping()
        && !this.dog.isInSittingPose()
        && !GroundNapRules.isOver(this.dog.getWorld().getTime(), this.startedAt)
        && this.situation().isCalm();
  }

  @Override
  public void start() {
    final World world = this.dog.getWorld();
    this.lastNappedPeriod = GroundNapRules.periodOf(world.getTimeOfDay());
    this.startedAt = world.getTime();
    this.dog.getSleepController().startNapping();
  }

  @Override
  public void stop() {
    if (this.dog.getSleepController().isNapping()) {
      this.dog.wakeUp();
    }
  }

  @Override
  public void tick() {
    this.dog.getNavigation().stop();
  }

  private GroundNapRules.Situation situation() {
    return new GroundNapRules.Situation(
        this.dog.getNavigation().isFollowingPath() || this.dog.getFetchCarry().isCarrying(),
        this.dog.isOnGround() && !this.dog.isTouchingWater() && !this.dog.isInLava(),
        this.dog.getTarget() != null,
        this.isHostileNearby(),
        GroundNapRules.isHurtRecently(
            this.dog.getWorld().getTime(), this.dog.getSleepController().getLastHurtAt()),
        this.dog.hasPassengers(),
        this.dog.isLeashed(),
        this.dog.getPlaySession().isInPlayMode());
  }

  private boolean isHostileNearby() {
    return !this.dog
        .getWorld()
        .getEntitiesByClass(
            MobEntity.class,
            this.dog.getBoundingBox().expand(GroundNapRules.HOSTILE_RANGE),
            mob ->
                mob instanceof Monster
                    && mob.isAlive()
                    && mob.squaredDistanceTo(this.dog)
                        <= GroundNapRules.HOSTILE_RANGE * GroundNapRules.HOSTILE_RANGE)
        .isEmpty();
  }
}
