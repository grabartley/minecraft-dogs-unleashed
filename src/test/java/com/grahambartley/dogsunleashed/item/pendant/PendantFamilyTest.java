package com.grahambartley.dogsunleashed.item.pendant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PendantFamilyTest {

  static Stream<Arguments> facing() {
    return Stream.of(
        Arguments.of(PendantFamily.WARDING, true),
        Arguments.of(PendantFamily.VITALITY, true),
        Arguments.of(PendantFamily.GUARDIAN, false),
        Arguments.of(PendantFamily.LOYALTY, false));
  }

  @ParameterizedTest(name = "{0} owner facing = {1}")
  @MethodSource("facing")
  void ownerFacingFamiliesAffectTheOwner(final PendantFamily family, final boolean ownerFacing) {
    assertEquals(ownerFacing, family.ownerFacing());
  }
}
