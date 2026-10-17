package com.grahambartley.dogsunleashed.pendant;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class OwnerDamageClock {

  public static final long SUPPRESSION_TICKS = 200L;

  private final Map<UUID, Long> lastDamagedTick = new HashMap<>();

  public void recordDamage(final UUID playerId, final long tick) {
    this.lastDamagedTick.put(playerId, tick);
  }

  public boolean isRecovered(final UUID playerId, final long tick) {
    final Long damagedAt = this.lastDamagedTick.get(playerId);
    return damagedAt == null || tick - damagedAt >= SUPPRESSION_TICKS;
  }

  public void forget(final UUID playerId) {
    this.lastDamagedTick.remove(playerId);
  }

  public void clear() {
    this.lastDamagedTick.clear();
  }
}
