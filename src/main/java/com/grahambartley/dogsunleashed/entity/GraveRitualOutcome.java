package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.pet.PetLifeState;
import org.jetbrains.annotations.Nullable;

public enum GraveRitualOutcome {
  NOTHING,
  RAISE,
  FIZZLE;

  public static GraveRitualOutcome decide(
      final boolean hasTotem, final boolean graveLost, final @Nullable PetLifeState petState) {
    if (!hasTotem) {
      return NOTHING;
    }
    if (graveLost || petState == PetLifeState.LOST) {
      return FIZZLE;
    }
    return petState != null && petState.isResurrectable() ? RAISE : NOTHING;
  }
}
