package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DimensionReactionRules;

public final class NetherUneaseSchedule {

  private boolean reactedSinceArrival;
  private long ticksSinceReaction;

  public boolean tick(final int roll) {
    if (DimensionReactionRules.actsUneasyNow(
        this.reactedSinceArrival, this.ticksSinceReaction, roll)) {
      this.reactedSinceArrival = true;
      this.ticksSinceReaction = 0;
      return true;
    }
    this.ticksSinceReaction++;
    return false;
  }
}
