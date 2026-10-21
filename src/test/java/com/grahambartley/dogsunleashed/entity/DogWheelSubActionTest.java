package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DogWheelSubActionTest {

  private static final Identifier BONE = Identifier.of("minecraft", "bone");

  @Test
  @DisplayName("an action built without a visibility rule is always shown")
  void defaultsToAlwaysShown() {
    final DogWheelSubAction action =
        new DogWheelSubAction(0, "test", BONE, (dog, player) -> null, (dog, player) -> {});
    assertSame(DogWheelSubAction.ALWAYS_SHOWN, action.visibility());
    assertEquals(true, action.visibility().shownFor(null));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  @DisplayName("an action keeps the visibility rule it was given")
  void keepsGivenVisibility(final boolean shown) {
    final DogWheelSubAction action =
        new DogWheelSubAction(
            0, "test", BONE, (dog, player) -> null, (dog, player) -> {}, dog -> shown);
    assertEquals(shown, action.visibility().shownFor(null));
  }
}
