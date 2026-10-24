package com.grahambartley.dogsunleashed.entity.trick;

import com.grahambartley.dogsunleashed.entity.DogTricks;
import com.grahambartley.dogsunleashed.entity.DogWheelSubAction;
import com.grahambartley.dogsunleashed.entity.DogWheelSubActions;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class DogTrickWheelActions {

  public static final String ASLEEP_REFUSAL_KEY =
      "screen.dogs-unleashed.command_wheel.trick.sleeping";
  public static final String BUSY_REFUSAL_KEY = "screen.dogs-unleashed.command_wheel.trick.busy";

  private DogTrickWheelActions() {}

  public static void register(final DogWheelSubActions registry) {
    for (final DogTrick trick : DogTrick.values()) {
      registry.register(action(trick));
    }
  }

  static DogWheelSubAction action(final DogTrick trick) {
    return new DogWheelSubAction(
        trick.wheelActionId(),
        trick.translationKey(),
        Identifier.of(trick.iconItemId()),
        (dog, player) -> refusalKey(tricksOf(dog).readiness()),
        (dog, player) -> tricksOf(dog).perform(trick),
        dog -> tricksOf(dog).progress().isLearned(trick));
  }

  static @Nullable String refusalKey(final TrickReadiness readiness) {
    if (readiness.asleep()) {
      return ASLEEP_REFUSAL_KEY;
    }
    return readiness.mayStart() ? null : BUSY_REFUSAL_KEY;
  }

  private static DogTricks tricksOf(final UnleashedDogEntity dog) {
    return dog.getComponents().get(DogTricks.class);
  }
}
