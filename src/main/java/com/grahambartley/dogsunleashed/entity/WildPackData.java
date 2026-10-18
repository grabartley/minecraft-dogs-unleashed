package com.grahambartley.dogsunleashed.entity;

import java.util.UUID;
import net.minecraft.entity.passive.PassiveEntity;

public final class WildPackData extends PassiveEntity.PassiveData {

  private final UUID packId;

  WildPackData(final UUID packId, final boolean babyAllowed) {
    super(babyAllowed);
    this.packId = packId;
  }

  public UUID packId() {
    return this.packId;
  }
}
