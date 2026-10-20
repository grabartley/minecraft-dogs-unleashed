package com.grahambartley.dogsunleashed.render.statue;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.block.DogStatuePose;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StatuePoseFrameTest {

  @ParameterizedTest(name = "{0} is frozen at tick {1}")
  @CsvSource({"SIT, 0", "STAND, 0", "HOWL, 40"})
  @DisplayName(
      "sit and stand freeze on their first frame, howl two seconds in with the head held up")
  void poseFreezesOnItsFrame(final DogStatuePose pose, final double tick) {
    assertEquals(tick, StatuePoseFrame.tickOf(pose));
  }
}
