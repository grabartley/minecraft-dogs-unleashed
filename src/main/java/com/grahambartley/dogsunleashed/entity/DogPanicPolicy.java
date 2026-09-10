package com.grahambartley.dogsunleashed.entity;

public final class DogPanicPolicy {

  private DogPanicPolicy() {}

  public static boolean shouldFleeDanger(final DogCommand command, final boolean onFire) {
    return onFire || !holdsGroundInCombat(command);
  }

  private static boolean holdsGroundInCombat(final DogCommand command) {
    return command == DogCommand.GUARD || command == DogCommand.HUNT;
  }
}
