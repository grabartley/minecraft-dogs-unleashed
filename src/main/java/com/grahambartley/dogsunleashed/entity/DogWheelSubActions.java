package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.network.payload.WheelSubActionState;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;

public final class DogWheelSubActions {

  public static final int MAX_ACTIONS = 16;
  public static final DogWheelSubActions REGISTERED = new DogWheelSubActions();

  private final List<DogWheelSubAction> actions = new ArrayList<>();

  public void register(final DogWheelSubAction action) {
    if (this.actions.size() >= MAX_ACTIONS) {
      throw new IllegalStateException("The actions wheel holds at most " + MAX_ACTIONS);
    }
    if (this.byId(action.id()).isPresent()) {
      throw new IllegalArgumentException("Duplicate wheel sub-action id " + action.id());
    }
    this.actions.add(action);
  }

  public List<DogWheelSubAction> all() {
    return List.copyOf(this.actions);
  }

  public Optional<DogWheelSubAction> byId(final int id) {
    return this.actions.stream().filter(action -> action.id() == id).findFirst();
  }

  public List<WheelSubActionState> statesFor(
      final UnleashedDogEntity dog, final ServerPlayerEntity player) {
    return this.actions.stream()
        .map(
            action ->
                new WheelSubActionState(
                    action.id(),
                    Objects.requireNonNullElse(action.refusal().refusalKey(dog, player), "")))
        .toList();
  }
}
