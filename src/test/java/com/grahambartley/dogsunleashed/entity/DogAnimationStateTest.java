package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DogAnimationStateTest {

  @ParameterizedTest(name = "{0} reads")
  @ValueSource(ints = {1, 2, 5})
  @DisplayName("every reader of a dog's animation state shares one pose hand-back")
  void sharesOneHandBack(final int reads) {
    final DogAnimationState state = new DogAnimationState(null);
    final PoseBlendOut first = state.poseBlendOut();
    IntStream.range(0, reads).forEach(read -> assertSame(first, state.poseBlendOut()));
  }
}
