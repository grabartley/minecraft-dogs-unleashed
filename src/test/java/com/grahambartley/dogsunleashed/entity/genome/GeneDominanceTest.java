package com.grahambartley.dogsunleashed.entity.genome;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class GeneDominanceTest {

  private static final GeneDominance DOMINANCE = new GeneDominance(4, Set.of(2, 3), 0);

  @ParameterizedTest(name = "{0} and {1} show {2}")
  @CsvSource({
    "0, 0, 0", "1, 1, 1", "0, 1, 0", "1, 0, 1", "2, 1, 1", "1, 2, 1", "2, 2, 2", "3, 3, 3",
    "2, 3, 0",
  })
  @DisplayName("a dominant allele shows, a recessive one only when both match, else the wild type")
  void expressFollowsDominance(final int first, final int second, final int shown) {
    assertEquals(shown, DOMINANCE.express(new AllelePair(first, second)));
  }

  static Stream<Arguments> carriedAlleles() {
    return Stream.of(
        Arguments.of(new AllelePair(0, 0), List.of()),
        Arguments.of(new AllelePair(0, 2), List.of(2)),
        Arguments.of(new AllelePair(2, 1), List.of(2)),
        Arguments.of(new AllelePair(0, 1), List.of(1)),
        Arguments.of(new AllelePair(2, 3), List.of(2, 3)));
  }

  @ParameterizedTest(name = "{0} carries {1}")
  @MethodSource("carriedAlleles")
  @DisplayName("carried alleles are the ones that do not show")
  void carriedAllelesAreTheHiddenOnes(final AllelePair pair, final List<Integer> carried) {
    assertEquals(carried, DOMINANCE.carried(pair));
  }

  @ParameterizedTest(name = "allele {0} valid is {1}")
  @CsvSource({"-1, false", "0, true", "3, true", "4, false"})
  @DisplayName("only alleles inside the gene's range are valid")
  void validityFollowsAlleleCount(final int allele, final boolean valid) {
    assertEquals(valid, DOMINANCE.isValid(allele));
  }

  @Test
  @DisplayName("the wild type must be a dominant allele inside the range")
  void wildTypeMustBeDominantAndInRange() {
    assertThrows(IllegalArgumentException.class, () -> new GeneDominance(4, Set.of(0), 0));
    assertThrows(IllegalArgumentException.class, () -> new GeneDominance(4, Set.of(), 4));
    assertThrows(IllegalArgumentException.class, () -> new GeneDominance(0, Set.of(), 0));
  }
}
