package com.grahambartley.dogsunleashed.pet;

import org.jetbrains.annotations.Nullable;

public enum PetAwayStatus {
  HOME(null),
  EXPLORING("screen.dogs-unleashed.pet_manager.exploring"),
  DELIVERING("screen.dogs-unleashed.pet_manager.delivering"),
  WAITING("screen.dogs-unleashed.pet_manager.waiting"),
  RETURNING("screen.dogs-unleashed.pet_manager.returning");

  private final @Nullable String translationKey;

  PetAwayStatus(final @Nullable String translationKey) {
    this.translationKey = translationKey;
  }

  public @Nullable String translationKey() {
    return this.translationKey;
  }

  public boolean isAway() {
    return this != HOME;
  }

  public static PetAwayStatus byId(final int id) {
    final PetAwayStatus[] values = values();
    return id >= 0 && id < values.length ? values[id] : HOME;
  }
}
