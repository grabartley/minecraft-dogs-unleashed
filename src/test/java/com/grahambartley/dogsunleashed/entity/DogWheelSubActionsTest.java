package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.network.payload.WheelSubActionState;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DogWheelSubActionsTest {

  private static DogWheelSubAction action(final int id, final String refusalKey) {
    return new DogWheelSubAction(
        id,
        "test.action." + id,
        Identifier.of("minecraft", "bone"),
        (dog, player) -> refusalKey,
        (dog, player) -> {});
  }

  @Test
  @DisplayName("a new registry has no actions, so the wheel shows no Actions sector")
  void newRegistryIsEmpty() {
    final DogWheelSubActions registry = new DogWheelSubActions();
    assertTrue(registry.all().isEmpty());
    assertTrue(registry.statesFor(null, null).isEmpty());
  }

  @Test
  @DisplayName("actions keep the order they were registered in")
  void actionsKeepRegistrationOrder() {
    final DogWheelSubActions registry = new DogWheelSubActions();
    registry.register(action(5, null));
    registry.register(action(2, null));
    registry.register(action(9, null));

    assertEquals(List.of(5, 2, 9), registry.all().stream().map(DogWheelSubAction::id).toList());
  }

  @Test
  @DisplayName("an action is found by its id, and an unknown id finds nothing")
  void byIdFindsRegisteredActionsOnly() {
    final DogWheelSubActions registry = new DogWheelSubActions();
    final DogWheelSubAction trick = action(3, null);
    registry.register(trick);

    assertEquals(trick, registry.byId(3).orElseThrow());
    assertTrue(registry.byId(4).isEmpty());
  }

  @Test
  @DisplayName("two actions can't share an id")
  void duplicateIdsAreRefused() {
    final DogWheelSubActions registry = new DogWheelSubActions();
    registry.register(action(1, null));
    assertThrows(IllegalArgumentException.class, () -> registry.register(action(1, null)));
  }

  @Test
  @DisplayName("the actions ring is capped so the payload can bound what it reads")
  void registrationIsCapped() {
    final DogWheelSubActions registry = new DogWheelSubActions();
    IntStream.range(0, DogWheelSubActions.MAX_ACTIONS)
        .forEach(id -> registry.register(action(id, null)));
    assertThrows(
        IllegalStateException.class,
        () -> registry.register(action(DogWheelSubActions.MAX_ACTIONS, null)));
  }

  @Test
  @DisplayName("each action's state carries its refusal reason, empty when it can run")
  void statesCarryRefusalReasons() {
    final DogWheelSubActions registry = new DogWheelSubActions();
    registry.register(action(1, null));
    registry.register(action(2, "test.refused"));

    assertEquals(
        List.of(new WheelSubActionState(1, ""), new WheelSubActionState(2, "test.refused")),
        registry.statesFor(null, null));
  }
}
