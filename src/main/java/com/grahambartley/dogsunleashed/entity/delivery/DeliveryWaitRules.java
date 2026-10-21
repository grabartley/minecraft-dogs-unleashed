package com.grahambartley.dogsunleashed.entity.delivery;

public final class DeliveryWaitRules {

  public static final long WAIT_TICKS = 6000L;
  public static final double RECIPIENT_RANGE = 48.0;

  private DeliveryWaitRules() {}

  public static boolean headsHome(
      final boolean recipientPresent,
      final boolean sameWorld,
      final double distanceToRecipient,
      final boolean waitOver) {
    return !recipientPresent || !sameWorld || distanceToRecipient > RECIPIENT_RANGE || waitOver;
  }
}
