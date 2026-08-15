package com.grahambartley.dogsunleashed.entity;

import org.jetbrains.annotations.Nullable;

/**
 * The selectable sectors of the command wheel, in clockwise display order starting at 12 o'clock.
 * Most actions map to a persistent {@link DogCommand}; {@code GO_TO_BED} is a one-shot action that
 * leaves the active command unchanged.
 */
public enum DogWheelAction {
  FOLLOW(0, DogCommand.FOLLOW),
  HEEL(1, DogCommand.HEEL),
  STAY(2, DogCommand.STAY),
  SIT(3, DogCommand.SIT),
  HUNT(4, DogCommand.HUNT),
  GUARD(5, DogCommand.GUARD),
  FREE_ROAM(6, DogCommand.FREE_ROAM),
  GO_TO_BED(7, null);

  private final int id;
  private final DogCommand command;

  DogWheelAction(final int id, final DogCommand command) {
    this.id = id;
    this.command = command;
  }

  public int id() {
    return this.id;
  }

  public @Nullable DogCommand command() {
    return this.command;
  }

  public String translationKey() {
    return this.command != null
        ? this.command.translationKey()
        : "command.dogs-unleashed.go_to_bed";
  }

  public static @Nullable DogWheelAction fromId(final int id) {
    for (final DogWheelAction action : values()) {
      if (action.id == id) {
        return action;
      }
    }
    return null;
  }
}
