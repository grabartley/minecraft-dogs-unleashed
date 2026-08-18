package com.grahambartley.dogsunleashed.pet;

import java.util.Locale;
import org.jetbrains.annotations.Nullable;

/**
 * Where a pet sits in the death, resurrection, and curing cycle. {@link #DECEASED} pets can be
 * raised at their grave, {@link #UNDEAD} pets can be cured back to {@link #LIVING}, and a pet that
 * dies while undead becomes {@link #LOST} for good.
 */
public enum PetLifeState {
  LIVING,
  UNDEAD,
  DECEASED,
  LOST;

  public String serializedName() {
    return this.name();
  }

  /** True while the pet still has a dog in the world, undead included. */
  public boolean isAlive() {
    return this == LIVING || this == UNDEAD;
  }

  public boolean isResurrectable() {
    return this == DECEASED;
  }

  public boolean isCurable() {
    return this == UNDEAD;
  }

  /** Records written before this enum existed carry only a boolean {@code Alive} flag. */
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
