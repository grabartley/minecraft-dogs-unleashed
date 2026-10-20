package com.grahambartley.dogsunleashed.block;

import java.util.Locale;

public enum DogStatuePose {
  SIT,
  STAND,
  HOWL;

  public DogStatuePose next(final boolean howls) {
    return switch (this) {
      case SIT -> STAND;
      case STAND -> howls ? HOWL : SIT;
      case HOWL -> SIT;
    };
  }

  public DogStatuePose shownFor(final boolean howls) {
    return this == HOWL && !howls ? SIT : this;
  }

  public boolean sits() {
    return this != STAND;
  }

  public boolean howling() {
    return this == HOWL;
  }

  public String serializedName() {
    return this.name().toLowerCase(Locale.ROOT);
  }

  public static DogStatuePose fromSerializedName(final String name) {
    for (final DogStatuePose pose : values()) {
      if (pose.serializedName().equals(name)) {
        return pose;
      }
    }
    return SIT;
  }
}
