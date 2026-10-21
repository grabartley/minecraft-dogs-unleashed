package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import java.util.OptionalDouble;

public final class LonelyWhineRules {

  public static final long LONELY_AFTER_TICKS = 2 * 60 * 20;
  public static final long WHINE_EVERY_TICKS = 20 * 20;
  public static final double OWNER_AWAY_RANGE = 12.0;

  private LonelyWhineRules() {}

  public static boolean isLonely(
      final DogCommand command, final boolean asleep, final OptionalDouble ownerSquaredDistance) {
    return command == DogCommand.STAY
        && !asleep
        && (ownerSquaredDistance.isEmpty()
            || ownerSquaredDistance.getAsDouble() > OWNER_AWAY_RANGE * OWNER_AWAY_RANGE);
  }

  public static boolean whinesNow(final long lonelyTicks) {
    return lonelyTicks >= LONELY_AFTER_TICKS
        && (lonelyTicks - LONELY_AFTER_TICKS) % WHINE_EVERY_TICKS == 0;
  }
}
