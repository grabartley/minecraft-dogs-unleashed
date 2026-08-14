package com.grahambartley.dogsunleashed.entity;

public enum DogCommand {
  FOLLOW(0, "follow"),
  HEEL(1, "heel"),
  STAY(2, "stay"),
  SIT(3, "sit"),
  HUNT(4, "hunt"),
  GUARD(5, "guard"),
  FREE_ROAM(6, "free_roam");

  private final int id;
  private final String serializedName;

  DogCommand(final int id, final String serializedName) {
    this.id = id;
    this.serializedName = serializedName;
  }

  public int id() {
    return this.id;
  }

  public String serializedName() {
    return this.serializedName;
  }

  public String translationKey() {
    return "command.dogs-unleashed." + this.serializedName;
  }

  public String messageKey() {
    return "message.dogs-unleashed.command_" + this.serializedName;
  }

  public boolean isAnchored() {
    return this == STAY || this == GUARD;
  }

  public boolean followsOwnerOnRelocation() {
    return this == FOLLOW || this == HEEL || this == HUNT || this == FREE_ROAM;
  }

  public static DogCommand fromId(final int id) {
    for (final DogCommand command : values()) {
      if (command.id == id) {
        return command;
      }
    }
    return FOLLOW;
  }
}
