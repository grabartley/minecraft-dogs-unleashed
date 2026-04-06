package com.grahambartley.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PetAliveFilterTest {

  @Test
  @DisplayName("Alive filter should round-trip serialized values")
  void testSerializedValues() {
    for (final PetAliveFilter filter : PetAliveFilter.values()) {
      assertEquals(filter, PetAliveFilter.fromSerializedName(filter.serializedName()));
    }
  }

  @Test
  @DisplayName("Missing or unknown alive filters should default to ALIVE")
  void testFallbackBehavior() {
    assertEquals(PetAliveFilter.ALIVE, PetAliveFilter.fromSerializedName(null));
    assertEquals(PetAliveFilter.ALIVE, PetAliveFilter.fromSerializedName(""));
    assertEquals(PetAliveFilter.ALIVE, PetAliveFilter.fromSerializedName("weird"));
    assertTrue(PetAliveFilter.ALL.appliesTo(true));
    assertTrue(PetAliveFilter.ALL.appliesTo(false));
  }
}
