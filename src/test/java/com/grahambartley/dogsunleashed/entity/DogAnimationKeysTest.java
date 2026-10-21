package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DogAnimationKeysTest {

  static Stream<Arguments> clipNames() {
    return Stream.of(
        Arguments.of(DogAnimationKeys.SIT, "sit"),
        Arguments.of(DogAnimationKeys.SLEEP, "sleep"),
        Arguments.of(DogAnimationKeys.PLAY_BOW, "play_bow"),
        Arguments.of(DogAnimationKeys.COWER, "cower"),
        Arguments.of(DogAnimationKeys.BEG, "beg"),
        Arguments.of(DogAnimationKeys.ROLL_OVER, "roll_over"),
        Arguments.of(DogAnimationKeys.PLAY_DEAD, "play_dead"),
        Arguments.of(DogAnimationKeys.TUG, "tug"),
        Arguments.of(DogAnimationKeys.EAT, "eat"));
  }

  @ParameterizedTest(name = "{1}")
  @MethodSource("clipNames")
  @DisplayName("each key names the clip the rig animation files use")
  void keysMatchClipNames(final String key, final String clip) {
    assertEquals(clip, key);
  }
}
