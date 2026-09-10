package com.grahambartley.dogsunleashed.entity;

import org.jetbrains.annotations.Nullable;

public enum DogWheelAction {
  FOLLOW(0, DogCommand.FOLLOW, null),
  HEEL(1, DogCommand.HEEL, null),
  STAY(2, DogCommand.STAY, null),
  SIT(3, DogCommand.SIT, null),
  HUNT(4, DogCommand.HUNT, null),
  GUARD(5, DogCommand.GUARD, null),
  FREE_ROAM(6, DogCommand.FREE_ROAM, null),
  GO_TO_BED(7, null, "command.dogs-unleashed.go_to_bed"),
  EQUIPMENT(8, null, "command.dogs-unleashed.equipment");

  private final int id;
  private final DogCommand command;
  private final String oneShotTranslationKey;

  DogWheelAction(final int id, final DogCommand command, final String oneShotTranslationKey) {
    this.id = id;
    this.command = command;
    this.oneShotTranslationKey = oneShotTranslationKey;
  }

  public int id() {
    return this.id;
  }

  public @Nullable DogCommand command() {
    return this.command;
  }

  public boolean isOneShot() {
    return this.command == null;
  }

  public String translationKey() {
    return this.command != null ? this.command.translationKey() : this.oneShotTranslationKey;
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
