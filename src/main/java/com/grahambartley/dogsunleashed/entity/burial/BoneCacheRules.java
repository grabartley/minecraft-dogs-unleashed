package com.grahambartley.dogsunleashed.entity.burial;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import java.util.EnumSet;
import java.util.Set;

public final class BoneCacheRules {

  public static final int BURY_RADIUS = 16;
  public static final int DIG_TICKS = 60;
  public static final int SNIFF_MIN_TICKS = 60;
  public static final int SNIFF_SPREAD_TICKS = 60;
  public static final int DAYS_UNTIL_DIG_UP = 3;
  public static final long TICKS_PER_DAY = 24000L;
  public static final int BONUS_ODDS = 5;
  public static final double OWNER_RANGE = 32.0;
  public static final double DIG_UP_RANGE = 48.0;

  private static final Set<DogCommand> DIGGING_UP_COMMANDS =
      EnumSet.of(DogCommand.FREE_ROAM, DogCommand.STAY);

  private BoneCacheRules() {}

  public static boolean digsUpOn(final DogCommand command) {
    return DIGGING_UP_COMMANDS.contains(command);
  }

  public static int sniffTicks(final int roll) {
    return SNIFF_MIN_TICKS + Math.floorMod(roll, SNIFF_SPREAD_TICKS + 1);
  }

  public static boolean standsToBury(final DogCommand command) {
    return command == DogCommand.SIT;
  }

  public static boolean keepsBurying(final DogCommand takenOn, final DogCommand now) {
    return takenOn == now;
  }

  public static boolean isDue(final long buriedAt, final long now) {
    return now < buriedAt || now - buriedAt >= DAYS_UNTIL_DIG_UP * TICKS_PER_DAY;
  }

  public static boolean rollsBonus(final int roll) {
    return roll == 0;
  }
}
