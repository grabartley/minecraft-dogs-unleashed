package com.grahambartley.dogsunleashed.entity.scent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class ScentPhaseTest {

  @ParameterizedTest
  @EnumSource(ScentPhase.class)
  @DisplayName("every phase survives the trip through its synced id")
  void idRoundTrips(final ScentPhase phase) {
    assertEquals(phase, ScentPhase.fromId(phase.id()));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 4, 99})
  @DisplayName("an unknown id reads as not tracking")
  void unknownIdIsNone(final int id) {
    assertEquals(ScentPhase.NONE, ScentPhase.fromId(id));
  }
}
