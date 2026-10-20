package com.grahambartley.dogsunleashed.block;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class DogStatueSourceRules {

  public static final double REACH_BLOCKS = 8.0;

  public record Candidate<T>(T source, double distanceSquared) {}

  private DogStatueSourceRules() {}

  public static boolean inReach(final double distanceSquared) {
    return distanceSquared <= REACH_BLOCKS * REACH_BLOCKS;
  }

  public static <T> Optional<T> pick(
      final List<Candidate<T>> dogs, final Supplier<List<Candidate<T>>> graves) {
    final Optional<T> dog = nearestInReach(dogs);
    return dog.isPresent() ? dog : nearestInReach(graves.get());
  }

  private static <T> Optional<T> nearestInReach(final List<Candidate<T>> candidates) {
    return candidates.stream()
        .filter(candidate -> inReach(candidate.distanceSquared()))
        .min(Comparator.comparingDouble(Candidate::distanceSquared))
        .map(Candidate::source);
  }
}
