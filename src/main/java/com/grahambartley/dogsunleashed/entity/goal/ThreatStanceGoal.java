package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogAnimationKeys;
import com.grahambartley.dogsunleashed.entity.DogFriendlyTargetPolicy;
import com.grahambartley.dogsunleashed.entity.DogPersonality;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.personality.DogTraits;
import java.util.Comparator;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class ThreatStanceGoal extends Goal {

  private static final double STANCE_SPEED = 1.2;
  private static final float LOOK_YAW = 30.0f;
  private static final float LOOK_PITCH = 30.0f;

  private final UnleashedDogEntity dog;
  private @Nullable LivingEntity threat;
  private int stanceTicks;

  public ThreatStanceGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    final LivingEntity owner = this.ownerInRange();
    if (!this.takesStance() || owner == null) {
      return false;
    }
    this.threat = this.nearestThreat(owner);
    return this.threat != null;
  }

  @Override
  public boolean shouldContinue() {
    final LivingEntity owner = this.ownerInRange();
    return this.takesStance()
        && owner != null
        && this.threat != null
        && this.threat.isAlive()
        && ThreatStanceRules.inRange(
            this.threat.squaredDistanceTo(owner), ThreatStanceRules.THREAT_RANGE);
  }

  @Override
  public void start() {
    this.stanceTicks = 0;
  }

  @Override
  public void tick() {
    final LivingEntity owner = this.dog.getOwner();
    if (owner == null || this.threat == null) {
      return;
    }
    this.dog.getLookControl().lookAt(this.threat, LOOK_YAW, LOOK_PITCH);
    if (this.stanceTicks % ThreatStanceRules.REPATH_TICKS == 0) {
      final Vec3d spot =
          ThreatStanceRules.stancePoint(
              owner.getPos(), this.threat.getPos(), this.traits().stanceOffset());
      this.dog.getNavigation().startMovingTo(spot.x, owner.getY(), spot.z, STANCE_SPEED);
    }
    if (this.traits().growlsAtThreats()) {
      this.dog.getGrowl().standOff(this.threat);
    }
    if (this.traits().cowersAtThreats() && ThreatStanceRules.cowersNow(this.stanceTicks)) {
      this.dog.triggerAnim(DogAnimationKeys.COWER, DogAnimationKeys.COWER);
    }
    this.stanceTicks++;
  }

  @Override
  public void stop() {
    this.dog.getGrowl().standOff(null);
    this.dog.getNavigation().stop();
    this.threat = null;
  }

  public @Nullable LivingEntity threat() {
    return this.threat;
  }

  private boolean takesStance() {
    return this.dog.isTamed()
        && ThreatStanceRules.takesStance(
            this.dog.getCommand(),
            this.traits().stanceOffset(),
            this.dog.getTarget() != null,
            this.dog.isInSittingPose());
  }

  private @Nullable LivingEntity ownerInRange() {
    final LivingEntity owner = this.dog.getOwner();
    if (owner == null
        || owner.getWorld() != this.dog.getWorld()
        || !ThreatStanceRules.inRange(
            this.dog.squaredDistanceTo(owner), ThreatStanceRules.OWNER_RANGE)) {
      return null;
    }
    return owner;
  }

  private @Nullable LivingEntity nearestThreat(final LivingEntity owner) {
    return this.dog
        .getWorld()
        .getEntitiesByClass(
            MobEntity.class,
            owner.getBoundingBox().expand(ThreatStanceRules.THREAT_RANGE),
            mob ->
                mob instanceof Monster
                    && mob.isAlive()
                    && !DogFriendlyTargetPolicy.isFriendlyTarget(mob)
                    && ThreatStanceRules.inRange(
                        mob.squaredDistanceTo(owner), ThreatStanceRules.THREAT_RANGE))
        .stream()
        .min(Comparator.comparingDouble(mob -> mob.squaredDistanceTo(owner)))
        .orElse(null);
  }

  private DogTraits traits() {
    return this.dog.getComponents().get(DogPersonality.class).traits();
  }
}
