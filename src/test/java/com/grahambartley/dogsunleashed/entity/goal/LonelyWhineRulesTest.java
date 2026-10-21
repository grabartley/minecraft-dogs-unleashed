package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import java.util.OptionalDouble;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class LonelyWhineRulesTest {

  static Stream<Arguments> lonelyCases() {
    return Stream.of(
        Arguments.of(DogCommand.STAY, false, OptionalDouble.of(200.0), true),
        Arguments.of(DogCommand.STAY, false, OptionalDouble.empty(), false),
        Arguments.of(DogCommand.STAY, false, OptionalDouble.of(144.0), false),
        Arguments.of(DogCommand.STAY, true, OptionalDouble.of(200.0), false),
        Arguments.of(DogCommand.SIT, false, OptionalDouble.of(200.0), false),
        Arguments.of(DogCommand.FOLLOW, false, OptionalDouble.empty(), false),
        Arguments.of(DogCommand.GUARD, false, OptionalDouble.of(200.0), false));
  }

  @ParameterizedTest(name = "{0}, asleep {1}, owner {2} -> {3}")
  @MethodSource("lonelyCases")
  @DisplayName("a dog on Stay is lonely while its owner is in its world but away, and it is awake")
  void isLonely(
      final DogCommand command,
      final boolean asleep,
      final OptionalDouble ownerSquaredDistance,
      final boolean expected) {
    assertEquals(expected, LonelyWhineRules.isLonely(command, asleep, ownerSquaredDistance));
  }

  @ParameterizedTest(name = "{0} lonely ticks -> {1}")
  @CsvSource({"1, false", "2399, false", "2400, true", "2401, false", "2800, true", "3200, true"})
  @DisplayName("it whines after two minutes, then every twenty seconds")
  void whinesNow(final long lonelyTicks, final boolean expected) {
    assertEquals(expected, LonelyWhineRules.whinesNow(lonelyTicks));
  }
}
