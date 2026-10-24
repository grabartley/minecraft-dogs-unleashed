package com.grahambartley.dogsunleashed.entity.trick;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.grahambartley.dogsunleashed.entity.DogWheelSubAction;
import com.grahambartley.dogsunleashed.entity.DogWheelSubActions;
import com.grahambartley.dogsunleashed.entity.delivery.DogDeliveries;
import com.grahambartley.dogsunleashed.entity.expedition.DogExpeditions;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

  @ParameterizedTest(name = "{0} refuses with {1}")
  @CsvSource({
    "asleep, " + DogTrickWheelActions.ASLEEP_REFUSAL_KEY,
    "hitched, " + DogTrickWheelActions.BUSY_REFUSAL_KEY,
    "mounted, " + DogTrickWheelActions.BUSY_REFUSAL_KEY,
    "fighting, " + DogTrickWheelActions.BUSY_REFUSAL_KEY,
    "hurt, " + DogTrickWheelActions.BUSY_REFUSAL_KEY,
    "inWater, " + DogTrickWheelActions.BUSY_REFUSAL_KEY,
    "occupied, " + DogTrickWheelActions.BUSY_REFUSAL_KEY,
    "performing, " + DogTrickWheelActions.BUSY_REFUSAL_KEY
  })
  @DisplayName("a sleeping dog is too sleepy and any other busy dog is too busy")
  void busyDogRefuses(final String state, final String refusal) {
    assertEquals(refusal, DogTrickWheelActions.refusalKey(TrickReadinessTest.with(state)));
  }

  @Test
  @DisplayName("a free dog does the trick")
  void freeDogPerforms() {
    assertNull(DogTrickWheelActions.refusalKey(TrickReadinessTest.with("free")));
  }
}
