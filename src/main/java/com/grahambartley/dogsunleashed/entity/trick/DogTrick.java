package com.grahambartley.dogsunleashed.entity.trick;

import com.grahambartley.dogsunleashed.entity.DogAnimationKeys;
import com.grahambartley.dogsunleashed.entity.DogWheelIcons;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.util.Identifier;

public enum DogTrick {
  BEG(2, DogAnimationKeys.BEG, "beg", 48),
  ROLL_OVER(3, DogAnimationKeys.ROLL_OVER, "roll_over", 76),
  PLAY_DEAD(4, DogAnimationKeys.PLAY_DEAD, "play_dead", 104);

  public static final long NOT_STARTED = Long.MIN_VALUE;

  private final int wheelActionId;
  private final String serializedId;
  private final Identifier icon;
  private final int durationTicks;

  DogTrick(
      final int wheelActionId,
      final String serializedId,
      final String iconName,
      final int durationTicks) {
    this.wheelActionId = wheelActionId;
    this.serializedId = serializedId;
    this.icon = DogWheelIcons.sprite(iconName);
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

  public Identifier icon() {
    return this.icon;
  }

  public int durationTicks() {
    return this.durationTicks;
  }

  public boolean isRunning(final long startedAt, final long now) {
    return startedAt != NOT_STARTED && now >= startedAt && now < startedAt + this.durationTicks;
  }

  public String translationKey() {
    return "trick.dogs-unleashed." + this.serializedId;
  }

  public static Optional<DogTrick> fromSerializedId(final String id) {
    return Arrays.stream(values()).filter(trick -> trick.serializedId.equals(id)).findFirst();
  }
}
