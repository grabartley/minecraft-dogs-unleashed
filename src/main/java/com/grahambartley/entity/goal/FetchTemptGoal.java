package com.grahambartley.entity.goal;

import com.grahambartley.entity.UnleashedDogEntity;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.recipe.Ingredient;

public class FetchTemptGoal extends TemptGoal {

  private final UnleashedDogEntity dog;

  public FetchTemptGoal(
      UnleashedDogEntity dog, double speed, Ingredient ingredient, boolean canBeScared) {
    super(dog, speed, ingredient, canBeScared);
    this.dog = dog;
  }

  @Override
  public boolean canStart() {
    if (this.isTemptSuppressed()) {
      return false;
    }
    if (!super.canStart()) {
      return false;
    }
    return this.canFollowClosestPlayer();
  }

  @Override
  public boolean shouldContinue() {
    if (this.isTemptSuppressed()) {
      return false;
    }

    return super.shouldContinue() && this.canFollowClosestPlayer();
  }

  @Override
  public void tick() {
    if (this.isTemptSuppressed()) {
      return;
    }
    super.tick();
  }

  private boolean isTemptSuppressed() {
    if (this.dog.isInPlayMode()) {
      return this.dog.isActivelyFetching();
    }

    return UnleashedDogEntity.isAnyDogInPlayMode();
  }

  private boolean canFollowClosestPlayer() {
    if (this.closestPlayer == null) {
      return true;
    }

    if (this.dog.isInPlayMode()
        && this.closestPlayer.getUuid().equals(this.dog.getPlayPartnerPlayerUuid())) {
      return true;
    }

    return !UnleashedDogEntity.isAnyDogInPlayModeFor(this.closestPlayer.getUuid());
  }
}
