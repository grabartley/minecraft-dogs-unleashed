package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class ActivePlaySessionsTest {

  @Test
  @DisplayName("clear removes the active dog's own session")
  void clearRemovesActiveDogSession() {
    final UUID playerUuid = UUID.randomUUID();
    final UUID dogUuid = UUID.randomUUID();
    final Map<UUID, UUID> sessions = new HashMap<>();
    sessions.put(playerUuid, dogUuid);

    ActivePlaySessions.clear(sessions, true, playerUuid, dogUuid);

    assertFalse(sessions.containsKey(playerUuid));
  }

  @Test
  @DisplayName("clear ignores dogs that are not currently in play mode")
  void clearIgnoresInactiveDogs() {
    final UUID playerUuid = UUID.randomUUID();
    final UUID dogUuid = UUID.randomUUID();
    final Map<UUID, UUID> sessions = new HashMap<>();
    sessions.put(playerUuid, dogUuid);

    ActivePlaySessions.clear(sessions, false, playerUuid, dogUuid);

    assertTrue(sessions.containsKey(playerUuid));
    assertEquals(dogUuid, sessions.get(playerUuid));
  }

  @Test
  @DisplayName("clear preserves another dog's active session")
  void clearPreservesAnotherDogsSession() {
    final UUID playerUuid = UUID.randomUUID();
    final UUID activeDogUuid = UUID.randomUUID();
    final UUID unloadedDogUuid = UUID.randomUUID();
    final Map<UUID, UUID> sessions = new HashMap<>();
    sessions.put(playerUuid, activeDogUuid);

    ActivePlaySessions.clear(sessions, true, playerUuid, unloadedDogUuid);

    assertEquals(activeDogUuid, sessions.get(playerUuid));
  }

  @Test
  @DisplayName("clearAll empties every session, e.g. on server stop")
  void clearAllEmptiesEverySession() {
    final Map<UUID, UUID> sessions = new HashMap<>();
    sessions.put(UUID.randomUUID(), UUID.randomUUID());
    sessions.put(UUID.randomUUID(), UUID.randomUUID());
    assertEquals(2, sessions.size());

    ActivePlaySessions.clearAll(sessions);

    assertTrue(sessions.isEmpty());
  }

  @Test
  @DisplayName("clearAll on an already-empty map is a harmless no-op")
  void clearAllOnEmptyMapIsNoOp() {
    final Map<UUID, UUID> sessions = new HashMap<>();

    ActivePlaySessions.clearAll(sessions);

    assertTrue(sessions.isEmpty());
  }

  static Stream<Arguments> nonKillingRemovals() {
    return Stream.of(
        Arguments.of(Entity.RemovalReason.UNLOADED_TO_CHUNK),
        Arguments.of(Entity.RemovalReason.UNLOADED_WITH_PLAYER),
        Arguments.of(Entity.RemovalReason.CHANGED_DIMENSION),
        Arguments.of(Entity.RemovalReason.DISCARDED));
  }

  @ParameterizedTest(name = "{0} ends the play session")
  @MethodSource("nonKillingRemovals")
  @DisplayName("non-killing removals end the active play session")
  void nonKillingRemovalsEndPlaySession(final Entity.RemovalReason reason) {
    assertTrue(ActivePlaySessions.shouldEndOnRemoval(reason));
  }

  @ParameterizedTest(name = "{0} keeps the play session alive")
  @EnumSource(value = Entity.RemovalReason.class, names = "KILLED")
  @DisplayName("killing removals leave the play session intact so it survives a respawn")
  void killingRemovalsKeepPlaySessionAlive(final Entity.RemovalReason reason) {
    assertFalse(ActivePlaySessions.shouldEndOnRemoval(reason));
  }

  @Test
  @DisplayName("takeover with no prior session installs the new dog and returns null")
  void takeoverFromEmptyMapInstallsNewDog() {
    final UUID playerUuid = UUID.randomUUID();
    final UUID newDogUuid = UUID.randomUUID();
    final Map<UUID, UUID> sessions = new HashMap<>();

    final UUID prior = ActivePlaySessions.takeover(sessions, playerUuid, newDogUuid);

    assertNull(prior);
    assertEquals(newDogUuid, sessions.get(playerUuid));
  }

  @Test
  @DisplayName("takeover replaces the prior dog and returns its uuid for cleanup")
  void takeoverReplacesPriorDog() {
    final UUID playerUuid = UUID.randomUUID();
    final UUID priorDogUuid = UUID.randomUUID();
    final UUID newDogUuid = UUID.randomUUID();
    final Map<UUID, UUID> sessions = new HashMap<>();
    sessions.put(playerUuid, priorDogUuid);

    final UUID returnedPrior = ActivePlaySessions.takeover(sessions, playerUuid, newDogUuid);

    assertSame(priorDogUuid, returnedPrior);
    assertEquals(newDogUuid, sessions.get(playerUuid));
  }

  @Test
  @DisplayName("takeover by the same dog is idempotent and reports no prior to clean up")
  void takeoverBySameDogIsIdempotent() {
    final UUID playerUuid = UUID.randomUUID();
    final UUID dogUuid = UUID.randomUUID();
    final Map<UUID, UUID> sessions = new HashMap<>();
    sessions.put(playerUuid, dogUuid);

    final UUID prior = ActivePlaySessions.takeover(sessions, playerUuid, dogUuid);

    assertNull(prior);
    assertEquals(dogUuid, sessions.get(playerUuid));
  }
}
