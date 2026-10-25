package com.grahambartley.dogsunleashed.entity.burial;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class BoneCacheRulesTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogCommand.class)
  @DisplayName("only roaming and staying dogs dig up bones")
  void digsUpOnlyWhenRoamingOrStaying(final DogCommand command) {
    assertEquals(
        command == DogCommand.FREE_ROAM || command == DogCommand.STAY,
        BoneCacheRules.digsUpOn(command));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogCommand.class)
  @DisplayName("only a sitting dog has to stand up to bury a bone")
  void standsToBuryOnlyWhenSitting(final DogCommand command) {
    assertEquals(command == DogCommand.SIT, BoneCacheRules.standsToBury(command));
  }

  static Stream<Arguments> commandPairs() {
    return Arrays.stream(DogCommand.values())
        .flatMap(
            takenOn -> Arrays.stream(DogCommand.values()).map(now -> Arguments.of(takenOn, now)));
  }

  @ParameterizedTest(name = "taken on {0}, now {1}")
  @MethodSource("commandPairs")
  @DisplayName("a dog keeps burying only while its command is unchanged")
  void keepsBuryingOnlyOnTheSameCommand(final DogCommand takenOn, final DogCommand now) {
    assertEquals(takenOn == now, BoneCacheRules.keepsBurying(takenOn, now));
  }

  @ParameterizedTest(name = "taken on {0}, now {1}")
  @MethodSource("commandPairs")
  @DisplayName("only a dog told to sit, and still told to, sits back down after burying")
  void sitsBackDownAfter(final DogCommand takenOn, final DogCommand now) {
    assertEquals(
        takenOn == DogCommand.SIT && now == DogCommand.SIT,
        BoneCacheRules.sitsBackDownAfter(takenOn, now));
  }

  @ParameterizedTest(name = "roll {0} -> {1} ticks")
  @CsvSource({"0, 60", "1, 61", "30, 90", "60, 120"})
  @DisplayName("a dog sniffs around for three to six seconds before it picks a spot")
  void sniffTicks(final int roll, final int expected) {
    assertEquals(expected, BoneCacheRules.sniffTicks(roll));
  }

  @ParameterizedTest(name = "buried at {0}, now {1}: due {2}")
  @CsvSource({
    "1000, 1000, false",
    "1000, 25000, false",
    "1000, 72999, false",
    "1000, 73000, true",
    "1000, 500000, true",
    "73000, 1000, true"
  })
  @DisplayName("a cache is due three in-game days after burial, or when time has run backwards")
  void dueAfterThreeDays(final long buriedAt, final long now, final boolean due) {
    assertEquals(due, BoneCacheRules.isDue(buriedAt, now));
  }

  @ParameterizedTest(name = "roll {0} gives a bonus: {1}")
  @CsvSource({"0, true", "1, false", "2, false", "3, false", "4, false"})
  @DisplayName("one roll in five finds a bonus item")
  void oneInFiveBonus(final int roll, final boolean bonus) {
    assertEquals(5, BoneCacheRules.BONUS_ODDS);
    assertEquals(bonus, BoneCacheRules.rollsBonus(roll));
  }
}
