package com.grahambartley.entity.goal;

import com.grahambartley.entity.UnleashedDogEntity;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.recipe.Ingredient;

public class TennisBallTemptGoal extends TemptGoal {

  private final UnleashedDogEntity dog;

  public TennisBallTemptGoal(
      UnleashedDogEntity dog, double speed, Ingredient ingredient, boolean canBeScared) {
    super(dog, speed, ingredient, canBeScared);
    this.dog = dog;
  }

  @Override
  public boolean canStart() {
    if (this.dog.isInPlayMode()) {
      return !this.dog.isActivelyFetchingBall() && super.canStart();
    }

    if (UnleashedDogEntity.isAnyDogInPlayMode()) {
      return false;
    }
    if (!super.canStart()) {
      return false;
    }
    return this.closestPlayer == null
        || !UnleashedDogEntity.isAnyDogInPlayModeFor(this.closestPlayer.getUuid());
  }

  @Override
  public boolean shouldContinue() {
    if (this.dog.isInPlayMode()) {
      return !this.dog.isActivelyFetchingBall() && super.shouldContinue();
    }

    if (UnleashedDogEntity.isAnyDogInPlayMode()) {
      return false;
    }

    return super.shouldContinue()
        && (this.closestPlayer == null
            || !UnleashedDogEntity.isAnyDogInPlayModeFor(this.closestPlayer.getUuid()));
  }
}
