package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogTrickPerformance;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;

public class PerformTrickGoal extends Goal {

  private final UnleashedDogEntity dog;

  public PerformTrickGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
  }

  @Override
  public boolean canStart() {
    return !this.dog.isInSittingPose() && this.shouldContinue();
  }

  @Override
  public boolean shouldContinue() {
    return !this.dog.isSitting()
        && this.dog.getComponents().get(DogTrickPerformance.class).isPerforming();
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void start() {
    this.holdStill();
  }

  @Override
  public void tick() {
    this.holdStill();
  }

  private void holdStill() {
    this.dog.getNavigation().stop();
    this.dog.setForwardSpeed(0.0f);
    this.dog.setSidewaysSpeed(0.0f);
  }
}
