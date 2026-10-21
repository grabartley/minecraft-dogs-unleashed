package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules.Spot;
import com.grahambartley.dogsunleashed.entity.goal.TugRules;
import java.util.UUID;

public record TugState(
    UUID player,
    boolean keepingAway,
    double balance,
    double dogStrength,
    Spot centre,
    long startedAt,
    long lastPullAt,
    long keepAwayEndsAt) {

  public static TugState start(
      final UUID player, final double dogStrength, final Spot centre, final long now) {
    return new TugState(player, false, 0.0, dogStrength, centre, now, now, now);
  }

  public boolean isPlayer(final UUID uuid) {
    return this.player.equals(uuid);
  }

  public long tugTicks(final long now) {
    return Math.max(0, now - this.startedAt);
  }

  public TugRules.Outcome outcome() {
    return this.keepingAway ? TugRules.Outcome.DOG_WINS : TugRules.outcomeOf(this.balance);
  }

  public TugState pulled(final long now) {
    if (this.keepingAway) {
      return this;
    }
    return this.withBalance(
            TugRules.balanceAfter(this.balance, TugRules.playerPull(now - this.lastPullAt)))
        .withLastPullAt(now);
  }

  public TugState dragged(final double roll) {
    if (this.keepingAway) {
      return this;
    }
    return this.withBalance(
        TugRules.balanceAfter(this.balance, -TugRules.dogPull(this.dogStrength, roll)));
  }

  public TugState keptAway(final long now) {
    return new TugState(
        this.player,
        true,
        this.balance,
        this.dogStrength,
        this.centre,
        this.startedAt,
        this.lastPullAt,
        now + TugRules.KEEP_AWAY_TICKS);
  }

  public boolean isKeepAwayOver(final long now, final boolean hurt) {
    return this.keepingAway && (hurt || now >= this.keepAwayEndsAt);
  }

  private TugState withBalance(final double newBalance) {
    return new TugState(
        this.player,
        this.keepingAway,
        newBalance,
        this.dogStrength,
        this.centre,
        this.startedAt,
        this.lastPullAt,
        this.keepAwayEndsAt);
  }

  private TugState withLastPullAt(final long now) {
    return new TugState(
        this.player,
        this.keepingAway,
        this.balance,
        this.dogStrength,
        this.centre,
        this.startedAt,
        now,
        this.keepAwayEndsAt);
  }
}
