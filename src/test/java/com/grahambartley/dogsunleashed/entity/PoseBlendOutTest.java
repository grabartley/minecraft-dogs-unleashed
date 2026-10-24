package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PoseBlendOutTest {

  static Stream<Arguments> frames() {
    return Stream.of(
        Arguments.of(
            "never posing never ends", List.of(false, false, false), List.of(false, false, false)),
        Arguments.of(
            "ends on the first frame after the pose",
            List.of(true, true, false, false),
            List.of(false, false, true, false)),
        Arguments.of(
            "a pose still playing has not ended",
            List.of(true, true, true),
            List.of(false, false, false)),
        Arguments.of(
            "each pose ends once",
            List.of(true, false, true, false),
            List.of(false, true, false, true)),
        Arguments.of(
            "a one frame pose still ends",
            List.of(false, true, false),
            List.of(false, false, true)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("frames")
  @DisplayName("the movement clip blends back once, on the frame after a pose stops")
  void ends(final String scenario, final List<Boolean> posing, final List<Boolean> expected) {
    final PoseBlendOut blend = new PoseBlendOut();
    final List<Boolean> ended = new ArrayList<>();
    for (final boolean frame : posing) {
      ended.add(blend.ended(frame));
    }
    assertEquals(expected, ended);
  }
}
