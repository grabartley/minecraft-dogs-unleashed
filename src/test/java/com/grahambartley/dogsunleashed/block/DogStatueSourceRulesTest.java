package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.grahambartley.dogsunleashed.block.DogStatueSourceRules.Candidate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class DogStatueSourceRulesTest {

  static Stream<Arguments> picks() {
    return Stream.of(
        Arguments.of(List.of(), List.of(), Optional.empty()),
        Arguments.of(
            List.of(new Candidate<>("far dog", 49.0), new Candidate<>("near dog", 4.0)),
            List.of(new Candidate<>("grave", 1.0)),
            Optional.of("near dog")),
        Arguments.of(
            List.of(new Candidate<>("dog out of reach", 64.1)),
            List.of(new Candidate<>("grave", 9.0)),
            Optional.of("grave")),
        Arguments.of(
            List.of(),
            List.of(new Candidate<>("far grave", 50.0), new Candidate<>("near grave", 2.0)),
            Optional.of("near grave")),
        Arguments.of(
            List.of(new Candidate<>("dog at the edge", 64.0)),
            List.of(),
            Optional.of("dog at the edge")),
        Arguments.of(List.of(), List.of(new Candidate<>("far grave", 65.0)), Optional.empty()));
  }

  @ParameterizedTest(name = "[{index}] picks {2}")
  @MethodSource("picks")
  @DisplayName("the nearest dog in reach wins, then the nearest grave, and nothing past 8 blocks")
  void picksTheNearestDogThenGrave(
      final List<Candidate<String>> dogs,
      final List<Candidate<String>> graves,
      final Optional<String> expected) {
    assertEquals(expected, DogStatueSourceRules.pick(dogs, () -> graves));
  }

  @ParameterizedTest(name = "{0} squared blocks in reach is {1}")
  @CsvSource({"0, true", "63.9, true", "64, true", "64.01, false"})
  @DisplayName("reach is 8 blocks")
  void reachIsEightBlocks(final double distanceSquared, final boolean inReach) {
    assertEquals(inReach, DogStatueSourceRules.inReach(distanceSquared));
  }

  @Test
  @DisplayName("graves are not searched once a dog is in reach")
  void gravesAreSkippedWhenADogIsNear() {
    final AtomicInteger searches = new AtomicInteger();
    DogStatueSourceRules.pick(
        List.of(new Candidate<>("dog", 1.0)),
        () -> {
          searches.incrementAndGet();
          return List.<Candidate<String>>of();
        });
    assertFalse(searches.get() > 0, "the grave search should not run");
  }
}
