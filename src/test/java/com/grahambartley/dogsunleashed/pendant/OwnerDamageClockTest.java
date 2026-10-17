package com.grahambartley.dogsunleashed.pendant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OwnerDamageClockTest {

  private static final UUID PLAYER = UUID.fromString("0f8fad5b-d9cb-469f-a165-70867728950e");

  @Test
  @DisplayName("a player never hurt counts as recovered")
  void neverHurtIsRecovered() {
    assertTrue(new OwnerDamageClock().isRecovered(PLAYER, 0L));
  }

  @Test
  @DisplayName("a hurt player recovers only after ten seconds")
  void recoversAfterTenSeconds() {
    final OwnerDamageClock clock = new OwnerDamageClock();
    clock.recordDamage(PLAYER, 1000L);

    assertFalse(clock.isRecovered(PLAYER, 1199L));
    assertTrue(clock.isRecovered(PLAYER, 1200L));
  }

  @Test
  @DisplayName("a later hit restarts the wait")
  void laterHitRestartsTheWait() {
    final OwnerDamageClock clock = new OwnerDamageClock();
    clock.recordDamage(PLAYER, 1000L);
    clock.recordDamage(PLAYER, 1150L);

    assertFalse(clock.isRecovered(PLAYER, 1300L));
  }

  @Test
  @DisplayName("forgetting a player, as on disconnect, clears their wait")
  void forgetClearsTheWait() {
    final OwnerDamageClock clock = new OwnerDamageClock();
    clock.recordDamage(PLAYER, 1000L);

    clock.forget(PLAYER);

    assertTrue(clock.isRecovered(PLAYER, 1001L));
  }

  @Test
  @DisplayName("clearing, as on server stop, forgets everyone")
  void clearForgetsEveryone() {
    final OwnerDamageClock clock = new OwnerDamageClock();
    clock.recordDamage(PLAYER, 1000L);

    clock.clear();

    assertTrue(clock.isRecovered(PLAYER, 1001L));
  }
}
