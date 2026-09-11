package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogPanicPolicy;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;

public class DogEscapeDangerGoal extends EscapeDangerGoal {

  private final UnleashedDogEntity dog;

  public DogEscapeDangerGoal(final UnleashedDogEntity dog, final double speed) {
    super(dog, speed);
    this.dog = dog;
  }

  @Override
  public boolean canStart() {
    return this.mayFlee() && super.canStart();
  }

  @Override
  public boolean shouldContinue() {
    return this.mayFlee() && super.shouldContinue();
  }

  private boolean mayFlee() {
    return DogPanicPolicy.shouldFleeDanger(this.dog.getCommand(), this.dog.isOnFire());
  }
}
