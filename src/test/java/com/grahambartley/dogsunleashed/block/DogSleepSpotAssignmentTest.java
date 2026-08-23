package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DogSleepSpotAssignmentTest {

  @BeforeEach
  void clearGlobalState() {
    DogSleepSpotAssignment.clearPendingAssignments();
  }

  @Test
  @DisplayName("a pending assignment maps the player to the dog and survives until it is consumed")
  void pendingAssignmentIsReadableUntilConsumed() {
    final UUID playerUuid = UUID.randomUUID();
    final UUID dogUuid = UUID.randomUUID();

    DogSleepSpotAssignment.setPendingAssignment(playerUuid, dogUuid);

    assertEquals(dogUuid, DogSleepSpotAssignment.consumePendingAssignment(playerUuid));
  }

  @Test
  @DisplayName("consuming a pending assignment spends it, so a second click assigns nothing")
  void pendingAssignmentIsSingleUse() {
    final UUID playerUuid = UUID.randomUUID();
    DogSleepSpotAssignment.setPendingAssignment(playerUuid, UUID.randomUUID());

    DogSleepSpotAssignment.consumePendingAssignment(playerUuid);

    assertNull(DogSleepSpotAssignment.consumePendingAssignment(playerUuid));
  }

  @Test
  @DisplayName("one player's pending assignment is not visible to another player")
  void pendingAssignmentsArePerPlayer() {
    final UUID playerUuid = UUID.randomUUID();
    DogSleepSpotAssignment.setPendingAssignment(playerUuid, UUID.randomUUID());

    assertNull(DogSleepSpotAssignment.consumePendingAssignment(UUID.randomUUID()));
    assertNotNull(
        DogSleepSpotAssignment.consumePendingAssignment(playerUuid),
        "the owning player's assignment should be untouched");
  }

  @Test
  @DisplayName("clearing drops every pending assignment, so state cannot leak between worlds")
  void clearingDropsEveryPendingAssignment() {
    final UUID firstPlayer = UUID.randomUUID();
    final UUID secondPlayer = UUID.randomUUID();
    DogSleepSpotAssignment.setPendingAssignment(firstPlayer, UUID.randomUUID());
    DogSleepSpotAssignment.setPendingAssignment(secondPlayer, UUID.randomUUID());

    DogSleepSpotAssignment.clearPendingAssignments();

    assertNull(DogSleepSpotAssignment.consumePendingAssignment(firstPlayer));
    assertNull(DogSleepSpotAssignment.consumePendingAssignment(secondPlayer));
  }
}
