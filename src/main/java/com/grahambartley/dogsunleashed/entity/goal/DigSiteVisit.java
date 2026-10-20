package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.Headings;
import com.grahambartley.dogsunleashed.entity.burial.BoneCacheRules;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

final class DigSiteVisit {

  enum Status {
    WALKING,
    DIGGING,
    DUG,
    UNREACHABLE
  }

  private static final double SPEED = 1.0;
  private static final int MAX_WALK_TICKS = 600;
  private static final int REPATH_INTERVAL_TICKS = 10;
  private static final int EFFECT_INTERVAL_TICKS = 5;
  private static final int DIG_PARTICLES = 6;
  private static final double DIG_PARTICLE_SPREAD = 0.3;
  private static final double DIG_PARTICLE_RISE = 0.1;
  private static final float DIG_SOUND_VOLUME = 0.6F;

  private final MobEntity mob;
  private final BlockPos ground;
  private final BooleanConsumer digging;
  private int walkTicks;
  private int digTicks = -1;
  private int repathCooldown;

  DigSiteVisit(final MobEntity mob, final BlockPos ground, final BooleanConsumer digging) {
    this.mob = mob;
    this.ground = ground.toImmutable();
    this.digging = digging;
  }

  BlockPos ground() {
    return this.ground;
  }

  Status tick(final ServerWorld world) {
    if (this.digTicks >= 0) {
      return this.dig(world);
    }
    if (this.isBeside()) {
      this.mob.getNavigation().stop();
      this.digTicks = BoneCacheRules.DIG_TICKS;
      this.digging.accept(true);
      return Status.DIGGING;
    }
    if (++this.walkTicks > MAX_WALK_TICKS) {
      return Status.UNREACHABLE;
    }
    if (--this.repathCooldown <= 0) {
      this.repathCooldown = REPATH_INTERVAL_TICKS;
      this.mob
          .getNavigation()
          .startMovingTo(
              this.ground.getX() + 0.5, this.ground.getY() + 1, this.ground.getZ() + 0.5, SPEED);
    }
    return Status.WALKING;
  }

  private Status dig(final ServerWorld world) {
    this.faceGround();
    if (this.digTicks % EFFECT_INTERVAL_TICKS == 0) {
      this.digEffects(world);
    }
    return --this.digTicks <= 0 ? Status.DUG : Status.DIGGING;
  }

  private void faceGround() {
    final float yaw =
        Headings.yawToward(
            this.mob.getX(), this.mob.getZ(), this.ground.getX() + 0.5, this.ground.getZ() + 0.5);
    this.mob.setYaw(yaw);
    this.mob.setBodyYaw(yaw);
    this.mob.setHeadYaw(yaw);
  }

  private void digEffects(final ServerWorld world) {
    final BlockState state = world.getBlockState(this.ground);
    final Vec3d top = Vec3d.ofCenter(this.ground).add(0.0, 0.5, 0.0);
    world.spawnParticles(
        new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
        top.x,
        top.y,
        top.z,
        DIG_PARTICLES,
        DIG_PARTICLE_SPREAD,
        DIG_PARTICLE_RISE,
        DIG_PARTICLE_SPREAD,
        0.0);
    this.mob.playSound(
        state.getSoundGroup().getHitSound(), DIG_SOUND_VOLUME, state.getSoundGroup().getPitch());
  }

  boolean canReach() {
    final Path path = this.mob.getNavigation().findPathTo(this.ground.up(), 1);
    return path != null && path.reachesTarget();
  }

  private boolean isBeside() {
    return DigSiteRules.isBeside(this.ground, this.mob.getX(), this.mob.getY(), this.mob.getZ());
  }
}
