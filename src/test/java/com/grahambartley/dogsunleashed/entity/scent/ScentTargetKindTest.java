package com.grahambartley.dogsunleashed.entity.scent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ScentTargetKindTest {

  static Stream<Arguments> declarations() {
    return Stream.of(
        Arguments.of(true, false, false, false, ScentTargetKind.ENTITY),
        Arguments.of(false, true, false, false, ScentTargetKind.BLOCK),
        Arguments.of(false, false, true, false, ScentTargetKind.SUSPICIOUS_BLOCK),
        Arguments.of(false, false, false, true, ScentTargetKind.LANDMARK),
        Arguments.of(false, false, false, false, null),
        Arguments.of(true, true, false, false, null),
        Arguments.of(true, false, true, false, null),
        Arguments.of(false, true, true, false, null),
        Arguments.of(true, false, false, true, null),
        Arguments.of(false, false, true, true, null),
        Arguments.of(true, true, true, true, null));
  }

  @ParameterizedTest(name = "entities={0} blocks={1} suspicious={2} structures={3} -> {4}")
  @MethodSource("declarations")
  @DisplayName("a rule names exactly one kind of target or it is invalid")
  void exactlyOneTarget(
      final boolean entities,
      final boolean blocks,
      final boolean suspicious,
      final boolean structures,
      final ScentTargetKind expected) {
    assertEquals(expected, ScentTargetKind.of(entities, blocks, suspicious, structures));
  }
}
