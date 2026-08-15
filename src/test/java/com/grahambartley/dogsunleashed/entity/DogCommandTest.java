package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class DogCommandTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogCommand.class)
  @DisplayName("every command round-trips through its id")
  void idRoundTrip(final DogCommand command) {
    assertSame(command, DogCommand.fromId(command.id()));
  }

  @ParameterizedTest(name = "id {0}")
  @ValueSource(ints = {-1, 7, 42, Integer.MAX_VALUE, Integer.MIN_VALUE})
  @DisplayName("unknown ids fall back to FOLLOW so corrupt saves stay playable")
  void unknownIdFallsBackToFollow(final int id) {
    assertSame(DogCommand.FOLLOW, DogCommand.fromId(id));
  }

  static Stream<Arguments> persistedIds() {
    return Stream.of(
        Arguments.of(DogCommand.FOLLOW, 0),
        Arguments.of(DogCommand.HEEL, 1),
        Arguments.of(DogCommand.STAY, 2),
        Arguments.of(DogCommand.SIT, 3),
        Arguments.of(DogCommand.HUNT, 4),
        Arguments.of(DogCommand.GUARD, 5),
        Arguments.of(DogCommand.FREE_ROAM, 6));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("persistedIds")
  @DisplayName("persisted ids stay stable for save compatibility")
  void persistedIdsAreStable(final DogCommand command, final int expectedId) {
    assertEquals(expectedId, command.id());
  }

  static Stream<Arguments> anchoredCommands() {
    return Stream.of(
        Arguments.of(DogCommand.FOLLOW, false),
        Arguments.of(DogCommand.HEEL, false),
        Arguments.of(DogCommand.STAY, true),
        Arguments.of(DogCommand.SIT, false),
        Arguments.of(DogCommand.HUNT, false),
        Arguments.of(DogCommand.GUARD, true),
        Arguments.of(DogCommand.FREE_ROAM, false));
  }

  @ParameterizedTest(name = "{0} anchored={1}")
  @MethodSource("anchoredCommands")
  @DisplayName("only Stay and Guard anchor the dog to a position")
  void anchoredCommands(final DogCommand command, final boolean expected) {
    assertEquals(expected, command.isAnchored());
  }

  static Stream<Arguments> relocationCommands() {
    return Stream.of(
        Arguments.of(DogCommand.FOLLOW, true),
        Arguments.of(DogCommand.HEEL, true),
        Arguments.of(DogCommand.STAY, false),
        Arguments.of(DogCommand.SIT, false),
        Arguments.of(DogCommand.HUNT, true),
        Arguments.of(DogCommand.GUARD, false),
        Arguments.of(DogCommand.FREE_ROAM, true));
  }

  @ParameterizedTest(name = "{0} follows={1}")
  @MethodSource("relocationCommands")
  @DisplayName("anchored and sitting commands opt out of owner-relocation summons")
  void relocationCommands(final DogCommand command, final boolean expected) {
    assertEquals(expected, command.followsOwnerOnRelocation());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogCommand.class)
  @DisplayName("translation and message keys derive from the serialized name")
  void translationKeysDeriveFromSerializedName(final DogCommand command) {
    assertEquals("command.dogs-unleashed." + command.serializedName(), command.translationKey());
    assertEquals(
        "message.dogs-unleashed.command_" + command.serializedName(), command.messageKey());
  }
}
