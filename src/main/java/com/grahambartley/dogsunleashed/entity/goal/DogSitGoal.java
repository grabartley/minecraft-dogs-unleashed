package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogDeathWatch;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.entity.ai.goal.SitGoal;

public class DogSitGoal extends SitGoal {

  private final UnleashedDogEntity dog;

  public DogSitGoal(final UnleashedDogEntity dog) {
    super(dog);
    this.dog = dog;
  }

  @Override
  public boolean canStart() {
    return !this.dog.getComponents().get(DogDeathWatch.class).isKeepingWatch() && super.canStart();
  }
}
