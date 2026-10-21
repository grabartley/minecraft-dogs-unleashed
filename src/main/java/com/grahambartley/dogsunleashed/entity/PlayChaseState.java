package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules;
import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules.Spot;
import java.util.UUID;

public record PlayChaseState(
    UUID partner, boolean chasing, Spot centre, long startedAt, long endsAt, long legStartedAt) {

  public static PlayChaseState begin(
      final UUID partner,
      final boolean chasing,
      final Spot centre,
      final long now,
      final int gameTicks) {
    final long chaseStarts = now + PlayChaseRules.BOW_TICKS;
    return new PlayChaseState(partner, chasing, centre, now, chaseStarts + gameTicks, chaseStarts);
  }

  public boolean isBowing(final long now) {
    return now < this.legStartedAt && now - this.startedAt < PlayChaseRules.BOW_TICKS;
  }

  public boolean isOver(final long now) {
    return now >= this.endsAt;
  }

  public long legTicks(final long now) {
    return Math.max(0, now - this.legStartedAt);
  }

  public PlayChaseState swapped(final long now) {
    return new PlayChaseState(
        this.partner, !this.chasing, this.centre, this.startedAt, this.endsAt, now);
  }
}
