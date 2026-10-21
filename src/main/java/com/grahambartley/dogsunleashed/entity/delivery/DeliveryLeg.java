package com.grahambartley.dogsunleashed.entity.delivery;

import com.grahambartley.dogsunleashed.pet.PetAwayStatus;
import org.jetbrains.annotations.Nullable;

public enum DeliveryLeg {
  TO_RECIPIENT("to_recipient", true, PetAwayStatus.DELIVERING),
  WAITING("waiting", false, PetAwayStatus.WAITING),
  TO_OWNER("to_owner", true, PetAwayStatus.RETURNING);

  private final String id;
  private final boolean offWorld;
  private final PetAwayStatus awayStatus;

  DeliveryLeg(final String id, final boolean offWorld, final PetAwayStatus awayStatus) {
    this.id = id;
    this.offWorld = offWorld;
    this.awayStatus = awayStatus;
  }

  public PetAwayStatus awayStatus() {
    return this.awayStatus;
  }

  public String id() {
    return this.id;
  }

  public boolean offWorld() {
    return this.offWorld;
  }

  public static @Nullable DeliveryLeg fromId(final String id) {
    for (final DeliveryLeg leg : values()) {
      if (leg.id.equals(id)) {
        return leg;
      }
    }
    return null;
  }
}
