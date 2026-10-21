package com.grahambartley.dogsunleashed.entity.trick;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DogWheelSubAction;
import com.grahambartley.dogsunleashed.entity.DogWheelSubActions;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DogTrickWheelActionsTest {

  @ParameterizedTest
  @EnumSource(DogTrick.class)
  @DisplayName("each trick's wheel action carries its slot, label and icon")
  void actionMirrorsTrick(final DogTrick trick) {
    final DogWheelSubAction action = DogTrickWheelActions.action(trick);
    assertEquals(trick.wheelActionId(), action.id());
    assertEquals(trick.translationKey(), action.translationKey());
    assertEquals(trick.iconItemId(), action.iconItemId().toString());
  }

  @Test
  @DisplayName("registering adds one action per trick")
  void registersEveryTrick() {
    final DogWheelSubActions registry = new DogWheelSubActions();
    DogTrickWheelActions.register(registry);
    assertEquals(
        Arrays.stream(DogTrick.values())
            .map(DogTrickWheelActions::action)
            .map(DogWheelSubAction::id)
            .toList(),
        Arrays.stream(DogTrick.values())
            .map(trick -> registry.byId(trick.wheelActionId()).orElseThrow().id())
            .toList());
  }
}
