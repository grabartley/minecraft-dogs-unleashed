package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules;
import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules.Spot;
import java.util.UUID;

public record PlayChaseState(
    UUID partner, boolean chasing, boolean joined, Spot centre, long endsAt, long legStartedAt) {

  public record Pair(PlayChaseState inviter, PlayChaseState invitee) {}

  public static Pair pair(
      final UUID inviter,
      final UUID invitee,
      final Spot centre,
      final long now,
      final int gameTicks) {
    final long chaseStarts = now + PlayChaseRules.BOW_TICKS;
    final long endsAt = chaseStarts + gameTicks;
    return new Pair(
        new PlayChaseState(invitee, false, false, centre, endsAt, chaseStarts),
        new PlayChaseState(inviter, true, false, centre, endsAt, chaseStarts));
  }

  public boolean isPartneredWith(final UUID dog) {
    return this.partner.equals(dog);
  }

  public boolean isBowing(final long now) {
    return now < this.legStartedAt;
  }

  public boolean isOver(final long now) {
    return now >= this.endsAt;
  }

  public boolean isStoodUp(final long now, final boolean partnerJoined) {
    return !partnerJoined && !this.isBowing(now);
  }

  public boolean earnsRest(final long now) {
    return this.joined && !this.isBowing(now);
  }

  public long legTicks(final long now) {
    return Math.max(0, now - this.legStartedAt);
  }

  public PlayChaseState asJoined() {
    return new PlayChaseState(
        this.partner, this.chasing, true, this.centre, this.endsAt, this.legStartedAt);
  }

  public PlayChaseState swapped(final long now) {
    return new PlayChaseState(
        this.partner, !this.chasing, this.joined, this.centre, this.endsAt, now);
  }
}
