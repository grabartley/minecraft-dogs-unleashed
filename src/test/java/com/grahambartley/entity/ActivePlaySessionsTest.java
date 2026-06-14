package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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

  @Test
  @DisplayName("takeover with no prior session installs the new dog and returns null")
  void takeoverFromEmptyMapInstallsNewDog() {
    final UUID playerUuid = UUID.randomUUID();
    final UUID newDogUuid = UUID.randomUUID();
    final Map<UUID, UUID> activePlaySessions = new HashMap<>();

    UUID prior = ActivePlaySessions.takeover(activePlaySessions, playerUuid, newDogUuid);

    assertNull(prior);
    assertEquals(newDogUuid, activePlaySessions.get(playerUuid));
  }

  @Test
  @DisplayName("takeover replaces the prior dog and returns its uuid for cleanup")
  void takeoverReplacesPriorDog() {
    final UUID playerUuid = UUID.randomUUID();
    final UUID priorDogUuid = UUID.randomUUID();
    final UUID newDogUuid = UUID.randomUUID();
    final Map<UUID, UUID> activePlaySessions = new HashMap<>();
    activePlaySessions.put(playerUuid, priorDogUuid);

    UUID returnedPrior = ActivePlaySessions.takeover(activePlaySessions, playerUuid, newDogUuid);

    assertSame(priorDogUuid, returnedPrior);
    assertEquals(newDogUuid, activePlaySessions.get(playerUuid));
  }

  @Test
  @DisplayName("takeover by the same dog is idempotent and reports no prior to clean up")
  void takeoverBySameDogReturnsNull() {
    final UUID playerUuid = UUID.randomUUID();
    final UUID dogUuid = UUID.randomUUID();
    final Map<UUID, UUID> activePlaySessions = new HashMap<>();
    activePlaySessions.put(playerUuid, dogUuid);

    UUID prior = ActivePlaySessions.takeover(activePlaySessions, playerUuid, dogUuid);

    assertNull(prior);
    assertEquals(dogUuid, activePlaySessions.get(playerUuid));
  }
}
