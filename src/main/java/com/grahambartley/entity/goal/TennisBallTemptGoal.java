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
      return false;
    }
    if (!super.canStart()) {
      return false;
    }
    return this.closestPlayer == null
        || !UnleashedDogEntity.isAnyDogInPlayModeFor(this.closestPlayer.getUuid());
  }
}
