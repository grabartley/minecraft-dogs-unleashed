package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class DogWheelActionTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogWheelAction.class)
  @DisplayName("every wheel action round-trips through its wire id")
  void idRoundTrip(final DogWheelAction action) {
    assertSame(action, DogWheelAction.fromId(action.id()));
  }

  @ParameterizedTest(name = "id {0}")
  @ValueSource(ints = {-1, 9, 42, Integer.MAX_VALUE, Integer.MIN_VALUE})
  @DisplayName("unknown wire ids resolve to null so the server drops the packet")
  void unknownIdResolvesToNull(final int id) {
    assertNull(DogWheelAction.fromId(id));
  }

  static Stream<Arguments> commandMappings() {
    return Stream.of(
        Arguments.of(DogWheelAction.FOLLOW, DogCommand.FOLLOW),
        Arguments.of(DogWheelAction.HEEL, DogCommand.HEEL),
        Arguments.of(DogWheelAction.STAY, DogCommand.STAY),
        Arguments.of(DogWheelAction.SIT, DogCommand.SIT),
        Arguments.of(DogWheelAction.HUNT, DogCommand.HUNT),
        Arguments.of(DogWheelAction.GUARD, DogCommand.GUARD),
        Arguments.of(DogWheelAction.FREE_ROAM, DogCommand.FREE_ROAM));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("commandMappings")
  @DisplayName("each persistent wheel action maps to its command")
  void commandMappings(final DogWheelAction action, final DogCommand expected) {
    assertSame(expected, action.command());
    assertEquals(expected.translationKey(), action.translationKey());
    assertFalse(action.isOneShot());
  }

  static Stream<Arguments> oneShotActions() {
    return Stream.of(
        Arguments.of(DogWheelAction.GO_TO_BED, "command.dogs-unleashed.go_to_bed"),
        Arguments.of(DogWheelAction.EQUIPMENT, "command.dogs-unleashed.equipment"));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("oneShotActions")
  @DisplayName("one-shot actions carry no persistent command but keep their own label")
  void oneShotActionsHaveNoCommand(final DogWheelAction action, final String expectedKey) {
    assertNull(action.command());
    assertTrue(action.isOneShot());
    assertEquals(expectedKey, action.translationKey());
  }
}
