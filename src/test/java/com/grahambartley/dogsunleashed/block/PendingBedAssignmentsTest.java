package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PendingBedAssignmentsTest {

  @Test
  @DisplayName("clearAll releases every pending assignment, e.g. on server stop")
  void clearAllReleasesEveryPendingAssignment() {
    final Map<UUID, UUID> pending = new HashMap<>();
    pending.put(UUID.randomUUID(), UUID.randomUUID());
    pending.put(UUID.randomUUID(), UUID.randomUUID());
    assertEquals(2, pending.size());

    PendingBedAssignments.clearAll(pending);

    assertTrue(pending.isEmpty());
  }

  @Test
  @DisplayName("clearAll on an already-empty map is a harmless no-op")
  void clearAllOnEmptyMapIsNoOp() {
    final Map<UUID, UUID> pending = new HashMap<>();

    PendingBedAssignments.clearAll(pending);

    assertTrue(pending.isEmpty());
  }
}
