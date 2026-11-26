package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogPetting;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;

public class PettedGoal extends Goal {

  private static final float LOOK_TURN_DEGREES = 30.0f;

  private final UnleashedDogEntity dog;

  public PettedGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
  }

  @Override
  public boolean canStart() {
    return this.petting().currentPetter().isPresent();
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void start() {
    this.holdStillFacingThePetter();
  }

  @Override
  public void tick() {
    this.holdStillFacingThePetter();
  }

  private void holdStillFacingThePetter() {
    this.dog.getNavigation().stop();
    this.dog.setForwardSpeed(0.0f);
    this.dog.setSidewaysSpeed(0.0f);
    this.petting()
        .currentPetter()
        .ifPresent(
            petter ->
                this.dog.getLookControl().lookAt(petter, LOOK_TURN_DEGREES, LOOK_TURN_DEGREES));
  }

  private DogPetting petting() {
    return this.dog.getComponents().get(DogPetting.class);
  }
}
