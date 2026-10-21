package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.Comparator;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.raid.RaiderEntity;
import org.jetbrains.annotations.Nullable;

public class RaidAlertGoal extends Goal {

  private final UnleashedDogEntity dog;
  private @Nullable RaiderEntity intruder;
  private int scanCooldown;
  private int barkCooldown;

  public RaidAlertGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (!this.listens() || this.scanCooldown-- > 0) {
      return false;
    }
    this.scanCooldown = RaidAlertRules.SCAN_INTERVAL_TICKS;
    this.intruder = this.nearestIntruder();
    return this.intruder != null;
  }

  @Override
  public boolean shouldContinue() {
    return this.listens()
        && this.intruder != null
        && this.intruder.isAlive()
        && RaidAlertRules.inRange(this.dog.squaredDistanceTo(this.intruder));
  }

  @Override
  public void start() {
    this.barkCooldown = 0;
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void tick() {
    if (this.intruder == null) {
      return;
    }
    this.dog.getLookControl().lookAt(this.intruder);
    if (this.barkCooldown-- <= 0) {
      this.dog.getVocalization().alertBark();
      this.barkCooldown = RaidAlertRules.REPEAT_TICKS;
    }
  }

  @Override
  public void stop() {
    this.intruder = null;
  }

  private boolean listens() {
    return RaidAlertRules.listens(
        this.dog.isTamed(), this.dog.getSleepController().isAsleep(), this.dog.getCommand());
  }

  private @Nullable RaiderEntity nearestIntruder() {
    return this.dog
        .getWorld()
        .getEntitiesByClass(
            RaiderEntity.class,
            this.dog.getBoundingBox().expand(RaidAlertRules.ALERT_RANGE),
            raider ->
                raider.isAlive()
                    && RaidAlertRules.isIntruder(
                        raider instanceof IllagerEntity,
                        raider.hasActiveRaid(),
                        raider.isPatrolLeader() || raider.hasPatrolTarget())
                    && RaidAlertRules.inRange(this.dog.squaredDistanceTo(raider)))
        .stream()
        .min(Comparator.comparingDouble(this.dog::squaredDistanceTo))
        .orElse(null);
  }
}
