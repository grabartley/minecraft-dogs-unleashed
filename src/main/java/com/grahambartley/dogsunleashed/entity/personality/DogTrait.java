package com.grahambartley.dogsunleashed.entity.personality;

import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public enum DogTrait {
  BRAVE,
  TIMID,
  GREEDY,
  LAZY,
  CLINGY,
  PLAYFUL;

  private static final double NEUTRAL_SCALE = 1.0;

  public String serializedId() {
    return this.name().toLowerCase(Locale.ROOT);
  }

  public String translationKey() {
    return "personality.dogs-unleashed." + this.serializedId();
  }

  public static @Nullable DogTrait fromSerializedIdOrNull(final String id) {
    for (final DogTrait trait : values()) {
      if (trait.serializedId().equals(id)) {
        return trait;
      }
    }
    return null;
  }

  public boolean conflictsWith(final DogTrait other) {
    return switch (this) {
      case BRAVE -> other == TIMID;
      case TIMID -> other == BRAVE;
      case LAZY -> other == PLAYFUL;
      case PLAYFUL -> other == LAZY;
      case GREEDY, CLINGY -> false;
    };
  }

  public boolean holdsGround() {
    return this == BRAVE;
  }

  public double fleeHealthFraction() {
    return this == TIMID ? 0.5 : 0.0;
  }

  public double stanceOffset() {
    return switch (this) {
      case BRAVE -> 2.0;
      case TIMID -> -2.5;
      default -> 0.0;
    };
  }

  public boolean growlsAtThreats() {
    return this == BRAVE;
  }

  public boolean cowersAtThreats() {
    return this == TIMID;
  }

  public boolean joinsOwnerFights() {
    return this != TIMID;
  }

  public boolean snacks() {
    return this == GREEDY;
  }

  public boolean napsOnGround() {
    return this == LAZY;
  }

  public double napScale() {
    return this == LAZY ? 1.5 : NEUTRAL_SCALE;
  }

  public double idleSpeedScale() {
    return this == LAZY ? 0.8 : NEUTRAL_SCALE;
  }

  public double followDistanceScale() {
    return this == CLINGY ? 0.6 : NEUTRAL_SCALE;
  }

  public static double closestFollowDistanceScale() {
    double closest = NEUTRAL_SCALE;
    for (final DogTrait trait : values()) {
      closest = Math.min(closest, trait.followDistanceScale());
    }
    return closest;
  }

  public boolean whinesWhenLonely() {
    return this == CLINGY;
  }

  public boolean greetsWithHearts() {
    return this == CLINGY;
  }

  public boolean bringsToys() {
    return this == PLAYFUL;
  }
}
