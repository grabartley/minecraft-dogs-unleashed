package com.grahambartley.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BreedingOwnerResolverTest {

  @Test
  @DisplayName("Primary owner UUID is used when present")
  void usesPrimaryOwnerWhenPresent() {
    UUID primary = UUID.randomUUID();
    UUID secondary = UUID.randomUUID();

    UUID resolved = BreedingOwnerResolver.resolveInheritedOwnerUuid(primary, secondary);

    assertEquals(primary, resolved);
  }

  @Test
  @DisplayName("Secondary owner UUID is used when primary is missing")
  void fallsBackToSecondaryOwnerWhenPrimaryMissing() {
    UUID secondary = UUID.randomUUID();

    UUID resolved = BreedingOwnerResolver.resolveInheritedOwnerUuid(null, secondary);

    assertEquals(secondary, resolved);
  }

  @Test
  @DisplayName("No owner UUID is returned when both parents have no owner")
  void returnsNullWhenNeitherParentHasOwner() {
    UUID resolved = BreedingOwnerResolver.resolveInheritedOwnerUuid(null, null);

    assertNull(resolved);
  }
}
