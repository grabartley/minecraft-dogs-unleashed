package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogAging;
import com.grahambartley.dogsunleashed.entity.DogSleepController;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.world.World;

public class GroundNapGoal extends Goal {

  private final UnleashedDogEntity dog;
  private long lastNappedPeriod = GroundNapRules.NEVER;
  private long startedAt;
  private long napTicks = GroundNapRules.NAP_TICKS;
  private long hostileCheckedAt = GroundNapRules.NEVER;
  private boolean hostileNearby;

  public GroundNapGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
  }

  @Override
  public boolean canStart() {
    final DogSleepController sleep = this.dog.getSleepController();
    return GroundNapRules.napsOnGround(this.dog.getBreed().nature().sleepsInBeds(), this.isSenior())
        && this.dog.isTamed()
        && !this.dog.isInSittingPose()
        && GroundNapRules.allowsCommand(this.dog.getCommand())
        && !sleep.isAsleep()
        && !sleep.isCommandedToSleep()
        && GroundNapRules.isDue(
            this.dog.getWorld().getTimeOfDay(),
            this.lastNappedPeriod,
            this.dog.getUuid().getLeastSignificantBits(),
            this.currentNapTicks())
        && this.situation().allowsNapToStart();
  }

  @Override
  public boolean shouldContinue() {
    return this.dog.getSleepController().isNapping()
        && !this.dog.isInSittingPose()
        && !GroundNapRules.isOver(this.dog.getWorld().getTime(), this.startedAt, this.napTicks)
        && this.situation().isCalm();
  }

  @Override
  public void start() {
    final World world = this.dog.getWorld();
    this.lastNappedPeriod = GroundNapRules.periodOf(world.getTimeOfDay());
    this.startedAt = world.getTime();
    this.napTicks = this.currentNapTicks();
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

  private boolean isSenior() {
    return this.dog.getComponents().get(DogAging.class).isSenior();
  }

  private long currentNapTicks() {
    return GroundNapRules.napTicks(this.isSenior());
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
        this.dog.getPlaySession().isInPlayMode(),
        this.isOwnerAway());
  }

  private boolean isOwnerAway() {
    final LivingEntity owner = this.dog.getOwner();
    return this.dog.getCommand().followsOwnerOnRelocation()
        && owner != null
        && owner.squaredDistanceTo(this.dog)
            > GroundNapRules.OWNER_NEARBY_RANGE * GroundNapRules.OWNER_NEARBY_RANGE;
  }

  private boolean isHostileNearby() {
    final long now = this.dog.getWorld().getTime();
    if (this.hostileCheckedAt == GroundNapRules.NEVER
        || now - this.hostileCheckedAt >= GroundNapRules.HOSTILE_CHECK_TICKS) {
      this.hostileCheckedAt = now;
      this.hostileNearby = this.scanForHostiles();
    }
    return this.hostileNearby;
  }

  private boolean scanForHostiles() {
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
