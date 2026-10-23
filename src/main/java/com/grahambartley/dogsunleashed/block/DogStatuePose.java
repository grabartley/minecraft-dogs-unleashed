package com.grahambartley.dogsunleashed.block;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.mojang.serialization.Codec;
import java.util.Locale;

public enum DogStatuePose {
  SIT,
  STAND,
  HOWL;

  public static final Codec<DogStatuePose> CODEC =
      Codec.STRING.xmap(DogStatuePose::fromSerializedName, DogStatuePose::serializedName);

  public DogStatuePose next(final boolean howls) {
    return switch (this) {
      case SIT -> STAND;
      case STAND -> howls ? HOWL : SIT;
      case HOWL -> SIT;
    };
  }

  public DogStatuePose shownFor(final boolean howls) {
    return this == HOWL && !howls ? SIT : this;
  }

  public boolean sits() {
    return this != STAND;
  }

  public boolean howling() {
    return this == HOWL;
  }

  public void applyTo(final UnleashedDogEntity dog) {
    dog.setSitting(this.sits());
    dog.setInSittingPose(this.sits());
    dog.getAnimationState().setHowling(this.howling());
  }

  public String serializedName() {
    return this.name().toLowerCase(Locale.ROOT);
  }

  public static DogStatuePose fromSerializedName(final String name) {
    for (final DogStatuePose pose : values()) {
      if (pose.serializedName().equals(name)) {
        return pose;
      }
    }
    return SIT;
  }
}
