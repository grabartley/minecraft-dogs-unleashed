package com.grahambartley.dogsunleashed.entity;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public enum DogWheelAction {
  FOLLOW(0, DogCommand.FOLLOW, null, "follow"),
  HEEL(1, DogCommand.HEEL, null, "heel"),
  STAY(2, DogCommand.STAY, null, "stay"),
  SIT(3, DogCommand.SIT, null, "sit"),
  HUNT(4, DogCommand.HUNT, null, "hunt"),
  GUARD(5, DogCommand.GUARD, null, "guard"),
  FREE_ROAM(6, DogCommand.FREE_ROAM, null, "free_roam"),
  GO_TO_BED(7, null, "command.dogs-unleashed.go_to_bed", "go_to_bed"),
  EQUIPMENT(8, null, "command.dogs-unleashed.equipment", "equipment"),
  RIDE(9, null, "command.dogs-unleashed.ride", "ride");

  private final int id;
  private final DogCommand command;
  private final String oneShotTranslationKey;
  private final Identifier icon;

  DogWheelAction(
      final int id,
      final DogCommand command,
      final String oneShotTranslationKey,
      final String iconName) {
    this.id = id;
    this.command = command;
    this.oneShotTranslationKey = oneShotTranslationKey;
    this.icon = DogWheelIcons.sprite(iconName);
  }

  public int id() {
    return this.id;
  }

  public @Nullable DogCommand command() {
    return this.command;
  }

  public Identifier icon() {
    return this.icon;
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
