package com.grahambartley.dogsunleashed.entity;

/**
 * Decides whether a dog is allowed to panic away from danger. A dog commanded to Guard or Hunt has
 * been told to engage, so it holds its ground when attacked instead of breaking off. Fire is the
 * one danger it still runs from, because burning to death on the spot reads as broken rather than
 * brave.
 */
public final class DogPanicPolicy {

  private DogPanicPolicy() {}

  public static boolean shouldFleeDanger(final DogCommand command, final boolean onFire) {
    return onFire || !holdsGroundInCombat(command);
  }

  private static boolean holdsGroundInCombat(final DogCommand command) {
    return command == DogCommand.GUARD || command == DogCommand.HUNT;
  }
}
