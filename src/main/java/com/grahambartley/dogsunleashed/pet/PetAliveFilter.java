package com.grahambartley.dogsunleashed.pet;

import org.jetbrains.annotations.Nullable;

public enum PetAliveFilter {
  ALL("ALL", null),
  ALIVE("ALIVE", true),
  DECEASED("DECEASED", false);

  private final String serializedName;
  private final Boolean aliveValue;

  PetAliveFilter(final String serializedName, final @Nullable Boolean aliveValue) {
    this.serializedName = serializedName;
    this.aliveValue = aliveValue;
  }

  public String serializedName() {
    return this.serializedName;
  }

  public @Nullable Boolean aliveValue() {
    return this.aliveValue;
  }

  public boolean appliesTo(final boolean alive) {
    return this.aliveValue == null || this.aliveValue == alive;
  }

  public static PetAliveFilter fromSerializedName(final @Nullable String serializedName) {
    if (serializedName == null || serializedName.isEmpty()) {
      return ALIVE;
    }

    for (final PetAliveFilter filter : values()) {
      if (filter.serializedName.equals(serializedName)) {
        return filter;
      }
    }

    return ALIVE;
  }
}
