package com.grahambartley.dogsunleashed.pet;

import java.util.Set;
import org.jetbrains.annotations.Nullable;

public enum PetAliveFilter {
  ALL("ALL", null),
  ALIVE("ALIVE", Set.of(PetLifeState.LIVING)),
  UNDEAD("UNDEAD", Set.of(PetLifeState.UNDEAD)),
  DECEASED("DECEASED", Set.of(PetLifeState.DECEASED, PetLifeState.LOST));

  private final String serializedName;
  private final @Nullable Set<PetLifeState> matchedStates;

  PetAliveFilter(final String serializedName, final @Nullable Set<PetLifeState> matchedStates) {
    this.serializedName = serializedName;
    this.matchedStates = matchedStates;
  }

  public String serializedName() {
    return this.serializedName;
  }

  public boolean appliesTo(final PetLifeState lifeState) {
    return this.matchedStates == null || this.matchedStates.contains(lifeState);
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
