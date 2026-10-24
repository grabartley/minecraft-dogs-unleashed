package com.grahambartley.dogsunleashed.entity.trick;

import com.grahambartley.dogsunleashed.entity.DogAnimationKeys;
import java.util.Arrays;
import java.util.Optional;

public enum DogTrick {
  BEG(2, DogAnimationKeys.BEG, "dogs-unleashed:dog_treat", 48),
  ROLL_OVER(3, DogAnimationKeys.ROLL_OVER, "minecraft:slime_ball", 76),
  PLAY_DEAD(4, DogAnimationKeys.PLAY_DEAD, "minecraft:skeleton_skull", 104);

  private final int wheelActionId;
  private final String serializedId;
  private final String iconItemId;
  private final int durationTicks;

  DogTrick(
      final int wheelActionId,
      final String serializedId,
      final String iconItemId,
      final int durationTicks) {
    this.wheelActionId = wheelActionId;
    this.serializedId = serializedId;
    this.iconItemId = iconItemId;
    this.durationTicks = durationTicks;
  }

  public int wheelActionId() {
    return this.wheelActionId;
  }

  public String serializedId() {
    return this.serializedId;
  }

  public String animationKey() {
    return this.serializedId;
  }

  public String iconItemId() {
    return this.iconItemId;
  }

  public int durationTicks() {
    return this.durationTicks;
  }

  public long endsAt(final long startedAt) {
    return startedAt + this.durationTicks;
  }

  public String translationKey() {
    return "trick.dogs-unleashed." + this.serializedId;
  }

  public static Optional<DogTrick> fromSerializedId(final String id) {
    return Arrays.stream(values()).filter(trick -> trick.serializedId.equals(id)).findFirst();
  }
}
