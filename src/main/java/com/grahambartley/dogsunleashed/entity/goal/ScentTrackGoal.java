package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogScent;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.scent.ScentLeadRules;
import com.grahambartley.dogsunleashed.entity.scent.ScentPhase;
import com.grahambartley.dogsunleashed.entity.scent.ScentProgress;
import com.grahambartley.dogsunleashed.entity.scent.ScentShellScan;
import com.grahambartley.dogsunleashed.entity.scent.ScentTargetFinder;
import com.grahambartley.dogsunleashed.entity.scent.ScentTargetKind;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ScentTrackGoal extends Goal {

  private static final double LEAD_SPEED = 1.0;
  private static final int REPATH_INTERVAL_TICKS = 10;
  private static final int ARRIVAL_TICKS = 60;
  private static final int GIVE_UP_TICKS = 40;
  private static final float LOOK_SPEED = 30.0F;

  private enum Stage {
    SEARCHING,
    LEADING,
    ARRIVED,
    GIVING_UP
  }

  private final UnleashedDogEntity dog;
  private final ScentProgress progress = new ScentProgress();
  private @Nullable ScentTargetFinder finder;
  private Stage stage = Stage.SEARCHING;
  private int generation = -1;
  private int repathCooldown;
  private int holdTicks;

  public ScentTrackGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    return this.scent().isTracking() && this.isFree();
  }

  @Override
  public boolean shouldContinue() {
    return this.canStart();
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void start() {
    this.begin();
  }

  @Override
  public void stop() {
    this.dog.getNavigation().stop();
    this.finder = null;
    this.generation = -1;
    if (this.stage == Stage.ARRIVED || this.stage == Stage.GIVING_UP) {
      this.scent().stop();
    } else if (this.scent().isTracking()) {
      this.scent().setPhase(ScentPhase.NONE);
    }
  }

  @Override
  public void tick() {
    if (this.scent().generation() != this.generation) {
      this.begin();
    }
    if (this.finder == null) {
      return;
    }
    switch (this.stage) {
      case SEARCHING -> this.search(this.finder);
      case LEADING -> this.lead(this.finder);
      case ARRIVED -> this.holdPoint(this.finder);
      case GIVING_UP -> this.holdGiveUp();
    }
  }

  private void begin() {
    this.generation = this.scent().generation();
    this.restartSearch();
  }

  private void restartSearch() {
    this.finder = this.scent().newFinder();
    if (this.finder == null) {
      this.scent().stop();
      return;
    }
    this.stage = Stage.SEARCHING;
    this.progress.reset();
    this.scent().setPhase(ScentPhase.SNIFFING);
    this.dog.getNavigation().stop();
  }

  private void search(final ScentTargetFinder activeFinder) {
    final ScentShellScan.Status status = activeFinder.advance(this.world());
    if (status == ScentShellScan.Status.FOUND) {
      this.stage = Stage.LEADING;
      this.repathCooldown = 0;
    } else if (status == ScentShellScan.Status.EXHAUSTED) {
      this.giveUp();
    }
  }

  private void lead(final ScentTargetFinder activeFinder) {
    final ScentTargetKind kind = activeFinder.kind();
    final Vec3d target = activeFinder.target();
    if (target == null || !activeFinder.isStillThere(this.world())) {
      this.restartSearch();
      return;
    }
    final double distance = this.dog.getPos().distanceTo(target);
    final double horizontal = Math.hypot(target.x - this.dog.getX(), target.z - this.dog.getZ());
    if (ScentLeadRules.hasArrived(kind, horizontal, distance)) {
      this.arrive(kind);
      return;
    }
    final LivingEntity owner = this.dog.getOwner();
    this.progress.record(distance);
    if (this.progress.hasStalled()) {
      this.giveUp();
      return;
    }
    if (owner != null && ScentLeadRules.shouldWaitForOwner(this.dog.distanceTo(owner))) {
      this.dog.getNavigation().stop();
      this.dog.getLookControl().lookAt(owner, LOOK_SPEED, LOOK_SPEED);
      return;
    }
    this.dog.getLookControl().lookAt(target.x, target.y, target.z, LOOK_SPEED, LOOK_SPEED);
    if (--this.repathCooldown <= 0) {
      this.repathCooldown = REPATH_INTERVAL_TICKS;
      this.dog.getNavigation().startMovingTo(target.x, target.y, target.z, LEAD_SPEED);
    }
  }

  private void arrive(final ScentTargetKind kind) {
    this.stage = Stage.ARRIVED;
    this.holdTicks = ARRIVAL_TICKS;
    this.dog.getNavigation().stop();
    this.scent().settle();
    this.scent().setPhase(ScentLeadRules.arrivalPhase(kind));
    this.scent().cues().found();
  }

  private void giveUp() {
    this.stage = Stage.GIVING_UP;
    this.holdTicks = GIVE_UP_TICKS;
    this.dog.getNavigation().stop();
    this.scent().settle();
    this.scent().setPhase(ScentPhase.NONE);
    this.scent().cues().whine();
  }

  private void holdGiveUp() {
    final LivingEntity owner = this.dog.getOwner();
    if (owner != null) {
      this.dog.getLookControl().lookAt(owner, LOOK_SPEED, LOOK_SPEED);
    }
    if (--this.holdTicks <= 0) {
      this.scent().stop();
    }
  }

  private void holdPoint(final ScentTargetFinder activeFinder) {
    final Vec3d target = activeFinder.target();
    if (target != null) {
      this.dog.getLookControl().lookAt(target.x, target.y, target.z, LOOK_SPEED, LOOK_SPEED);
    }
    if (--this.holdTicks <= 0) {
      this.scent().stop();
    }
  }

  private boolean isFree() {
    return this.dog.getOwner() != null
        && this.dog.getTarget() == null
        && !this.dog.isInSittingPose()
        && !this.dog.isLeashed()
        && !this.dog.getSleepController().isAsleep()
        && !this.dog.getFetchCarry().isCarrying();
  }

  private ServerWorld world() {
    return (ServerWorld) this.dog.getWorld();
  }

  private DogScent scent() {
    return this.dog.getComponents().get(DogScent.class);
  }
}
