package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogTug;
import com.grahambartley.dogsunleashed.entity.TugState;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules.Spot;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;

public class TugGoal extends Goal {

  private static final double RUN_SPEED = 1.2;
  private static final int REPATH_TICKS = 10;

  private final UnleashedDogEntity dog;
  private int repathCooldown;

  public TugGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
  }

  @Override
  public boolean canStart() {
    return this.tug().state() != null;
  }

  @Override
  public boolean shouldContinue() {
    return this.canStart();
  }

  @Override
  public void start() {
    this.repathCooldown = 0;
  }

  @Override
  public void stop() {
    this.dog.getNavigation().stop();
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void tick() {
    final TugState state = this.tug().state();
    final PlayerEntity player = this.tug().player();
    if (state == null || player == null) {
      return;
    }
    this.dog.getLookControl().lookAt(player);
    if (!this.tug().isKeepingAway()) {
      this.dog.getNavigation().stop();
      return;
    }
    if (--this.repathCooldown > 0) {
      return;
    }
    this.repathCooldown = REPATH_TICKS;
    final Spot spot =
        PlayChaseRules.fleeTarget(
            state.centre(),
            new Spot(this.dog.getX(), this.dog.getZ()),
            new Spot(player.getX(), player.getZ()));
    StandingGround.runTo(this.dog, spot, RUN_SPEED);
  }

  private DogTug tug() {
    return this.dog.getComponents().get(DogTug.class);
  }
}
