package com.grahambartley.dogsunleashed.block;

import java.util.Optional;
import java.util.UUID;

public final class DogSleepSpotHandover {

  private DogSleepSpotHandover() {}

  public static Optional<UUID> displaced(final UUID currentDog, final UUID incomingDog) {
    if (currentDog == null || currentDog.equals(incomingDog)) {
      return Optional.empty();
    }
    return Optional.of(currentDog);
  }

  public static boolean isTakenByAnother(final UUID currentDog, final UUID dog) {
    return displaced(currentDog, dog).isPresent();
  }
}
