package com.grahambartley.dogsunleashed.entity;

public final class DogBowlMeal extends DogComponent {

  private final UnleashedDogEntity dog;

  DogBowlMeal(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public boolean isEating() {
    return this.dog.getDataTracker().get(UnleashedDogEntity.BOWL_MEAL);
  }

  public void setEating(final boolean eating) {
    this.dog.getDataTracker().set(UnleashedDogEntity.BOWL_MEAL, eating);
  }
}
