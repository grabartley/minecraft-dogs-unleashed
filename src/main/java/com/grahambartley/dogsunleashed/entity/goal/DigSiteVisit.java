package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogBoneBurial;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.burial.BoneCacheRules;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

final class DigSiteVisit {

  enum Status {
    WALKING,
    DIGGING,
    DUG,
    UNREACHABLE
  }

  private static final double SPEED = 1.0;
  private static final double REACH = 1.5;
  private static final int MAX_WALK_TICKS = 600;
  private static final int REPATH_INTERVAL_TICKS = 10;
  private static final int EFFECT_INTERVAL_TICKS = 5;
  private static final float LOOK_SPEED = 30.0F;

  private final UnleashedDogEntity dog;
  private final BlockPos ground;
  private int walkTicks;
  private int digTicks = -1;
  private int repathCooldown;

  DigSiteVisit(final UnleashedDogEntity dog, final BlockPos ground) {
    this.dog = dog;
    this.ground = ground.toImmutable();
  }

  BlockPos ground() {
    return this.ground;
  }

  Status tick(final ServerWorld world) {
    if (this.digTicks >= 0) {
      return this.dig(world);
    }
    if (this.isBeside()) {
      this.dog.getNavigation().stop();
      this.digTicks = BoneCacheRules.DIG_TICKS;
      this.burial().setDigging(true);
      return Status.DIGGING;
    }
    if (++this.walkTicks > MAX_WALK_TICKS) {
      return Status.UNREACHABLE;
    }
    if (--this.repathCooldown <= 0) {
      this.repathCooldown = REPATH_INTERVAL_TICKS;
      this.dog
          .getNavigation()
          .startMovingTo(
              this.ground.getX() + 0.5, this.ground.getY() + 1, this.ground.getZ() + 0.5, SPEED);
    }
    return Status.WALKING;
  }

  private Status dig(final ServerWorld world) {
    this.dog
        .getLookControl()
        .lookAt(
            this.ground.getX() + 0.5,
            this.ground.getY() + 0.5,
            this.ground.getZ() + 0.5,
            LOOK_SPEED,
            LOOK_SPEED);
    if (this.digTicks % EFFECT_INTERVAL_TICKS == 0) {
      this.burial().digEffects(world, this.ground);
    }
    return --this.digTicks <= 0 ? Status.DUG : Status.DIGGING;
  }

  private boolean isBeside() {
    final double dx = this.dog.getX() - (this.ground.getX() + 0.5);
    final double dz = this.dog.getZ() - (this.ground.getZ() + 0.5);
    final double dy = this.dog.getY() - (this.ground.getY() + 1);
    return Math.hypot(dx, dz) <= REACH && Math.abs(dy) <= REACH;
  }

  private DogBoneBurial burial() {
    return this.dog.getComponents().get(DogBoneBurial.class);
  }
}
