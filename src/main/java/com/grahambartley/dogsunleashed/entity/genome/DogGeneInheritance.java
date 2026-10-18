package com.grahambartley.dogsunleashed.entity.genome;

import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import org.jetbrains.annotations.Nullable;

public final class DogGeneInheritance {

  public static final int MUTATION_ONE_IN = 32;

  private DogGeneInheritance() {}

  public static AllelePair inherit(
      final @Nullable AllelePair parentA,
      final @Nullable AllelePair parentB,
      final GeneDominance dominance,
      final IntUnaryOperator nextInt) {
    return new AllelePair(passOn(parentA, dominance, nextInt), passOn(parentB, dominance, nextInt));
  }

  public static AllelePair wild(
      final int shown, final GeneDominance dominance, final IntSupplier carriedRoll) {
    return dominance.isRecessive(shown)
        ? AllelePair.matching(shown)
        : new AllelePair(shown, carriedRoll.getAsInt());
  }

  private static int passOn(
      final @Nullable AllelePair parent,
      final GeneDominance dominance,
      final IntUnaryOperator nextInt) {
    if (nextInt.applyAsInt(MUTATION_ONE_IN) == 0) {
      return nextInt.applyAsInt(dominance.alleleCount());
    }
    if (parent == null) {
      return dominance.wildType();
    }
    return nextInt.applyAsInt(2) == 0 ? parent.first() : parent.second();
  }
}
