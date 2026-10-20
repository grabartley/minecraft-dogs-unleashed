package com.grahambartley.dogsunleashed.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.item.WhistlePointRules.MobFacts;
import com.grahambartley.dogsunleashed.item.WhistlePointRules.Order;
import com.grahambartley.dogsunleashed.item.WhistlePointRules.PointedKind;
import com.grahambartley.dogsunleashed.item.WhistlePointRules.Unavailable;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class WhistlePointRulesTest {

  private static final MobFacts ZOMBIE =
      new MobFacts(false, false, false, false, true, false, false);

  @ParameterizedTest(name = "{0} -> {2}")
  @MethodSource("pointedEntities")
  void ordersFollowWhatIsPointedAt(
      final String label, final PointedKind kind, final MobFacts mob, final Order expected) {
    assertEquals(expected, WhistlePointRules.orderForEntity(kind, mob));
  }

  static Stream<Arguments> pointedEntities() {
    return Stream.of(
        Arguments.of("dropped item", PointedKind.ITEM, ZOMBIE, Order.FETCH),
        Arguments.of("another player", PointedKind.PLAYER, ZOMBIE, Order.NONE),
        Arguments.of("armour stand", PointedKind.OTHER, ZOMBIE, Order.NONE),
        Arguments.of("zombie", PointedKind.MOB, ZOMBIE, Order.ATTACK),
        Arguments.of(
            "named zombie",
            PointedKind.MOB,
            new MobFacts(false, false, true, false, true, false, false),
            Order.ATTACK),
        Arguments.of(
            "wild wolf",
            PointedKind.MOB,
            new MobFacts(false, false, false, false, false, true, false),
            Order.ATTACK),
        Arguments.of(
            "named wolf",
            PointedKind.MOB,
            new MobFacts(false, false, true, false, false, true, false),
            Order.NONE),
        Arguments.of(
            "leashed llama",
            PointedKind.MOB,
            new MobFacts(false, false, false, true, false, true, false),
            Order.NONE),
        Arguments.of(
            "tamed wolf",
            PointedKind.MOB,
            new MobFacts(false, true, false, false, false, true, false),
            Order.NONE),
        Arguments.of(
            "villager or iron golem",
            PointedKind.MOB,
            new MobFacts(true, false, false, false, false, true, false),
            Order.NONE),
        Arguments.of(
            "creeper",
            PointedKind.MOB,
            new MobFacts(false, false, false, false, true, false, true),
            Order.NONE),
        Arguments.of(
            "cow",
            PointedKind.MOB,
            new MobFacts(false, false, false, false, false, false, false),
            Order.NONE));
  }

  @ParameterizedTest(name = "loaded={0} sitting={1} asleep={2} ridden={3} -> {4}")
  @CsvSource({
    "false, false, false, false, TOO_FAR",
    "false, true, true, true, TOO_FAR",
    "true, true, true, true, RIDDEN",
    "true, true, true, false, ASLEEP",
    "true, true, false, false, SITTING",
  })
  void anUnavailableDogSaysWhy(
      final boolean loaded,
      final boolean sitting,
      final boolean asleep,
      final boolean ridden,
      final Unavailable expected) {
    assertEquals(
        Optional.of(expected), WhistlePointRules.unavailable(loaded, sitting, asleep, ridden));
  }

  @ParameterizedTest
  @CsvSource({"true, false, false, false"})
  void aFreeDogTakesTheOrder(
      final boolean loaded, final boolean sitting, final boolean asleep, final boolean ridden) {
    assertEquals(Optional.empty(), WhistlePointRules.unavailable(loaded, sitting, asleep, ridden));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Unavailable.class)
  void everyRefusalHasAWhistleMessage(final Unavailable unavailable) {
    assertEquals(
        "message.dogs-unleashed.whistle." + unavailable.name().toLowerCase(),
        unavailable.messageKey());
  }

  @ParameterizedTest(name = "{0} blocks -> {1}")
  @CsvSource({"0, true", "63.9, true", "64, true", "64.1, false", "200, false"})
  void rangeIsSixtyFourBlocks(final double distance, final boolean expected) {
    assertEquals(expected, WhistlePointRules.inRange(distance * distance));
  }

  @ParameterizedTest(name = "item={0} claimed={1} player={2} mob={3} -> {4}")
  @CsvSource({
    "true, false, false, false, ITEM",
    "true, true, false, false, OTHER",
    "false, false, true, false, PLAYER",
    "false, false, false, true, MOB",
    "false, false, false, false, OTHER"
  })
  void kindOfSortsThePointedThing(
      final boolean item,
      final boolean claimedByAnotherPlayer,
      final boolean player,
      final boolean mob,
      final PointedKind expected) {
    assertEquals(expected, WhistlePointRules.kindOf(item, claimedByAnotherPlayer, player, mob));
  }
}
