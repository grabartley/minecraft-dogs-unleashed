package com.grahambartley.dogsunleashed.item.pendant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PendantTypeTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(PendantType.class)
  @DisplayName("only warding pendants name a mob family, only guardians name a damage family")
  void familyDecidesWhatItProtectsAgainst(final PendantType type) {
    if (type.family() == PendantFamily.WARDING) {
      assertNotNull(type.wardedMobs());
    } else {
      assertNull(type.wardedMobs());
    }
    if (type.family() == PendantFamily.GUARDIAN) {
      assertNotNull(type.guardedDamage());
    } else {
      assertNull(type.guardedDamage());
    }
  }

  @Test
  @DisplayName("every pendant has its own tint so they are told apart at a glance")
  void tintsAreUnique() {
    final Set<Integer> tints =
        Arrays.stream(PendantType.values()).map(PendantType::tint).collect(Collectors.toSet());

    assertEquals(PendantType.values().length, tints.size());
  }
}
