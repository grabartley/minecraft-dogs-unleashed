package com.grahambartley.dogsunleashed.entity.trick;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DogWheelSubAction;
import com.grahambartley.dogsunleashed.entity.DogWheelSubActions;
import com.grahambartley.dogsunleashed.entity.delivery.DogDeliveries;
import com.grahambartley.dogsunleashed.entity.expedition.DogExpeditions;
import net.minecraft.util.Identifier;
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
  @DisplayName("tricks register beside explore and deliver without clashing")
  void registersBesideTheOtherActions() {
    final DogWheelSubActions registry = new DogWheelSubActions();
    for (final int id : new int[] {DogExpeditions.WHEEL_ACTION_ID, DogDeliveries.WHEEL_ACTION_ID}) {
      registry.register(
          new DogWheelSubAction(
              id,
              "test",
              Identifier.of("minecraft", "bone"),
              (dog, player) -> null,
              (dog, player) -> {}));
    }

    DogTrickWheelActions.register(registry);

    for (final DogTrick trick : DogTrick.values()) {
      assertEquals(
          trick.translationKey(),
          registry.byId(trick.wheelActionId()).orElseThrow().translationKey());
    }
  }
}
