package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.personality.DogTraits;

public final class DogPanicPolicy {

  private DogPanicPolicy() {}

  public static boolean shouldFleeDanger(
      final DogCommand command,
      final boolean onFire,
      final DogTraits traits,
      final double healthFraction) {
    if (onFire) {
      return true;
    }
    if (traits.holdsGround()) {
      return false;
    }
    return !holdsGroundInCombat(command) || healthFraction <= traits.fleeHealthFraction();
  }

  public static boolean joinsOwnerFight(final DogCommand command, final DogTraits traits) {
    return holdsGroundInCombat(command) || traits.joinsOwnerFights();
  }

  private static boolean holdsGroundInCombat(final DogCommand command) {
    return command == DogCommand.GUARD || command == DogCommand.HUNT;
  }
}
