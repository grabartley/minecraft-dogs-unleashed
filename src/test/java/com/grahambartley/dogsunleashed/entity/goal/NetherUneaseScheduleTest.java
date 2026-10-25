package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.DimensionReactionRules;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NetherUneaseScheduleTest {

  private static final int MISS = 1;
  private static final int HIT = 0;

  @ParameterizedTest(name = "roll {0}")
  @ValueSource(ints = {0, 1, 2399})
  @DisplayName("the first tick after arriving always reacts, whatever the roll")
  void reactsOnArrival(final int roll) {
    assertTrue(new NetherUneaseSchedule().tick(roll));
  }

  @ParameterizedTest(name = "{0} quiet ticks")
  @ValueSource(longs = {1, 600, DimensionReactionRules.UNEASE_MIN_GAP_TICKS})
  @DisplayName("no reaction inside the minimum gap, even on a winning roll")
  void staysQuietInsideTheGap(final long quietTicks) {
    final NetherUneaseSchedule schedule = new NetherUneaseSchedule();
    schedule.tick(MISS);
    for (long tick = 0; tick < quietTicks; tick++) {
      assertFalse(schedule.tick(HIT), "tick " + tick);
    }
  }

  @ParameterizedTest(name = "{0} missed rolls after the gap")
  @ValueSource(ints = {0, 1, 500})
  @DisplayName("after the gap only a winning roll reacts, and it starts a new gap")
  void reactsOnAWinningRollAfterTheGap(final int missedRolls) {
    final NetherUneaseSchedule schedule = new NetherUneaseSchedule();
    schedule.tick(MISS);
    for (long tick = 0; tick < DimensionReactionRules.UNEASE_MIN_GAP_TICKS; tick++) {
      schedule.tick(HIT);
    }
    for (int miss = 0; miss < missedRolls; miss++) {
      assertFalse(schedule.tick(MISS), "miss " + miss);
    }
    assertTrue(schedule.tick(HIT));
    assertFalse(schedule.tick(HIT), "a reaction starts a new gap");
  }

  @ParameterizedTest(name = "roll {0}")
  @ValueSource(ints = {1, 2, DimensionReactionRules.UNEASE_ONE_IN_TICKS - 1})
  @DisplayName("a losing roll never reacts after the gap")
  void losingRollsNeverReact(final int roll) {
    final NetherUneaseSchedule schedule = new NetherUneaseSchedule();
    schedule.tick(MISS);
    for (long tick = 0; tick < DimensionReactionRules.UNEASE_MIN_GAP_TICKS; tick++) {
      schedule.tick(HIT);
    }
    for (int tick = 0; tick < DimensionReactionRules.UNEASE_ONE_IN_TICKS; tick++) {
      assertFalse(schedule.tick(roll), "tick " + tick);
    }
  }
}
