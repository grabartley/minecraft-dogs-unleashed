package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogBoneBurial;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.burial.BoneThieves;
import com.grahambartley.dogsunleashed.entity.burial.FoxRaidRules;
import java.util.Comparator;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ChaseBoneThiefGoal extends Goal {

  private static final int SCAN_INTERVAL_TICKS = 20;
  private static final int COOLDOWN_TICKS = 400;
  private static final int REPATH_INTERVAL_TICKS = 10;
  private static final double SPEED = 1.4;

  private final UnleashedDogEntity dog;
  private @Nullable FoxEntity thief;
  private Vec3d start = Vec3d.ZERO;
  private int scanCooldown;
  private int chaseTicks;

  public ChaseBoneThiefGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (--this.scanCooldown > 0 || !this.isFreeToChase()) {
      return false;
    }
    this.scanCooldown = SCAN_INTERVAL_TICKS;
    this.thief = this.nearestThief();
    return this.thief != null;
  }

  @Override
  public void start() {
    this.start = this.dog.getPos();
    this.chaseTicks = 0;
    this.dog.getVocalization().forceBark(this.dog.getVocalization().getBarkPitch());
  }

  @Override
  public boolean shouldContinue() {
    return this.thief != null
        && this.thief.isAlive()
        && this.isFreeToChase()
        && !FoxRaidRules.chaseIsOver(this.chaseTicks, this.dog.getPos().distanceTo(this.start));
  }

  @Override
  public void tick() {
    if (this.thief == null) {
      return;
    }
    this.dog.getLookControl().lookAt(this.thief, 30.0F, 30.0F);
    if (this.chaseTicks++ % REPATH_INTERVAL_TICKS == 0) {
      this.dog.getNavigation().startMovingTo(this.thief, SPEED);
    }
    this.dog.getVocalization().barkIfReady(this.dog.getVocalization().getBarkPitch());
  }

  @Override
  public void stop() {
    this.dog.getNavigation().stop();
    this.thief = null;
    this.scanCooldown = COOLDOWN_TICKS;
  }

  public @Nullable FoxEntity thief() {
    return this.thief;
  }

  private boolean isFreeToChase() {
    return this.dog.isTamed()
        && !this.dog.isOnFire()
        && this.dog.getComponents().get(DogBoneBurial.class).isFreeToDig();
  }

  private @Nullable FoxEntity nearestThief() {
    return this.dog
        .getWorld()
        .getEntitiesByClass(
            FoxEntity.class,
            this.dog.getBoundingBox().expand(FoxRaidRules.GUARD_RANGE),
            fox ->
                BoneThieves.isThief(fox)
                    && fox.squaredDistanceTo(this.dog)
                        <= FoxRaidRules.GUARD_RANGE * FoxRaidRules.GUARD_RANGE)
        .stream()
        .min(Comparator.comparingDouble(fox -> fox.squaredDistanceTo(this.dog)))
        .orElse(null);
  }
}
