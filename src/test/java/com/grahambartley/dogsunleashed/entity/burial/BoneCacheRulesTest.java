package com.grahambartley.dogsunleashed.entity.burial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

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

  @ParameterizedTest(name = "taken on {0}")
  @EnumSource(DogCommand.class)
  @DisplayName("a dog keeps burying while its command is unchanged")
  void keepsBuryingOnTheSameCommand(final DogCommand command) {
    assertTrue(BoneCacheRules.keepsBurying(command, command));
  }

  @ParameterizedTest(name = "taken on {0}")
  @EnumSource(DogCommand.class)
  @DisplayName("a new command calls the dog off its burial")
  void stopsBuryingOnANewCommand(final DogCommand command) {
    for (final DogCommand other : DogCommand.values()) {
      if (other != command) {
        assertFalse(BoneCacheRules.keepsBurying(command, other), other.name());
      }
    }
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
