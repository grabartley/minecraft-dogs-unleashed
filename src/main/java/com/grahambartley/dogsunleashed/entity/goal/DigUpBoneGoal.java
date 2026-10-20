package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogBoneBurial;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.burial.BoneCache;
import com.grahambartley.dogsunleashed.entity.burial.BoneCacheRules;
import com.grahambartley.dogsunleashed.entity.burial.BoneCaches;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public class DigUpBoneGoal extends Goal {

  private static final int SEARCH_INTERVAL_TICKS = 100;
  private static final int UNREACHABLE_COOLDOWN_TICKS = 1200;

  private final UnleashedDogEntity dog;
  private @Nullable DigSiteVisit visit;
  private int searchCooldown;

  public DigUpBoneGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    if (--this.searchCooldown > 0 || !this.dog.isOnGround() || !this.isReady()) {
      return false;
    }
    this.searchCooldown = SEARCH_INTERVAL_TICKS;
    final Optional<BoneCache> due =
        BoneCaches.get(this.world())
            .nearestDue(
                this.dog.getUuid(),
                this.dog.getBlockPos(),
                BoneCacheRules.DIG_UP_RANGE,
                this.world().getTimeOfDay());
    this.visit =
        due.map(cache -> new DigSiteVisit(this.dog, cache.pos(), this.burial()::setDigging))
            .filter(DigSiteVisit::canReach)
            .orElse(null);
    if (due.isPresent() && this.visit == null) {
      this.searchCooldown = UNREACHABLE_COOLDOWN_TICKS;
    }
    return this.visit != null;
  }

  @Override
  public boolean shouldContinue() {
    return this.visit != null
        && this.isReady()
        && BoneCaches.get(this.world()).at(this.visit.ground()).isPresent();
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void tick() {
    if (this.visit == null) {
      return;
    }
    switch (this.visit.tick(this.world())) {
      case DUG -> {
        if (this.burial().digUp(this.world(), this.visit.ground())) {
          this.burial().tellOwner("message.dogs-unleashed.bone.dug_up");
        }
        this.visit = null;
      }
      case UNREACHABLE -> {
        this.searchCooldown = UNREACHABLE_COOLDOWN_TICKS;
        this.visit = null;
      }
      case WALKING, DIGGING -> {}
    }
  }

  @Override
  public void stop() {
    this.dog.getNavigation().stop();
    this.burial().setDigging(false);
    this.visit = null;
  }

  private boolean isReady() {
    final LivingEntity owner = this.dog.getOwner();
    return owner != null
        && this.dog.distanceTo(owner) <= BoneCacheRules.OWNER_RANGE
        && !this.dog.getFetchCarry().isCarrying()
        && this.burial().isFreeToDig();
  }

  private ServerWorld world() {
    return (ServerWorld) this.dog.getWorld();
  }

  private DogBoneBurial burial() {
    return this.dog.getComponents().get(DogBoneBurial.class);
  }
}
