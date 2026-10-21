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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DogWheelSubActionsTest {

  private static final Identifier BONE = Identifier.of("minecraft", "bone");

  private static DogWheelSubAction action(final int id, final String refusalKey) {
    return new DogWheelSubAction(
        id, "test.action." + id, BONE, (dog, player) -> refusalKey, (dog, player) -> {});
  }

  @Test
  @DisplayName("a new registry has no actions, so the wheel shows no Actions sector")
  void newRegistryIsEmpty() {
    final DogWheelSubActions registry = new DogWheelSubActions();
    assertTrue(registry.statesFor(null, null).isEmpty());
  }

  @Test
  @DisplayName("actions keep the order they were registered in")
  void actionsKeepRegistrationOrder() {
    final DogWheelSubActions registry = new DogWheelSubActions();
    registry.register(action(5, null));
    registry.register(action(2, null));
    registry.register(action(9, null));

    assertEquals(
        List.of(5, 2, 9),
        registry.statesFor(null, null).stream().map(WheelSubActionState::id).toList());
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
    IntStream.range(0, WheelSubActionState.MAX_PER_WHEEL)
        .forEach(id -> registry.register(action(id, null)));
    assertThrows(
        IllegalStateException.class,
        () -> registry.register(action(WheelSubActionState.MAX_PER_WHEEL, null)));
  }

  @Test
  @DisplayName("each action's state carries its refusal reason, empty when it can run")
  void statesCarryRefusalReasons() {
    final DogWheelSubActions registry = new DogWheelSubActions();
    registry.register(action(1, null));
    registry.register(action(2, "test.refused"));

    assertEquals(
        List.of(
            new WheelSubActionState(1, "test.action.1", BONE, ""),
            new WheelSubActionState(2, "test.action.2", BONE, "test.refused")),
        registry.statesFor(null, null));
  }

  @ParameterizedTest(name = "shown={0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("an action hidden for a dog is left off that dog's wheel")
  void hiddenActionsAreLeftOut(final boolean shown) {
    final DogWheelSubActions registry = new DogWheelSubActions();
    registry.register(action(1, null));
    registry.register(
        new DogWheelSubAction(
            2, "test.action.2", BONE, (dog, player) -> null, (dog, player) -> {}, dog -> shown));

    assertEquals(
        shown ? List.of(1, 2) : List.of(1),
        registry.statesFor(null, null).stream().map(WheelSubActionState::id).toList());
  }
}
