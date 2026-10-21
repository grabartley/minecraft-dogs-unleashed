package com.grahambartley.dogsunleashed.pet;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public enum PetAwayStatus {
  HOME(null, null),
  EXPLORING("screen.dogs-unleashed.pet_manager.exploring", PetAwayStatus.EXPLORING_KEY),
  DELIVERING("screen.dogs-unleashed.pet_manager.delivering", PetAwayStatus.DELIVERING_KEY),
  WAITING("screen.dogs-unleashed.pet_manager.waiting", PetAwayStatus.DELIVERING_KEY),
  RETURNING("screen.dogs-unleashed.pet_manager.returning", PetAwayStatus.DELIVERING_KEY);

  public static final String EXPLORING_KEY = "message.dogs-unleashed.expedition.away";
  public static final String DELIVERING_KEY = "message.dogs-unleashed.delivery.away";

  private final @Nullable String translationKey;
  private final @Nullable String busyKey;

  PetAwayStatus(final @Nullable String translationKey, final @Nullable String busyKey) {
    this.translationKey = translationKey;
    this.busyKey = busyKey;
  }

  public Optional<String> busyKey() {
    return Optional.ofNullable(this.busyKey);
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
