package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.burial.BoneCaches;
import com.grahambartley.dogsunleashed.entity.burial.BoneThieves;
import com.grahambartley.dogsunleashed.entity.burial.FoxRaidRules;
import java.util.EnumSet;
import java.util.function.BooleanSupplier;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class FoxRaidCacheGoal extends Goal {

  private static final int SEARCH_INTERVAL_TICKS = 100;
  private static final int RUN_OFF_TICKS = 100;
  private static final int RUN_OFF_RANGE = 16;
  private static final int RUN_OFF_HEIGHT = 7;
  private static final double RUN_OFF_SPEED = 1.5;

  private final FoxEntity fox;
  private final BooleanSupplier raidRoll;
  private @Nullable DigSiteVisit visit;
  private int searchCooldown;
  private int runOffTicks;

  public FoxRaidCacheGoal(final FoxEntity fox) {
    this(fox, () -> FoxRaidRules.rollsRaid(fox.getRandom().nextInt(FoxRaidRules.RAID_ODDS)));
  }

  public FoxRaidCacheGoal(final FoxEntity fox, final BooleanSupplier raidRoll) {
    this.fox = fox;
    this.raidRoll = raidRoll;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (--this.searchCooldown > 0 || !this.fox.isOnGround()) {
      return false;
    }
    this.searchCooldown = SEARCH_INTERVAL_TICKS;
    if (!this.raidRoll.getAsBoolean()) {
      return false;
    }
    this.visit =
        BoneThieves.pickCache(this.world(), this.fox)
            .map(ground -> new DigSiteVisit(this.fox, ground, digging -> {}))
            .filter(DigSiteVisit::canReach)
            .orElse(null);
    return this.visit != null;
  }

  @Override
  public void start() {
    if (this.visit != null) {
      BoneThieves.setTarget(this.fox, this.visit.ground());
    }
  }

  @Override
  public boolean shouldContinue() {
    if (this.runOffTicks > 0) {
      return !this.fox.getNavigation().isIdle();
    }
    return this.visit != null
        && BoneThieves.mayRaid(this.fox)
        && BoneCaches.get(this.world()).at(this.visit.ground()).isPresent();
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void tick() {
    if (this.runOffTicks > 0) {
      this.runOffTicks--;
      return;
    }
    if (this.visit == null) {
      return;
    }
    this.fox.setCrouching(true);
    switch (this.visit.tick(this.world())) {
      case DUG -> this.stealAndRun(this.visit.ground());
      case UNREACHABLE -> this.visit = null;
      case WALKING, DIGGING -> {}
    }
  }

  @Override
  public void stop() {
    this.fox.getNavigation().stop();
    this.fox.setCrouching(false);
    BoneThieves.clearTarget(this.fox);
    this.visit = null;
    this.runOffTicks = 0;
  }

  private void stealAndRun(final BlockPos ground) {
    this.visit = null;
    if (!BoneThieves.steal(this.world(), this.fox, ground)) {
      return;
    }
    this.fox.setCrouching(false);
    this.fox.playSound(SoundEvents.ENTITY_FOX_SNIFF, 1.0F, 1.0F);
    final Vec3d away =
        NoPenaltyTargeting.findFrom(
            this.fox, RUN_OFF_RANGE, RUN_OFF_HEIGHT, Vec3d.ofBottomCenter(ground));
    if (away != null
        && this.fox.getNavigation().startMovingTo(away.x, away.y, away.z, RUN_OFF_SPEED)) {
      this.runOffTicks = RUN_OFF_TICKS;
    }
  }

  private ServerWorld world() {
    return (ServerWorld) this.fox.getWorld();
  }
}
