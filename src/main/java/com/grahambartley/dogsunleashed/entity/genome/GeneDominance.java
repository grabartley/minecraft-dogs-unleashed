package com.grahambartley.dogsunleashed.entity.genome;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record GeneDominance(int alleleCount, Set<Integer> recessiveAlleles, int wildType) {

  public GeneDominance {
    if (alleleCount <= 0) {
      throw new IllegalArgumentException("A gene needs at least one allele");
    }
    if (!isValid(wildType, alleleCount) || recessiveAlleles.contains(wildType)) {
      throw new IllegalArgumentException("The wild type must be a dominant allele");
    }
    recessiveAlleles = Set.copyOf(recessiveAlleles);
  }

  public boolean isRecessive(final int allele) {
    return recessiveAlleles.contains(allele);
  }

  public boolean isValid(final int allele) {
    return isValid(allele, alleleCount);
  }

  public int express(final AllelePair pair) {
    if (!isRecessive(pair.first())) {
      return pair.first();
    }
    if (!isRecessive(pair.second())) {
      return pair.second();
    }
    return pair.first() == pair.second() ? pair.first() : wildType;
  }

  public List<Integer> carried(final AllelePair pair) {
    final int shown = express(pair);
    final Set<Integer> carried = new LinkedHashSet<>();
    for (final int allele : List.of(pair.first(), pair.second())) {
      if (allele != shown) {
        carried.add(allele);
      }
    }
    return List.copyOf(carried);
  }

  private static boolean isValid(final int allele, final int alleleCount) {
    return allele >= 0 && allele < alleleCount;
  }
}
