package com.grahambartley.dogsunleashed.entity.genome;

public record AllelePair(int first, int second) {

  public static AllelePair matching(final int allele) {
    return new AllelePair(allele, allele);
  }
}
