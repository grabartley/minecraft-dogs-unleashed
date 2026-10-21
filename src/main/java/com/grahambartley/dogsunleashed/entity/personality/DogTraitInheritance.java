package com.grahambartley.dogsunleashed.entity.personality;

import com.grahambartley.dogsunleashed.entity.genome.DogGeneInheritance;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;

public final class DogTraitInheritance {

  public static final int SECOND_TRAIT_ONE_IN = 3;

  private DogTraitInheritance() {}

  public static DogTraits wild(final IntUnaryOperator nextInt) {
    final DogTrait first = randomTrait(nextInt);
    if (nextInt.applyAsInt(SECOND_TRAIT_ONE_IN) != 0) {
      return DogTraits.of(first);
    }
    final List<DogTrait> partners = new ArrayList<>();
    for (final DogTrait trait : DogTrait.values()) {
      if (trait != first && !trait.conflictsWith(first)) {
        partners.add(trait);
      }
    }
    return DogTraits.of(first, partners.get(nextInt.applyAsInt(partners.size())));
  }

  public static DogTraits inherit(
      final DogTraits parentA, final DogTraits parentB, final IntUnaryOperator nextInt) {
    return DogTraits.of(passOn(parentA, nextInt), passOn(parentB, nextInt));
  }

  private static DogTrait passOn(final DogTraits parent, final IntUnaryOperator nextInt) {
    if (parent.isEmpty() || nextInt.applyAsInt(DogGeneInheritance.MUTATION_ONE_IN) == 0) {
      return randomTrait(nextInt);
    }
    final List<DogTrait> traits = parent.traits();
    return traits.get(nextInt.applyAsInt(traits.size()));
  }

  private static DogTrait randomTrait(final IntUnaryOperator nextInt) {
    return DogTrait.values()[nextInt.applyAsInt(DogTrait.values().length)];
  }
}
