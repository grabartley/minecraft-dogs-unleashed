package com.grahambartley.dogsunleashed.entity.delivery;

import org.jetbrains.annotations.Nullable;

public enum DeliveryLeg {
  TO_RECIPIENT("to_recipient", true),
  WAITING("waiting", false),
  TO_OWNER("to_owner", true);

  private final String id;
  private final boolean offWorld;

  DeliveryLeg(final String id, final boolean offWorld) {
    this.id = id;
    this.offWorld = offWorld;
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
