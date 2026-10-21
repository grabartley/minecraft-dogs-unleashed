package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules;
import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules.Spot;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PlayChaseStateTest {

  private static final UUID PARTNER = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final Spot CENTRE = new Spot(4, -2);
  private static final long START = 1000;
  private static final int GAME_TICKS = 300;

  private static PlayChaseState begun(final boolean chasing) {
    return PlayChaseState.begin(PARTNER, chasing, CENTRE, START, GAME_TICKS);
  }

  @ParameterizedTest
  @CsvSource({"1000, true", "1029, true", "1030, false", "1200, false"})
  void bothDogsHoldStillForTheBowBeforeTheChase(final long now, final boolean bowing) {
    assertEquals(bowing, begun(true).isBowing(now));
  }

  @ParameterizedTest
  @CsvSource({"1000, false", "1329, false", "1330, true", "2000, true"})
  void theGameRunsItsFullLengthAfterTheBow(final long now, final boolean over) {
    assertEquals(over, begun(false).isOver(now));
  }

  @ParameterizedTest
  @CsvSource({"1000, 0", "1030, 0", "1100, 70"})
  void theFirstChaseLegStartsWhenTheBowEnds(final long now, final long legTicks) {
    assertEquals(legTicks, begun(true).legTicks(now));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void swappingFlipsTheRoleAndRestartsTheLegButKeepsTheGame(final boolean chasing) {
    final PlayChaseState state = begun(chasing);
    final PlayChaseState swapped = state.swapped(1200);
    assertNotEquals(chasing, swapped.chasing());
    assertEquals(0, swapped.legTicks(1200));
    assertEquals(state.endsAt(), swapped.endsAt());
    assertEquals(PARTNER, swapped.partner());
    assertEquals(CENTRE, swapped.centre());
    assertEquals(false, swapped.isBowing(1200));
  }

  @ParameterizedTest
  @ValueSource(ints = {PlayChaseRules.MIN_GAME_TICKS, PlayChaseRules.MAX_GAME_TICKS})
  void bothEndsOfTheGameLengthFinishOnTime(final int ticks) {
    final PlayChaseState state = PlayChaseState.begin(PARTNER, true, CENTRE, 0, ticks);
    assertEquals(PlayChaseRules.BOW_TICKS + ticks, state.endsAt());
  }
}
