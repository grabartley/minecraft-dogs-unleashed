package com.grahambartley.dogsunleashed.entity.scent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SuspiciousScanRulesTest {

  private static final BlockPos ORIGIN = new BlockPos(0, 64, 0);

  static Stream<Arguments> chunks() {
    return Stream.of(
        Arguments.of("nothing", List.of(), 32, Optional.empty()),
        Arguments.of(
            "the nearer of two",
            List.of(new BlockPos(3, 64, 0), new BlockPos(2, 64, 0)),
            32,
            Optional.of(new BlockPos(2, 64, 0))),
        Arguments.of(
            "the first of a tie",
            List.of(new BlockPos(0, 64, 5), new BlockPos(5, 64, 0)),
            32,
            Optional.of(new BlockPos(0, 64, 5))),
        Arguments.of("one past range", List.of(new BlockPos(33, 64, 0)), 32, Optional.empty()),
        Arguments.of(
            "one at range",
            List.of(new BlockPos(32, 64, 0)),
            32,
            Optional.of(new BlockPos(32, 64, 0))),
        Arguments.of(
            "the nearer in range, skipping a far one",
            List.of(new BlockPos(40, 64, 0), new BlockPos(0, 40, 0)),
            32,
            Optional.of(new BlockPos(0, 40, 0))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("chunks")
  @DisplayName("a chunk scan picks the nearest brushable block within range")
  void picksNearestWithinRange(
      final String label,
      final List<BlockPos> candidates,
      final int range,
      final Optional<BlockPos> expected) {
    assertEquals(expected, SuspiciousScanRules.nearestWithin(ORIGIN, range, candidates));
  }

  static Stream<Arguments> comparisons() {
    return Stream.of(
        Arguments.of(new BlockPos(9, 64, 0), null, true),
        Arguments.of(new BlockPos(2, 64, 0), new BlockPos(3, 64, 0), true),
        Arguments.of(new BlockPos(3, 64, 0), new BlockPos(0, 64, 3), false),
        Arguments.of(new BlockPos(4, 64, 0), new BlockPos(3, 64, 0), false));
  }

  @ParameterizedTest(name = "{0} vs {1} -> {2}")
  @MethodSource("comparisons")
  @DisplayName("a find replaces the best so far only when strictly nearer")
  void closerOnlyWhenStrictlyNearer(
      final BlockPos candidate, final BlockPos current, final boolean expected) {
    assertEquals(expected, SuspiciousScanRules.isCloser(ORIGIN, candidate, current));
  }
}
