package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules;
import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules.Spot;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PlayChaseStateTest {

  private static final UUID INVITER = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID INVITEE = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final Spot CENTRE = new Spot(4, -2);
  private static final long START = 1000;
  private static final int GAME_TICKS = 300;

  private static PlayChaseState.Pair pair() {
    return PlayChaseState.pair(INVITER, INVITEE, CENTRE, START, GAME_TICKS);
  }

  @Test
  void theInviterRunsFirstAndTheInviteeChasesIt() {
    final PlayChaseState.Pair pair = pair();
    assertFalse(pair.inviter().chasing());
    assertTrue(pair.invitee().chasing());
    assertTrue(pair.inviter().isPartneredWith(INVITEE));
    assertTrue(pair.invitee().isPartneredWith(INVITER));
    assertFalse(pair.inviter().isPartneredWith(INVITER));
    assertEquals(pair.inviter().endsAt(), pair.invitee().endsAt());
    assertEquals(CENTRE, pair.invitee().centre());
    assertFalse(pair.inviter().joined());
    assertFalse(pair.invitee().joined());
  }

  @ParameterizedTest
  @CsvSource({"1000, true", "1029, true", "1030, false", "1200, false"})
  void bothDogsHoldStillForTheBowBeforeTheChase(final long now, final boolean bowing) {
    assertEquals(bowing, pair().invitee().isBowing(now));
    assertEquals(bowing, pair().inviter().isBowing(now));
  }

  @ParameterizedTest
  @CsvSource({"1000, false", "1329, false", "1330, true", "2000, true"})
  void theGameRunsItsFullLengthAfterTheBow(final long now, final boolean over) {
    assertEquals(over, pair().inviter().isOver(now));
  }

  @ParameterizedTest
  @CsvSource({
    "1000, false, false",
    "1029, false, false",
    "1030, false, true",
    "1030, true, false",
    "1200, true, false"
  })
  void aDogIsStoodUpWhenItsPartnerHasNotJoinedByTheEndOfTheBow(
      final long now, final boolean partnerJoined, final boolean stood) {
    assertEquals(stood, pair().inviter().isStoodUp(now, partnerJoined));
  }

  @ParameterizedTest
  @CsvSource({
    "false, 1000, false",
    "false, 1200, false",
    "true, 1029, false",
    "true, 1030, true",
    "true, 1200, true"
  })
  void onlyAGameThatGotPastTheBowEarnsARest(
      final boolean joined, final long now, final boolean rests) {
    final PlayChaseState state = joined ? pair().inviter().asJoined() : pair().inviter();
    assertEquals(rests, state.earnsRest(now));
  }

  @ParameterizedTest
  @CsvSource({"1000, 0", "1030, 0", "1100, 70"})
  void theFirstChaseLegStartsWhenTheBowEnds(final long now, final long legTicks) {
    assertEquals(legTicks, pair().invitee().legTicks(now));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void joiningKeepsEverythingElse(final boolean inviter) {
    final PlayChaseState state = inviter ? pair().inviter() : pair().invitee();
    final PlayChaseState joined = state.asJoined();
    assertTrue(joined.joined());
    assertEquals(state.chasing(), joined.chasing());
    assertEquals(state.endsAt(), joined.endsAt());
    assertEquals(state.legStartedAt(), joined.legStartedAt());
    assertEquals(state.partner(), joined.partner());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void swappingFlipsTheRoleAndRestartsTheLegButKeepsTheGame(final boolean inviter) {
    final PlayChaseState state = (inviter ? pair().inviter() : pair().invitee()).asJoined();
    final PlayChaseState swapped = state.swapped(1200);
    assertNotEquals(state.chasing(), swapped.chasing());
    assertEquals(0, swapped.legTicks(1200));
    assertEquals(state.endsAt(), swapped.endsAt());
    assertEquals(state.partner(), swapped.partner());
    assertTrue(swapped.joined());
    assertFalse(swapped.isBowing(1200));
  }

  @ParameterizedTest
  @ValueSource(ints = {PlayChaseRules.MIN_GAME_TICKS, PlayChaseRules.MAX_GAME_TICKS})
  void bothEndsOfTheGameLengthFinishOnTime(final int ticks) {
    final PlayChaseState state = PlayChaseState.pair(INVITER, INVITEE, CENTRE, 0, ticks).invitee();
    assertEquals(PlayChaseRules.BOW_TICKS + ticks, state.endsAt());
  }
}
