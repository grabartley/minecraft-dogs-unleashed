package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.grahambartley.dogsunleashed.entity.DogPlaySession.FetchStatus;
import com.grahambartley.dogsunleashed.entity.fetch.FetchItemType;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DogPlaySessionTest {

  static Stream<Arguments> fetchStatuses() {
    return Stream.of(
        Arguments.of("not playing at all", false, false, false, false, FetchStatus.IDLE),
        Arguments.of("carrying the item home", true, true, false, true, FetchStatus.FETCHING),
        Arguments.of("running at a landed item", true, false, true, true, FetchStatus.FETCHING),
        Arguments.of(
            "playing with nothing to chase yet",
            true,
            false,
            false,
            true,
            FetchStatus.SCAN_FOR_PROJECTILE),
        Arguments.of(
            "playing but the partner is gone", true, false, false, false, FetchStatus.IDLE),
        Arguments.of(
            "carrying counts even without a partner",
            true,
            true,
            false,
            false,
            FetchStatus.FETCHING));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("fetchStatuses")
  @DisplayName("fetch status resolves without a world wherever the answer is already decided")
  void fetchStatusResolvesFromState(
      final String label,
      final boolean inPlayMode,
      final boolean carrying,
      final boolean hasFetchTarget,
      final boolean hasPartner,
      final FetchStatus expected) {
    assertEquals(
        expected, DogPlaySession.fetchStatus(inPlayMode, carrying, hasFetchTarget, hasPartner));
  }

  @Test
  @DisplayName("a dog outside play mode is idle whatever else is true of it")
  void outsidePlayModeIsAlwaysIdle() {
    for (int combination = 0; combination < 8; combination++) {
      final boolean carrying = (combination & 1) != 0;
      final boolean hasFetchTarget = (combination & 2) != 0;
      final boolean hasPartner = (combination & 4) != 0;
      assertEquals(
          FetchStatus.IDLE,
          DogPlaySession.fetchStatus(false, carrying, hasFetchTarget, hasPartner),
          "carrying=" + carrying + " target=" + hasFetchTarget + " partner=" + hasPartner);
    }
  }

  @Test
  @DisplayName("a projectile scan is only ever needed when nothing local settles the question")
  void scanIsTheLastResort() {
    for (int combination = 0; combination < 8; combination++) {
      final boolean carrying = (combination & 1) != 0;
      final boolean hasFetchTarget = (combination & 2) != 0;
      final boolean hasPartner = (combination & 4) != 0;
      final FetchStatus status =
          DogPlaySession.fetchStatus(true, carrying, hasFetchTarget, hasPartner);
      if (status == FetchStatus.SCAN_FOR_PROJECTILE) {
        assertEquals(false, carrying, "a carrying dog should not need a scan");
        assertEquals(false, hasFetchTarget, "a dog with a target should not need a scan");
        assertEquals(true, hasPartner, "a scan is pointless without a partner");
      }
    }
  }

  @Test
  @DisplayName("no active fetch type serialises as the empty id")
  void noFetchTypeSerialisesEmpty() {
    assertEquals("", DogPlaySession.fetchTypeIdOf(null));
    assertEquals(DogPlaySession.NO_ACTIVE_FETCH_TYPE, DogPlaySession.fetchTypeIdOf(null));
  }

  @Test
  @DisplayName("an active fetch type serialises as its identifier")
  void fetchTypeSerialisesAsItsIdentifier() {
    final FetchItemType stick =
        new FetchItemType(Identifier.of("dogs-unleashed", "stick"), null, null, null, null);
    assertEquals("dogs-unleashed:stick", DogPlaySession.fetchTypeIdOf(stick));
  }

  @Test
  @DisplayName("the empty id reads back as no active fetch type")
  void emptyIdReadsBackAsNoFetchType() {
    assertNull(DogPlaySession.fetchTypeFromId(""));
    assertNull(DogPlaySession.fetchTypeFromId(DogPlaySession.NO_ACTIVE_FETCH_TYPE));
  }
}
