package com.grahambartley.dogsunleashed.entity.genome;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogGeneInheritanceTest {

  private static final int DOMINANT = 0;
  private static final int OTHER_DOMINANT = 1;
  private static final int RECESSIVE = 2;
  private static final GeneDominance DOMINANCE = new GeneDominance(3, Set.of(RECESSIVE), DOMINANT);
  private static final AllelePair CARRIER = new AllelePair(DOMINANT, RECESSIVE);
  private static final int NO_MUTATION = 1;
  private static final int MUTATION = 0;
  private static final int LITTERS = 200_000;

  @ParameterizedTest(name = "picks {0} from A and {1} from B")
  @CsvSource({"0, 0, 0, 1", "0, 1, 0, 3", "1, 0, 2, 1", "1, 1, 2, 3"})
  @DisplayName("a pup takes one allele from each parent")
  void pupTakesOneAllelePerParent(
      final int pickA, final int pickB, final int expectedFirst, final int expectedSecond) {
    final AllelePair pup =
        DogGeneInheritance.inherit(
            new AllelePair(0, 2),
            new AllelePair(1, 3),
            new GeneDominance(4, Set.of(), 0),
            scripted(NO_MUTATION, pickA, NO_MUTATION, pickB));
    assertEquals(new AllelePair(expectedFirst, expectedSecond), pup);
  }

  @Test
  @DisplayName("a mutation replaces the passed-on allele with a random one")
  void mutationRollsARandomAllele() {
    final AllelePair pup =
        DogGeneInheritance.inherit(
            CARRIER, CARRIER, DOMINANCE, scripted(MUTATION, OTHER_DOMINANT, NO_MUTATION, 1));
    assertEquals(new AllelePair(OTHER_DOMINANT, RECESSIVE), pup);
  }

  @Test
  @DisplayName("mutation draws one in thirty-two")
  void mutationIsOneInThirtyTwo() {
    final List<Integer> bounds = new java.util.ArrayList<>();
    DogGeneInheritance.inherit(
        CARRIER,
        CARRIER,
        DOMINANCE,
        bound -> {
          bounds.add(bound);
          return 1;
        });
    assertEquals(List.of(32, 2, 32, 2), bounds);
  }

  @Test
  @DisplayName("a parent without the gene passes on the wild type")
  void missingParentPassesTheWildType() {
    final AllelePair pup =
        DogGeneInheritance.inherit(null, CARRIER, DOMINANCE, scripted(NO_MUTATION, NO_MUTATION, 1));
    assertEquals(new AllelePair(DOMINANT, RECESSIVE), pup);
  }

  @Test
  @DisplayName("a wild dog showing a recessive allele carries two copies of it")
  void wildRecessiveIsHomozygous() {
    assertEquals(
        AllelePair.matching(RECESSIVE),
        DogGeneInheritance.wild(RECESSIVE, DOMINANCE, () -> OTHER_DOMINANT));
  }

  @Test
  @DisplayName("a wild dog showing a dominant allele keeps it first and rolls what it carries")
  void wildDominantKeepsShownFirst() {
    final AllelePair pair = DogGeneInheritance.wild(OTHER_DOMINANT, DOMINANCE, () -> DOMINANT);
    assertEquals(new AllelePair(OTHER_DOMINANT, DOMINANT), pair);
    assertEquals(OTHER_DOMINANT, DOMINANCE.express(pair));
  }

  @Test
  @DisplayName("two carriers have about one pup in four showing the recessive allele")
  void twoCarriersShowRecessiveInAboutOneInFour() {
    final Random random = new Random(478L);
    int recessive = 0;
    int carriers = 0;
    for (int litter = 0; litter < LITTERS; litter++) {
      final AllelePair pup =
          DogGeneInheritance.inherit(CARRIER, CARRIER, DOMINANCE, random::nextInt);
      recessive += DOMINANCE.express(pup) == RECESSIVE ? 1 : 0;
      carriers += DOMINANCE.carried(pup).contains(RECESSIVE) ? 1 : 0;
    }
    assertNear(0.25, recessive, "recessive pups");
    assertNear(0.5, carriers, "carrier pups");
  }

  private static void assertNear(final double expected, final int count, final String what) {
    final double share = (double) count / LITTERS;
    assertTrue(Math.abs(share - expected) < 0.02, what + " share was " + share);
  }

  private static IntUnaryOperator scripted(final int... draws) {
    final Deque<Integer> queue = new ArrayDeque<>();
    for (final int draw : draws) {
      queue.add(draw);
    }
    return bound -> queue.removeFirst();
  }
}
