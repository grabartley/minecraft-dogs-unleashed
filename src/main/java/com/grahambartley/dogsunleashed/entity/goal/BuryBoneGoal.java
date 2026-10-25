package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogBoneBurial;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.burial.BoneCacheRules;
import com.grahambartley.dogsunleashed.entity.burial.BurialGround;
import java.util.EnumSet;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class BuryBoneGoal extends Goal {

  private static final int SEARCH_TICKS = 40;
  private static final int SNIFF_RADIUS = 3;
  private static final int SNIFF_HEIGHT = 2;
  private static final double SNIFF_SPEED = 0.8;

  private final UnleashedDogEntity dog;
  private @Nullable DigSiteVisit visit;
  private int searchTicks;
  private int sniffTicks;

  public BuryBoneGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    return this.burial().canBuryNow();
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
    this.visit = null;
    this.searchTicks = 0;
    this.sniffTicks =
        BoneCacheRules.sniffTicks(
            this.dog.getRandom().nextInt(BoneCacheRules.SNIFF_SPREAD_TICKS + 1));
  }

  @Override
  public void tick() {
    if (!this.burial().isBuryPending()) {
      return;
    }
    if (this.sniffTicks > 0) {
      this.sniff();
      return;
    }
    if (this.visit == null) {
      this.search();
      return;
    }
    switch (this.visit.tick(this.world())) {
      case DUG -> this.finish();
      case UNREACHABLE -> this.giveUp();
      case WALKING, DIGGING -> {}
    }
  }

  private void sniff() {
    this.sniffTicks--;
    if (!this.dog.getNavigation().isIdle()) {
      return;
    }
    final Vec3d spot = NoPenaltyTargeting.find(this.dog, SNIFF_RADIUS, SNIFF_HEIGHT);
    if (spot != null) {
      this.dog.getNavigation().startMovingTo(spot.x, spot.y, spot.z, SNIFF_SPEED);
      this.dog.getLookControl().lookAt(spot.x, spot.y - 1, spot.z);
    }
  }

  private void search() {
    this.visit =
        BurialGround.pickSpot(this.world(), this.dog.getBlockPos(), this.dog.getRandom())
            .map(ground -> new DigSiteVisit(this.dog, ground, this.burial()::setDigging))
            .filter(DigSiteVisit::canReach)
            .orElse(null);
    if (this.visit == null && ++this.searchTicks >= SEARCH_TICKS) {
      this.giveUp();
    }
  }

  @Override
  public void stop() {
    this.dog.getNavigation().stop();
    this.visit = null;
    this.burial().abandonBurial();
  }

  private void finish() {
    if (this.visit == null) {
      return;
    }
    if (!BurialGround.isBurialSpot(this.world(), this.visit.ground())
        || !this.burial().bury(this.world(), this.visit.ground())) {
      this.giveUp();
      return;
    }
    this.visit = null;
  }

  private void giveUp() {
    this.burial().tellOwner("message.dogs-unleashed.bone.no_spot");
    this.burial().abandonBurial();
    this.visit = null;
  }

  private ServerWorld world() {
    return (ServerWorld) this.dog.getWorld();
  }

  private DogBoneBurial burial() {
    return this.dog.getComponents().get(DogBoneBurial.class);
  }
}
