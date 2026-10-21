package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.personality.DogTrait;
import com.grahambartley.dogsunleashed.entity.personality.DogTraits;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class DogPanicPolicyTest {

  private static final DogTraits BRAVE = DogTraits.of(DogTrait.BRAVE);
  private static final DogTraits TIMID = DogTraits.of(DogTrait.TIMID);

  static Stream<Arguments> notOnFireCases() {
    return Stream.of(
        Arguments.of(DogCommand.GUARD, false),
        Arguments.of(DogCommand.HUNT, false),
        Arguments.of(DogCommand.FOLLOW, true),
        Arguments.of(DogCommand.HEEL, true),
        Arguments.of(DogCommand.STAY, true),
        Arguments.of(DogCommand.SIT, true),
        Arguments.of(DogCommand.FREE_ROAM, true));
  }

  @ParameterizedTest(name = "{0} flees={1}")
  @MethodSource("notOnFireCases")
  @DisplayName("only Guard and Hunt dogs hold their ground when hurt")
  void guardAndHuntHoldGroundWhenNotOnFire(final DogCommand command, final boolean expectedToFlee) {
    assertEquals(
        expectedToFlee, DogPanicPolicy.shouldFleeDanger(command, false, DogTraits.NONE, 0.1));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogCommand.class)
  @DisplayName("a burning dog flees under every command, brave or not")
  void burningDogAlwaysFlees(final DogCommand command) {
    assertTrue(DogPanicPolicy.shouldFleeDanger(command, true, DogTraits.NONE, 1.0));
    assertTrue(DogPanicPolicy.shouldFleeDanger(command, true, BRAVE, 1.0));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogCommand.class)
  @DisplayName("a brave dog never flees a fight")
  void braveDogNeverFlees(final DogCommand command) {
    assertEquals(false, DogPanicPolicy.shouldFleeDanger(command, false, BRAVE, 0.05));
  }

  static Stream<Arguments> timidCases() {
    return Stream.of(
        Arguments.of(DogCommand.GUARD, 0.51, false),
        Arguments.of(DogCommand.GUARD, 0.5, true),
        Arguments.of(DogCommand.HUNT, 0.2, true),
        Arguments.of(DogCommand.HUNT, 0.9, false),
        Arguments.of(DogCommand.FOLLOW, 0.9, true));
  }

  @ParameterizedTest(name = "{0} at {1} health flees={2}")
  @MethodSource("timidCases")
  @DisplayName("a timid Guard or Hunt dog breaks off at half health")
  void timidDogFleesAtHalfHealth(
      final DogCommand command, final double health, final boolean expectedToFlee) {
    assertEquals(expectedToFlee, DogPanicPolicy.shouldFleeDanger(command, false, TIMID, health));
  }

  static Stream<Arguments> ownerFightCases() {
    return Stream.of(
        Arguments.of(DogCommand.FOLLOW, DogTraits.NONE, true),
        Arguments.of(DogCommand.FOLLOW, BRAVE, true),
        Arguments.of(DogCommand.FOLLOW, TIMID, false),
        Arguments.of(DogCommand.HEEL, TIMID, false),
        Arguments.of(DogCommand.GUARD, TIMID, true),
        Arguments.of(DogCommand.HUNT, TIMID, true));
  }

  @ParameterizedTest(name = "{0} {1} joins={2}")
  @MethodSource("ownerFightCases")
  @DisplayName("a timid dog only joins its owner's fights on Guard or Hunt")
  void joinsOwnerFight(final DogCommand command, final DogTraits traits, final boolean joins) {
    assertEquals(joins, DogPanicPolicy.joinsOwnerFight(command, traits));
  }
}
