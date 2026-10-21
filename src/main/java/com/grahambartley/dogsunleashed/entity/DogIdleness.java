package com.grahambartley.dogsunleashed.entity;

public final class DogIdleness {

  private DogIdleness() {}

  public static boolean isFreeForChores(final UnleashedDogEntity dog) {
    final DogPointedOrders orders = dog.getComponents().get(DogPointedOrders.class);
    return dog.getTarget() == null
        && !dog.isInSittingPose()
        && !dog.isLeashed()
        && !dog.getSleepController().isAsleep()
        && !dog.getPlaySession().isInPlayMode()
        && !dog.getComponents().get(DogScent.class).isTracking()
        && dog.getComponents().get(DogTug.class).state() == null
        && orders.attackTarget() == null
        && orders.fetchItem() == null
        && !dog.getCommandController().isHeadingToAnchor();
  }
}
