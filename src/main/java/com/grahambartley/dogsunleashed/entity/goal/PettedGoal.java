package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogPetting;
import com.grahambartley.dogsunleashed.entity.PetSpot;
import com.grahambartley.dogsunleashed.entity.PettingRules;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.player.PlayerEntity;

public class PettedGoal extends Goal {

  private static final float LOOK_TURN_DEGREES = 30.0f;
  private static final double APPROACH_SPEED = 1.0;
  private static final int REPATH_TICKS = 10;

  private final UnleashedDogEntity dog;
  private int repathIn;

  public PettedGoal(final UnleashedDogEntity dog) {
    this.dog = dog;
    this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
  }

  @Override
  public boolean canStart() {
    return this.petting().approachingPetter().isPresent()
        || this.petting().currentPetter().isPresent();
  }

  @Override
  public boolean shouldRunEveryTick() {
    return true;
  }

  @Override
  public void start() {
    this.repathIn = 0;
    this.tick();
  }

  @Override
  public void stop() {
    this.dog.getNavigation().stop();
  }

  @Override
  public void tick() {
    final Optional<PlayerEntity> approaching = this.petting().approachingPetter();
    if (approaching.isPresent() && !this.petting().isAtHandOf(approaching.get())) {
      this.walkToHandOf(approaching.get());
      return;
    }
    this.holdStill();
  }

  private void walkToHandOf(final PlayerEntity petter) {
    this.dog.getLookControl().lookAt(petter, LOOK_TURN_DEGREES, LOOK_TURN_DEGREES);
    final PetSpot.Place spot = this.petting().spotBeside(petter);
    if (PettingRules.closeEnoughToStep(
        Math.hypot(spot.x() - this.dog.getX(), spot.z() - this.dog.getZ()),
        this.dog.getWidth() / 2.0)) {
      this.dog.getNavigation().stop();
      this.dog.getMoveControl().moveTo(spot.x(), petter.getY(), spot.z(), APPROACH_SPEED);
      return;
    }
    if (--this.repathIn > 0 && !this.dog.getNavigation().isIdle()) {
      return;
    }
    this.repathIn = REPATH_TICKS;
    this.dog
        .getNavigation()
        .startMovingAlong(
            this.dog.getNavigation().findPathTo(spot.x(), petter.getY(), spot.z(), 0),
            APPROACH_SPEED);
  }

  private void holdStill() {
    this.dog.getNavigation().stop();
    this.dog.setForwardSpeed(0.0f);
    this.dog.setSidewaysSpeed(0.0f);
  }

  private DogPetting petting() {
    return this.dog.getComponents().get(DogPetting.class);
  }
}
