package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules.Spot;
import com.grahambartley.dogsunleashed.entity.goal.TugRules.Outcome;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TugStateTest {

  private static final UUID PLAYER = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final Spot CENTRE = new Spot(3, 4);
  private static final double EPSILON = 1.0e-9;

  private static TugState fresh(final double strength) {
    return TugState.start(PLAYER, strength, CENTRE, 100);
  }

  @ParameterizedTest(name = "{0} is the player -> {1}")
  @CsvSource({
    "00000000-0000-0000-0000-000000000001, true",
    "00000000-0000-0000-0000-000000000002, false"
  })
  @DisplayName("a tug knows which player holds the other end")
  void knowsItsPlayer(final UUID uuid, final boolean expected) {
    assertEquals(expected, fresh(0.01).isPlayer(uuid));
  }

  @ParameterizedTest(name = "pulls at {0} -> balance {1}")
  @CsvSource({
    "108, 0.18",
    "108;116, 0.36",
    "101;102, 0.0",
    "108;110, 0.18",
    "108;108;108, 0.18",
    "102;104;106;108;110, 0.0",
    "150, 0.08",
    "105;110;115;120;125;130, 1.0"
  })
  @DisplayName("each pull moves the rope your way by how well it keeps the rhythm")
  void pullsMoveTheRope(final String ticks, final double expected) {
    TugState state = fresh(0.01);
    for (final String tick : ticks.split(";")) {
      state = state.pulled(Long.parseLong(tick));
    }
    assertEquals(expected, state.balance(), EPSILON);
  }

  @ParameterizedTest(name = "strength {0}, {1} ticks at roll {2} -> balance {3}")
  @CsvSource({"0.01, 1, 0.5, -0.01", "0.01, 10, 0.0, -0.075", "0.02, 100, 1.0, -1.0"})
  @DisplayName("the dog drags the rope its way every tick")
  void dogDragsTheRope(
      final double strength, final int ticks, final double roll, final double expected) {
    TugState state = fresh(strength);
    for (int i = 0; i < ticks; i++) {
      state = state.dragged(roll);
    }
    assertEquals(expected, state.balance(), EPSILON);
  }

  @ParameterizedTest(name = "{0} rhythmic pulls, {1} drag ticks -> {2}")
  @CsvSource({"6, 0, PLAYER_WINS", "0, 100, DOG_WINS", "2, 10, ONGOING"})
  @DisplayName("the game is won at either end of the rope")
  void outcome(final int pulls, final int drags, final Outcome expected) {
    TugState state = fresh(0.01);
    for (int i = 1; i <= pulls; i++) {
      state = state.pulled(100 + 8L * i);
    }
    for (int i = 0; i < drags; i++) {
      state = state.dragged(1.0);
    }
    assertEquals(expected, state.outcome());
  }

  @ParameterizedTest(name = "kept away at {0}, checked at {1}, hurt {2} -> over {3}")
  @CsvSource({
    "500, 500, false, false",
    "500, 699, false, false",
    "500, 700, false, true",
    "500, 900, false, true",
    "500, 501, true, true"
  })
  @DisplayName("a dog that won keeps the rope away for ten seconds, or until it gets hurt")
  void keepAwayLastsTenSecondsOrUntilHurt(
      final long keptAt, final long now, final boolean hurt, final boolean over) {
    final TugState state = fresh(0.01).keptAway(keptAt);
    assertEquals(over, state.isKeepAwayOver(now, hurt));
    assertEquals(Outcome.DOG_WINS, state.outcome());
    assertEquals(CENTRE, state.centre());
  }

  @ParameterizedTest(name = "hurt {0}")
  @CsvSource({"true", "false"})
  @DisplayName("a tug that is still being played is never a finished keep-away")
  void anOngoingTugIsNotAKeepAway(final boolean hurt) {
    assertEquals(false, fresh(0.01).isKeepAwayOver(10_000, hurt));
  }

  @ParameterizedTest(name = "pull at {0}, drag roll {1}")
  @CsvSource({"108, 0.0", "500, 1.0"})
  @DisplayName("once the dog runs off, pulling and dragging no longer move the rope")
  void keepAwayFreezesTheRope(final long tick, final double roll) {
    final TugState kept = fresh(0.01).pulled(108).keptAway(120);
    assertEquals(0.18, kept.pulled(tick).dragged(roll).balance(), EPSILON);
  }

  @ParameterizedTest(name = "at {0} -> {1} ticks")
  @CsvSource({"100, 0", "350, 250", "50, 0"})
  @DisplayName("tug time counts from the start and never goes negative")
  void tugTicks(final long now, final long expected) {
    assertEquals(expected, fresh(0.01).tugTicks(now));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"00000000-0000-0000-0000-000000000002"})
  @DisplayName("a fresh tug starts level, on the player who started it")
  void startsLevel(final UUID stranger) {
    final TugState state = fresh(0.01);
    assertEquals(0.0, state.balance(), EPSILON);
    assertEquals(PLAYER, state.player());
    assertEquals(false, state.isPlayer(stranger));
    assertEquals(false, state.keepingAway());
  }
}
