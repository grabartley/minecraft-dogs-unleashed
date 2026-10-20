package com.grahambartley.dogsunleashed.item;

import java.util.Optional;

public final class WhistlePointRules {

  public static final double RANGE = 64.0;

  private WhistlePointRules() {}

  public enum Order {
    GO_THERE,
    ATTACK,
    FETCH,
    NONE
  }

  public enum PointedKind {
    ITEM,
    PLAYER,
    MOB,
    OTHER
  }

  public enum Unavailable {
    TOO_FAR("too_far"),
    SITTING("sitting"),
    ASLEEP("asleep"),
    RIDDEN("ridden");

    private final String suffix;

    Unavailable(final String suffix) {
      this.suffix = suffix;
    }

    public String messageKey() {
      return "message.dogs-unleashed.whistle." + this.suffix;
    }
  }

  public record MobFacts(
      boolean friendlyType,
      boolean tamed,
      boolean named,
      boolean leashed,
      boolean hostile,
      boolean neutral,
      boolean unsafe) {}

  public static Order orderForEntity(final PointedKind kind, final MobFacts mob) {
    return switch (kind) {
      case ITEM -> Order.FETCH;
      case MOB -> orderForMob(mob);
      case PLAYER, OTHER -> Order.NONE;
    };
  }

  static Order orderForMob(final MobFacts mob) {
    if (mob.friendlyType() || mob.tamed() || mob.unsafe()) {
      return Order.NONE;
    }
    if (mob.hostile()) {
      return Order.ATTACK;
    }
    if (mob.neutral() && !mob.named() && !mob.leashed()) {
      return Order.ATTACK;
    }
    return Order.NONE;
  }

  public static Optional<Unavailable> unavailable(
      final boolean loadedNearby,
      final boolean sitting,
      final boolean asleep,
      final boolean ridden) {
    if (!loadedNearby) {
      return Optional.of(Unavailable.TOO_FAR);
    }
    if (ridden) {
      return Optional.of(Unavailable.RIDDEN);
    }
    if (asleep) {
      return Optional.of(Unavailable.ASLEEP);
    }
    if (sitting) {
      return Optional.of(Unavailable.SITTING);
    }
    return Optional.empty();
  }

  public static boolean inRange(final double squaredDistance) {
    return squaredDistance <= RANGE * RANGE;
  }

  public static PointedKind kindOf(
      final boolean item,
      final boolean claimedByAnotherPlayer,
      final boolean player,
      final boolean mob) {
    if (item) {
      return claimedByAnotherPlayer ? PointedKind.OTHER : PointedKind.ITEM;
    }
    if (player) {
      return PointedKind.PLAYER;
    }
    return mob ? PointedKind.MOB : PointedKind.OTHER;
  }
}
