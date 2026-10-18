package com.grahambartley.dogsunleashed.entity.goal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

final class PenMemory {

  static final int MEMORY_TICKS = 20;
  static final int PURGE_SIZE = 256;

  private record Answer(boolean penned, long checkedAt) {}

  private final Map<Long, Answer> answers = new HashMap<>();

  boolean isPenned(final long blockPos, final long now, final BooleanSupplier scan) {
    final Answer known = this.answers.get(blockPos);
    if (known != null && isFresh(known, now)) {
      return known.penned();
    }
    if (this.answers.size() >= PURGE_SIZE) {
      this.answers.values().removeIf(answer -> !isFresh(answer, now));
    }
    final boolean penned = scan.getAsBoolean();
    this.answers.put(blockPos, new Answer(penned, now));
    return penned;
  }

  int size() {
    return this.answers.size();
  }

  private static boolean isFresh(final Answer answer, final long now) {
    return now >= answer.checkedAt() && now - answer.checkedAt() < MEMORY_TICKS;
  }
}
