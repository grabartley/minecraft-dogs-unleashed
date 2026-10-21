package com.grahambartley.dogsunleashed.entity.sled;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.config.DireWolfStaminaSettings;
import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.DireWolfStamina;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class SledTeamStaminaTest {

  private static final DireWolfStaminaSettings DEFAULTS = DireWolfStaminaSettings.DEFAULTS;

  @ParameterizedTest(name = "{0} at {1}% -> {2}")
  @CsvSource({
    "YEARLING, 100, 100",
    "ADULT, 100, 100",
    "PRIME, 100, 100",
    "ALPHA, 100, 140",
    "ADULT, 200, 200",
    "ALPHA, 50, 70"
  })
  @DisplayName("every puller holds the prime's stamina, an alpha more, scaled by the setting")
  void capacity(final DireWolfGrowthStage stage, final int percent, final float expected) {
    assertEquals(
        expected, SledTeamStamina.capacity(stage, DEFAULTS.withCapacityPercent(percent)), 1.0e-4f);
  }

  @ParameterizedTest(name = "{0} dogs share the drain at x{1}")
  @CsvSource({"0, 0.0", "1, 2.0", "2, 1.0", "4, 0.5", "6, 0.33333334"})
  @DisplayName("a pair sprints at the riding drain and bigger teams share it")
  void drainScale(final int size, final float expected) {
    assertEquals(expected, SledTeamStamina.drainScale(size), 1.0e-6f);
  }

  @ParameterizedTest(name = "{0} dogs sprint for {1} ticks")
  @CsvSource({"1, 125", "2, 250", "6, 750"})
  @DisplayName("a bigger team lasts longer at a sprint before it slows to a trot")
  void biggerTeamsLastLonger(final int size, final int expectedTicks) {
    List<DireWolfStamina.State> team =
        new ArrayList<>(Collections.nCopies(size, new DireWolfStamina.State(100.0f, false)));
    int ticks = 0;
    while (SledTeamStamina.canSprint(team)) {
      final List<DireWolfStamina.State> next = new ArrayList<>();
      for (final DireWolfStamina.State state : team) {
        next.add(
            DireWolfStamina.next(
                state, 100.0f, true, true, DEFAULTS, SledTeamStamina.drainScale(size)));
      }
      team = next;
      ticks++;
    }
    assertEquals(expectedTicks, ticks, 1);
  }

  static Stream<Arguments> teams() {
    return Stream.of(
        Arguments.of(List.of(), false),
        Arguments.of(List.of(new DireWolfStamina.State(50.0f, false)), true),
        Arguments.of(
            List.of(
                new DireWolfStamina.State(50.0f, false), new DireWolfStamina.State(10.0f, true)),
            false),
        Arguments.of(
            List.of(
                new DireWolfStamina.State(50.0f, false), new DireWolfStamina.State(0.0f, false)),
            false));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("teams")
  @DisplayName("a team sprints only while every dog still can")
  void canSprint(final List<DireWolfStamina.State> team, final boolean expected) {
    assertEquals(expected, SledTeamStamina.canSprint(team));
  }

  @ParameterizedTest(name = "{0}/{1} and {2}/{3} -> {4}")
  @CsvSource({"50, 100, 100, 100, 0.75", "0, 100, 140, 140, 0.5833333", "0, 0, 0, 0, 0.0"})
  @DisplayName("the bar shows the whole team's stamina against its whole capacity")
  void fraction(
      final float first,
      final float firstCapacity,
      final float second,
      final float secondCapacity,
      final float expected) {
    assertEquals(
        expected,
        SledTeamStamina.fraction(
            List.of(
                new DireWolfStamina.State(first, false), new DireWolfStamina.State(second, false)),
            List.of(firstCapacity, secondCapacity)),
        1.0e-6f);
  }
}
