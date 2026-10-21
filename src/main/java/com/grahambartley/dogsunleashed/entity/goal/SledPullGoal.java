package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogSledHarness;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.sled.SledDrive;
import com.grahambartley.dogsunleashed.entity.sled.SledEntity;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Vec3d;

public class SledPullGoal extends Goal {

  static final double ARRIVED_DISTANCE = 0.35;
  static final double LOOK_AHEAD = 4.0;

  private final UnleashedDogEntity dog;

  public SledPullGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    return this.harness().isHitched();
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
    this.dog.getNavigation().stop();
    this.dog.setInSittingPose(false);
  }

  @Override
  public void stop() {
    this.dog.getNavigation().stop();
  }

  @Override
  public void tick() {
    final SledEntity sled = this.harness().sled();
    final SledDrive.Target target = sled == null ? null : sled.getDrive().targetFor(this.dog);
    if (target == null) {
      return;
    }
    final Vec3d to = target.position().subtract(this.dog.getPos());
    if (to.horizontalLength() > ARRIVED_DISTANCE) {
      this.dog
          .getMoveControl()
          .moveTo(
              target.position().x,
              target.position().y,
              target.position().z,
              target.speedMultiplier());
      if (this.dog.horizontalCollision && this.dog.isOnGround()) {
        this.dog.getJumpControl().setActive();
      }
    }
    final Vec3d ahead = this.dog.getPos().add(target.facing().multiply(LOOK_AHEAD));
    this.dog.getLookControl().lookAt(ahead.x, this.dog.getEyeY(), ahead.z);
  }

  private DogSledHarness harness() {
    return this.dog.getComponents().get(DogSledHarness.class);
  }
}
