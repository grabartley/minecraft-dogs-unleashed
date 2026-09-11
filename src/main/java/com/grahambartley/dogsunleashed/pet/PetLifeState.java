package com.grahambartley.dogsunleashed.pet;

import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public enum PetLifeState {
  LIVING,
  UNDEAD,
  DECEASED,
  LOST;

  public String serializedName() {
    return this.name();
  }

  public boolean isAlive() {
    return this == LIVING || this == UNDEAD;
  }

  public boolean isResurrectable() {
    return this == DECEASED;
  }

  public boolean isCurable() {
    return this == UNDEAD;
  }

  public static PetLifeState fromLegacyAliveFlag(final boolean alive) {
    return alive ? LIVING : DECEASED;
  }

  public static PetLifeState fromSerializedName(final @Nullable String serializedName) {
    if (serializedName == null || serializedName.isEmpty()) {
      return LIVING;
    }
    for (final PetLifeState state : values()) {
      if (state.name().equals(serializedName.toUpperCase(Locale.ROOT))) {
        return state;
      }
    }
    return LIVING;
  }
}
