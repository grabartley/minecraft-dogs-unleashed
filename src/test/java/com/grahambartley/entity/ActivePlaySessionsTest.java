package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ActivePlaySessionsTest {

  @Test
  @DisplayName("clear removes the active dog's own session")
  void clearRemovesMatchingActiveSession() {
    final UUID playerUuid = UUID.randomUUID();
    final UUID dogUuid = UUID.randomUUID();
    final Map<UUID, UUID> activePlaySessions = new HashMap<>();
    activePlaySessions.put(playerUuid, dogUuid);

    ActivePlaySessions.clear(activePlaySessions, true, playerUuid, dogUuid);

    assertFalse(activePlaySessions.containsKey(playerUuid));
  }

  @Test
  @DisplayName("clear ignores dogs that are not in play mode")
  void clearIgnoresInactiveDogs() {
    final UUID playerUuid = UUID.randomUUID();
    final UUID dogUuid = UUID.randomUUID();
    final Map<UUID, UUID> activePlaySessions = new HashMap<>();
    activePlaySessions.put(playerUuid, dogUuid);

    ActivePlaySessions.clear(activePlaySessions, false, playerUuid, dogUuid);

    assertTrue(activePlaySessions.containsKey(playerUuid));
  }

  @Test
  @DisplayName("clear preserves another dog's active session")
  void clearPreservesDifferentDogSession() {
    final UUID playerUuid = UUID.randomUUID();
    final UUID activeDogUuid = UUID.randomUUID();
    final UUID unloadedDogUuid = UUID.randomUUID();
    final Map<UUID, UUID> activePlaySessions = new HashMap<>();
    activePlaySessions.put(playerUuid, activeDogUuid);

    ActivePlaySessions.clear(activePlaySessions, true, playerUuid, unloadedDogUuid);

    assertTrue(activePlaySessions.containsKey(playerUuid));
    assertEquals(activeDogUuid, activePlaySessions.get(playerUuid));
  }

  @Test
  @DisplayName("play mode should end for non-killed removals")
  void shouldEndOnNonKilledRemoval() {
    assertTrue(ActivePlaySessions.shouldEndOnRemoval(Entity.RemovalReason.UNLOADED_TO_CHUNK));
    assertTrue(ActivePlaySessions.shouldEndOnRemoval(Entity.RemovalReason.UNLOADED_WITH_PLAYER));
    assertTrue(ActivePlaySessions.shouldEndOnRemoval(Entity.RemovalReason.CHANGED_DIMENSION));
    assertTrue(ActivePlaySessions.shouldEndOnRemoval(Entity.RemovalReason.DISCARDED));
  }

  @Test
  @DisplayName("play mode cleanup should be skipped for killed removals")
  void shouldNotEndOnKilledRemoval() {
    assertFalse(ActivePlaySessions.shouldEndOnRemoval(Entity.RemovalReason.KILLED));
  }
}
