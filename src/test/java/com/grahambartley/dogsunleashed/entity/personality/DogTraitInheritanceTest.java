package com.grahambartley.dogsunleashed.entity.personality;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.genome.DogGeneInheritance;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import java.util.function.IntUnaryOperator;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class DogTraitInheritanceTest {

  private static IntUnaryOperator rolls(final int... values) {
    final Deque<Integer> queue = new ArrayDeque<>();
    for (final int value : values) {
      queue.add(value);
    }
    return bound -> {
      final int value = queue.removeFirst();
      if (value < 0 || value >= bound) {
        throw new IllegalStateException(value + " is outside 0.." + bound);
      }
      return value;
    };
  }

  static Stream<Arguments> wildRolls() {
    return Stream.of(
        Arguments.of(rolls(DogTrait.GREEDY.ordinal(), 1), List.of(DogTrait.GREEDY)),
        Arguments.of(
            rolls(DogTrait.BRAVE.ordinal(), 0, 0), List.of(DogTrait.BRAVE, DogTrait.GREEDY)),
        Arguments.of(
            rolls(DogTrait.LAZY.ordinal(), 0, 3), List.of(DogTrait.LAZY, DogTrait.CLINGY)));
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("wildRolls")
  @DisplayName("a wild dog rolls one trait, and sometimes a second that fits with it")
  void wildRoll(final IntUnaryOperator nextInt, final List<DogTrait> expected) {
    assertEquals(expected, DogTraitInheritance.wild(nextInt).traits());
  }

  @ParameterizedTest(name = "seed {0}")
  @ValueSource(longs = {1L, 7L, 42L, 1234L})
  @DisplayName("wild rolls never pair conflicting traits")
  void wildRollsStayCompatible(final long seed) {
    final Random random = new Random(seed);
    for (int i = 0; i < 500; i++) {
      final DogTraits traits = DogTraitInheritance.wild(random::nextInt);
      assertFalse(traits.isEmpty());
      if (traits.traits().size() == 2) {
        assertFalse(traits.traits().get(0).conflictsWith(traits.traits().get(1)));
      }
    }
  }

  static Stream<Arguments> inheritRolls() {
    final DogTraits braveGreedy = DogTraits.of(DogTrait.BRAVE, DogTrait.GREEDY);
    final DogTraits clingy = DogTraits.of(DogTrait.CLINGY);
    final DogTraits timid = DogTraits.of(DogTrait.TIMID);
    return Stream.of(
        Arguments.of(
            braveGreedy, clingy, rolls(1, 1, 1, 0), List.of(DogTrait.GREEDY, DogTrait.CLINGY)),
        Arguments.of(
            braveGreedy, clingy, rolls(1, 0, 1, 0), List.of(DogTrait.BRAVE, DogTrait.CLINGY)),
        Arguments.of(braveGreedy, timid, rolls(1, 0, 1, 0), List.of(DogTrait.BRAVE)),
        Arguments.of(clingy, clingy, rolls(1, 0, 1, 0), List.of(DogTrait.CLINGY)),
        Arguments.of(
            clingy,
            clingy,
            rolls(0, DogTrait.PLAYFUL.ordinal(), 1, 0),
            List.of(DogTrait.PLAYFUL, DogTrait.CLINGY)),
        Arguments.of(
            DogTraits.NONE,
            clingy,
            rolls(DogTrait.LAZY.ordinal(), 1, 0),
            List.of(DogTrait.LAZY, DogTrait.CLINGY)));
  }

  @ParameterizedTest(name = "{0} x {1} -> {3}")
  @MethodSource("inheritRolls")
  @DisplayName("a pup takes one trait from each parent, with a rare fresh one")
  void inherit(
      final DogTraits parentA,
      final DogTraits parentB,
      final IntUnaryOperator nextInt,
      final List<DogTrait> expected) {
    assertEquals(expected, DogTraitInheritance.inherit(parentA, parentB, nextInt).traits());
  }

  @ParameterizedTest(name = "seed {0}")
  @ValueSource(longs = {3L, 99L})
  @DisplayName("pups of two clingy parents are nearly always clingy")
  void pupsMostlyKeepASharedTrait(final long seed) {
    final Random random = new Random(seed);
    final DogTraits clingy = DogTraits.of(DogTrait.CLINGY);
    int clingyPups = 0;
    final int litters = 2000;
    for (int i = 0; i < litters; i++) {
      if (DogTraitInheritance.inherit(clingy, clingy, random::nextInt).has(DogTrait.CLINGY)) {
        clingyPups++;
      }
    }
    final double expectedMiss =
        1.0 / (DogGeneInheritance.MUTATION_ONE_IN * DogGeneInheritance.MUTATION_ONE_IN);
    assertTrue(clingyPups >= litters * (1.0 - expectedMiss) - 10, "only " + clingyPups);
  }
}
